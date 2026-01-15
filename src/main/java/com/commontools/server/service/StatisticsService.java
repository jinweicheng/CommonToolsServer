package com.commontools.server.service;

import com.commontools.server.dto.StatisticsRequest;
import com.commontools.server.model.UsageStatistics;
import com.commontools.server.repository.UsageStatisticsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final UsageStatisticsRepository statisticsRepository;

    /**
     * 获取客户端真实IP地址
     */
    public String getClientIpAddress(HttpServletRequest request) {
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

    /**
     * 批量记录统计数据
     */
    @Transactional
    public int recordStatistics(String userId, List<StatisticsRequest.StatisticsItem> statistics, 
                               String clientIp, String userAgent) {
        int recorded = 0;
        // 支持多种日期时间格式
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd") // 如果只有日期，默认使用当天00:00:00
        };

        for (StatisticsRequest.StatisticsItem item : statistics) {
            try {
                LocalDateTime statDate = null;
                // 尝试解析日期时间字符串
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        if (item.getStatDate().length() == 10) {
                            // 如果只有日期（yyyy-MM-dd），转换为当天的00:00:00
                            statDate = LocalDateTime.parse(item.getStatDate() + " 00:00:00", 
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        } else {
                            statDate = LocalDateTime.parse(item.getStatDate(), formatter);
                        }
                        break;
                    } catch (Exception e) {
                        // 继续尝试下一个格式
                        continue;
                    }
                }
                
                if (statDate == null) {
                    throw new IllegalArgumentException("无法解析日期时间格式: " + item.getStatDate());
                }
                
                // 使用后端获取的真实IP（如果前端传null）
                String ipAddress = item.getIpAddress() != null && !item.getIpAddress().isEmpty() 
                        ? item.getIpAddress() 
                        : clientIp;
                
                // 使用请求头中的User-Agent（如果前端未传）
                String finalUserAgent = item.getUserAgent() != null && !item.getUserAgent().isEmpty()
                        ? item.getUserAgent()
                        : userAgent;

                // 查找是否已存在相同的统计记录
                Optional<UsageStatistics> existingStats = statisticsRepository
                        .findByUserIdAndModuleAndActionAndStatDateAndEndpointAndIpAddress(
                                userId, 
                                item.getModule(), 
                                item.getAction(), 
                                statDate, 
                                item.getEndpoint(),
                                ipAddress
                        );

                if (existingStats.isPresent()) {
                    // 如果已存在，增加计数并更新User-Agent
                    UsageStatistics stats = existingStats.get();
                    int oldCount = stats.getCount();
                    stats.setCount(stats.getCount() + 1);
                    if (finalUserAgent != null) {
                        stats.setUserAgent(finalUserAgent);
                    }
                    UsageStatistics saved = statisticsRepository.save(stats);
                    log.info("========== 更新统计记录 ==========");
                    log.info("记录ID: {}", saved.getId());
                    log.info("用户ID: {}", saved.getUserId());
                    log.info("模块: {}", saved.getModule());
                    log.info("操作: {}", saved.getAction());
                    log.info("旧计数: {} -> 新计数: {}", oldCount, saved.getCount());
                    log.info("日期时间: {}", saved.getStatDate());
                    log.info("====================================");
                } else {
                    // 如果不存在，创建新记录
                    UsageStatistics newStats = new UsageStatistics();
                    newStats.setUserId(userId);
                    newStats.setModule(item.getModule());
                    newStats.setAction(item.getAction());
                    newStats.setEndpoint(item.getEndpoint());
                    newStats.setIpAddress(ipAddress);
                    newStats.setUserAgent(finalUserAgent);
                    newStats.setDeviceType(item.getDeviceType() != null ? item.getDeviceType() : "UNKNOWN");
                    newStats.setBrowser(item.getBrowser());
                    newStats.setOs(item.getOs());
                    newStats.setStatDate(statDate);
                    newStats.setCount(1);
                    UsageStatistics saved = statisticsRepository.save(newStats);
                    log.info("========== 插入新统计记录 ==========");
                    log.info("记录ID: {}", saved.getId());
                    log.info("用户ID: {}", saved.getUserId());
                    log.info("模块: {}", saved.getModule());
                    log.info("操作: {}", saved.getAction());
                    log.info("端点: {}", saved.getEndpoint());
                    log.info("计数: {}", saved.getCount());
                    log.info("日期时间: {}", saved.getStatDate());
                    log.info("IP: {}", saved.getIpAddress());
                    log.info("User-Agent: {}", saved.getUserAgent());
                    log.info("====================================");
                }
                recorded++;
            } catch (Exception e) {
                // 记录单条失败不影响其他记录
                log.error("记录统计失败: userId={}, module={}, action={}, error={}", 
                        userId, item.getModule(), item.getAction(), e.getMessage(), e);
            }
        }

        return recorded;
    }

    /**
     * 根据用户ID查询统计数据（用于调试）
     */
    public List<UsageStatistics> getStatisticsByUserId(String userId) {
        return statisticsRepository.findByUserId(userId);
    }

    /**
     * 查询所有统计数据（用于调试）
     */
    public List<UsageStatistics> getAllStatistics() {
        return statisticsRepository.findAll();
    }
}
