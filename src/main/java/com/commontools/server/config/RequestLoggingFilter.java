package com.commontools.server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Slf4j
@Component
@Order(1) // 确保在SecurityFilter之前执行
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 记录请求信息（记录所有请求，包括404）
        logRequest(request);
        
        // 包装请求和响应以支持多次读取
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logResponse(wrappedRequest, wrappedResponse, duration);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n========== 请求信息 ==========\n");
        logMessage.append("请求方法: ").append(request.getMethod()).append("\n");
        logMessage.append("请求URL: ").append(request.getRequestURL()).append("\n");
        logMessage.append("请求URI: ").append(request.getRequestURI()).append("\n");
        logMessage.append("查询参数: ").append(request.getQueryString()).append("\n");
        logMessage.append("Context Path: ").append(request.getContextPath()).append("\n");
        logMessage.append("Servlet Path: ").append(request.getServletPath()).append("\n");
        logMessage.append("Path Info: ").append(request.getPathInfo()).append("\n");
        logMessage.append("远程地址: ").append(request.getRemoteAddr()).append("\n");
        logMessage.append("远程主机: ").append(request.getRemoteHost()).append("\n");
        logMessage.append("远程端口: ").append(request.getRemotePort()).append("\n");
        
        // 请求头
        logMessage.append("请求头:\n");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            logMessage.append("  ").append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
        });
        
        logMessage.append("================================\n");
        log.info(logMessage.toString());
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n========== 响应信息 ==========\n");
        logMessage.append("请求URI: ").append(request.getRequestURI()).append("\n");
        logMessage.append("状态码: ").append(response.getStatus()).append("\n");
        logMessage.append("响应时间: ").append(duration).append("ms\n");
        
        // 响应头
        logMessage.append("响应头:\n");
        response.getHeaderNames().forEach(headerName -> {
            logMessage.append("  ").append(headerName).append(": ").append(response.getHeader(headerName)).append("\n");
        });
        
        // 响应体（如果有）
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            try {
                String contentString = new String(content, response.getCharacterEncoding());
                logMessage.append("响应体: ").append(contentString).append("\n");
            } catch (UnsupportedEncodingException e) {
                logMessage.append("响应体: [无法解析]\n");
            }
        }
        
        logMessage.append("================================\n");
        log.info(logMessage.toString());
    }
}
