package com.commontools.server.controller;

import com.commontools.server.dto.ApiResponse;
import com.commontools.server.dto.StatisticsRequest;
import com.commontools.server.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 上报用户使用统计数据
     * POST /api/statistics/usage
     */
    @PostMapping("/usage")
    public ResponseEntity<ApiResponse<Void>> recordUsage(
            @Valid @RequestBody StatisticsRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        log.info("========== 统计接口被调用 ==========");
        log.info("请求方法: {}", httpRequest.getMethod());
        log.info("请求URL: {}", httpRequest.getRequestURL());
        log.info("请求URI: {}", httpRequest.getRequestURI());
        log.info("Context Path: {}", httpRequest.getContextPath());
        log.info("Servlet Path: {}", httpRequest.getServletPath());
        log.info("Path Info: {}", httpRequest.getPathInfo());
        log.info("用户ID: {}", request.getUserId());
        log.info("统计记录数: {}", request.getStatistics() != null ? request.getStatistics().size() : 0);
        
        // 验证请求数据
        if (bindingResult.hasErrors()) {
            String errorDetails = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Invalid request data");
            
            log.error("请求验证失败: {}", errorDetails);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request data", errorDetails));
        }

        if (request.getStatistics() == null || request.getStatistics().isEmpty()) {
            log.error("统计数据为空");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request data", "Statistics array cannot be empty"));
        }

        try {
            // 获取客户端真实IP和User-Agent
            String clientIp = statisticsService.getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            log.info("客户端IP: {}", clientIp);
            log.info("User-Agent: {}", userAgent);

            // 批量记录统计数据
            int recorded = statisticsService.recordStatistics(
                    request.getUserId(),
                    request.getStatistics(),
                    clientIp,
                    userAgent
            );

            log.info("成功记录 {} 条统计数据", recorded);
            log.info("========================================");
            
            return ResponseEntity.ok(
                    ApiResponse.success("Statistics recorded successfully", recorded)
            );
        } catch (Exception e) {
            log.error("记录统计失败", e);
            log.info("========================================");
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Internal server error", e.getMessage()));
        }
    }

    /**
     * 查询统计数据（用于调试）
     * GET /api/statistics/debug?userId=xxx
     */
    @GetMapping("/debug")
    public ResponseEntity<ApiResponse<Object>> debugStatistics(
            @RequestParam(required = false) String userId) {
        try {
            if (userId != null && !userId.isEmpty()) {
                var stats = statisticsService.getStatisticsByUserId(userId);
                log.info("查询到 {} 条统计记录 (userId={})", stats.size(), userId);
                return ResponseEntity.ok(ApiResponse.success("查询成功", stats));
            } else {
                var allStats = statisticsService.getAllStatistics();
                log.info("查询到 {} 条统计记录 (全部)", allStats.size());
                return ResponseEntity.ok(ApiResponse.success("查询成功", allStats));
            }
        } catch (Exception e) {
            log.error("查询统计失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("查询失败", e.getMessage()));
        }
    }
}
