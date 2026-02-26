package com.commontools.server.service;

import com.commontools.server.dto.StatisticsRequest;
import com.commontools.server.model.UsageStatistics;
import com.commontools.server.repository.UsageStatisticsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
//    public String getClientIpAddress(HttpServletRequest request) {
//        String ip = request.getHeader("X-Forwarded-For");
//        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("X-Real-IP");
//        }
//        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getRemoteAddr();
//        }
//        // 处理多个IP的情况，取第一个
//        if (ip != null && ip.contains(",")) {
//            ip = ip.split(",")[0].trim();
//        }
//        return ip;
//    }

    /**
     * 批量记录统计数据（异步执行，避免阻塞主请求）
     */
    @Async("statisticsTaskExecutor")
    @Transactional
    public void recordStatisticsAsync(String userId, List<StatisticsRequest.StatisticsItem> statistics, 
                                      String clientIp, String userAgent) {
        try {
            recordStatistics(userId, statistics, clientIp, userAgent);
        } catch (Exception e) {
            log.error("异步批量记录统计数据失败: userId={}", userId, e);
        }
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

                // 查找是否已存在相同的统计记录（不包含日期时间，只根据用户、模块、操作、端点、IP判断）
                // 只查询最新的一条记录
                Pageable pageable = PageRequest.of(0, 1);
                List<UsageStatistics> existingStatsList = statisticsRepository
                        .findByUserIdAndModuleAndActionAndEndpointAndIpAddress(
                                userId, 
                                item.getModule(), 
                                item.getAction(), 
                                item.getEndpoint(),
                                ipAddress,
                                pageable
                        );
                Optional<UsageStatistics> existingStats = existingStatsList.isEmpty() 
                        ? Optional.empty() 
                        : Optional.of(existingStatsList.get(0));

                if (existingStats.isPresent()) {
                    // 如果已存在，增加计数并更新User-Agent和stat_date
                    UsageStatistics stats = existingStats.get();
                    stats.setCount(stats.getCount() + 1);
                    stats.setStatDate(statDate); // 更新为最新的访问时间
                    if (finalUserAgent != null) {
                        stats.setUserAgent(finalUserAgent);
                    }
                    // 更新其他可能变化的字段
                    if (item.getDeviceType() != null) {
                        stats.setDeviceType(item.getDeviceType());
                    }
                    if (item.getBrowser() != null) {
                        stats.setBrowser(item.getBrowser());
                    }
                    if (item.getOs() != null) {
                        stats.setOs(item.getOs());
                    }
                    statisticsRepository.save(stats);
                    // 仅在debug级别记录详细信息，避免高并发下大量日志输出
                    if (log.isDebugEnabled()) {
                        log.debug("更新统计记录 - userId: {}, module: {}, action: {}, count: {}", 
                                userId, stats.getModule(), stats.getAction(), stats.getCount());
                    }
                } else {
                    // 如果不存在，尝试创建新记录
                    try {
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
                        statisticsRepository.save(newStats);
                        if (log.isDebugEnabled()) {
                            log.debug("插入新统计记录 - userId: {}, module: {}, action: {}", 
                                    userId, newStats.getModule(), newStats.getAction());
                        }
                    } catch (DataIntegrityViolationException e) {
                        // 如果出现唯一约束冲突，重新查询并更新
                        log.debug("检测到唯一约束冲突，重试更新: userId={}, module={}", userId, item.getModule());
                        
                        // 使用新事务重新查询并更新
                        retryUpdateAfterConflict(userId, item.getModule(), item.getAction(), 
                                                item.getEndpoint(), ipAddress, statDate, finalUserAgent,
                                                item.getDeviceType(), item.getBrowser(), item.getOs());
                    }
                }
                recorded++;
            } catch (Exception e) {
                // 记录单条失败不影响其他记录
                log.warn("记录单条统计失败: userId={}, module={}, action={}", 
                        userId, item.getModule(), item.getAction(), e);
            }
        }

        return recorded;
    }

    /**
     * 在唯一约束冲突后，使用新事务重新查询并更新
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retryUpdateAfterConflict(String userId, String module, String action, 
                                        String endpoint, String ipAddress, LocalDateTime statDate,
                                        String userAgent, String deviceType, String browser, String os) {
        // 重新查询（不包含stat_date）
        Pageable pageable = PageRequest.of(0, 1);
        List<UsageStatistics> retryStatsList = statisticsRepository
                .findByUserIdAndModuleAndActionAndEndpointAndIpAddress(
                        userId, module, action, endpoint, ipAddress, pageable
                );
        Optional<UsageStatistics> retryStats = retryStatsList.isEmpty() 
                ? Optional.empty() 
                : Optional.of(retryStatsList.get(0));
        
        if (retryStats.isPresent()) {
            UsageStatistics stats = retryStats.get();
            stats.setCount(stats.getCount() + 1);
            stats.setStatDate(statDate); // 更新为最新的访问时间
            if (userAgent != null) {
                stats.setUserAgent(userAgent);
            }
            if (deviceType != null) {
                stats.setDeviceType(deviceType);
            }
            if (browser != null) {
                stats.setBrowser(browser);
            }
            if (os != null) {
                stats.setOs(os);
            }
            statisticsRepository.save(stats);
            if (log.isDebugEnabled()) {
                log.debug("并发冲突后更新统计记录 - userId: {}, module: {}, count: {}", 
                        userId, module, stats.getCount());
            }
        } else {
            // 如果仍然查询不到，可能是数据已被删除或stat_date不同导致唯一约束冲突
            // 尝试使用包含stat_date的精确查询
            Optional<UsageStatistics> exactStats = statisticsRepository
                    .findByUserIdAndModuleAndActionAndStatDateAndEndpointAndIpAddress(
                            userId, module, action, statDate, endpoint, ipAddress
                    );
            
            if (exactStats.isPresent()) {
                // 如果找到了精确匹配的记录，更新计数
                UsageStatistics stats = exactStats.get();
                stats.setCount(stats.getCount() + 1);
                if (userAgent != null) {
                    stats.setUserAgent(userAgent);
                }
                statisticsRepository.save(stats);
            } else {
                log.warn("唯一约束冲突后重新查询仍然失败: userId={}, module={}, action={}", 
                        userId, module, action);
            }
        }
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
}
