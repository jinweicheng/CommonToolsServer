package com.commontools.server.aspect;

import com.commontools.server.model.UsageStatistics;
import com.commontools.server.repository.UsageStatisticsRepository;
import com.commontools.server.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsAspect {
    private final UsageStatisticsRepository statisticsRepository;
    private final StatisticsService statisticsService;

    @Value("${statistics.enabled:true}")
    private boolean statisticsEnabled;

    @Pointcut("execution(* com.commontools.server.controller..*(..))")
    public void controllerMethods() {}

    @AfterReturning(pointcut = "controllerMethods()")
    public void recordStatistics(JoinPoint joinPoint) {
        if (!statisticsEnabled) {
            return;
        }

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            String endpoint = request.getRequestURI();
            String method = request.getMethod();

            // 获取用户ID（优先从请求参数或请求头获取）
            String userId = getUserIdFromRequest(request);
            if (userId == null || userId.isEmpty()) {
                // 如果没有用户ID，跳过统计
                return;
            }

            // 解析模块和操作
            // 移除 /api 前缀
            String path = endpoint.replaceFirst("^/api", "");
            String[] parts = path.split("/");
            
            String module = "unknown";
            String action = method.toLowerCase();

            // 解析模块名称（通常是路径的第一部分，如 /files, /passwords, /feedback等）
            if (parts.length > 1 && !parts[1].isEmpty()) {
                module = parts[1];
            }

            // 解析操作名称
            if (parts.length > 2) {
                String lastPart = parts[parts.length - 1];
                // 如果是数字，说明是ID，查找倒数第二个部分
                if (lastPart.matches("\\d+")) {
                    if (parts.length > 2) {
                        action = parts[parts.length - 2];
                    }
                } else {
                    action = lastPart;
                }
            }

            // 特殊处理一些常见的操作
            if (endpoint.contains("/upload")) {
                action = "upload";
            } else if (endpoint.contains("/download")) {
                action = "download";
            } else if (endpoint.contains("/me") || endpoint.contains("/profile")) {
                action = "view";
            } else if (endpoint.contains("/my")) {
                action = "list";
            } else if (method.equals("POST")) {
                action = "create";
            } else if (method.equals("PUT")) {
                action = "update";
            } else if (method.equals("DELETE")) {
                action = "delete";
            } else if (method.equals("GET") && action.equals(method.toLowerCase())) {
                action = "list";
            }

            // 获取请求信息
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String deviceType = parseDeviceType(userAgent);
            String browser = parseBrowser(userAgent);
            String os = parseOS(userAgent);

            // 异步记录统计，不阻塞主请求
            recordUsageAsync(userId, module, action, endpoint, ipAddress, userAgent, 
                           deviceType, browser, os);
        } catch (Exception e) {
            // 记录统计失败不应影响主业务
            log.warn("统计记录失败: {}", e.getMessage());
        }
    }

    /**
     * 异步记录使用统计，避免阻塞主请求
     */
    @Async("statisticsTaskExecutor")
    public void recordUsageAsync(String userId, String module, String action, String endpoint, 
                                String ipAddress, String userAgent, 
                                String deviceType, String browser, String os) {
        try {
            LocalDateTime now = LocalDateTime.now();
            // 将时间精确到秒（去掉毫秒和微秒），便于聚合统计
            LocalDateTime statDate = now.withNano(0);
            
            Optional<UsageStatistics> existingStats = statisticsRepository
                    .findByUserIdAndModuleAndActionAndStatDateAndEndpointAndIpAddress(
                            userId, module, action, statDate, endpoint, ipAddress);

            if (existingStats.isPresent()) {
                // 如果已存在，增加计数
                UsageStatistics stats = existingStats.get();
                stats.setCount(stats.getCount() + 1);
                statisticsRepository.save(stats);
            } else {
                // 如果不存在，创建新记录
                UsageStatistics newStats = new UsageStatistics();
                newStats.setUserId(userId);
                newStats.setModule(module);
                newStats.setAction(action);
                newStats.setEndpoint(endpoint);
                newStats.setIpAddress(ipAddress);
                newStats.setUserAgent(userAgent);
                newStats.setDeviceType(deviceType);
                newStats.setBrowser(browser);
                newStats.setOs(os);
                newStats.setStatDate(statDate);
                newStats.setCount(1);
                statisticsRepository.save(newStats);
            }
        } catch (Exception e) {
            log.error("异步统计记录失败: userId={}, module={}, action={}", userId, module, action, e);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String parseDeviceType(String userAgent) {
        if (userAgent == null) return "UNKNOWN";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "MOBILE";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "TABLET";
        } else {
            return "PC";
        }
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null) return null;
        String ua = userAgent.toLowerCase();
        if (ua.contains("chrome") && !ua.contains("edg")) {
            return "Chrome";
        } else if (ua.contains("firefox")) {
            return "Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        } else if (ua.contains("edg")) {
            return "Edge";
        } else if (ua.contains("opera")) {
            return "Opera";
        }
        return "Unknown";
    }

    private String parseOS(String userAgent) {
        if (userAgent == null) return null;
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) {
            return "Windows";
        } else if (ua.contains("mac")) {
            return "macOS";
        } else if (ua.contains("linux")) {
            return "Linux";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("ios") || ua.contains("iphone") || ua.contains("ipad")) {
            return "iOS";
        }
        return "Unknown";
    }

    /**
     * 从请求中获取用户ID（UUID字符串）
     * 优先级：请求参数userId > 请求头X-User-Id
     */
    private String getUserIdFromRequest(HttpServletRequest request) {
        // 1. 尝试从请求参数获取用户ID
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && !userIdParam.isEmpty()) {
            return userIdParam;
        }

        // 2. 尝试从请求头获取用户ID
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            return userIdHeader;
        }

        return null;
    }
}
