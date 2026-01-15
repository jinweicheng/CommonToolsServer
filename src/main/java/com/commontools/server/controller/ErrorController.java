package com.commontools.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Object> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        log.error("========== 错误请求 ==========");
        log.error("状态码: {}", status);
        log.error("错误消息: {}", message);
        log.error("请求URI: {}", requestUri);
        log.error("完整URL: {}", request.getRequestURL());
        log.error("Context Path: {}", request.getContextPath());
        log.error("Servlet Path: {}", request.getServletPath());
        log.error("Path Info: {}", request.getPathInfo());
        if (exception != null) {
            log.error("异常: ", (Exception) exception);
        }
        log.error("==============================");

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (status != null) {
            try {
                httpStatus = HttpStatus.valueOf(Integer.parseInt(status.toString()));
            } catch (Exception e) {
                // 忽略
            }
        }

        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), 
                        message != null ? message.toString() : "Unknown error",
                        requestUri != null ? requestUri.toString() : request.getRequestURI()));
    }

    public static class ErrorResponse {
        private int status;
        private String message;
        private String path;

        public ErrorResponse(int status, String message, String path) {
            this.status = status;
            this.message = message;
            this.path = path;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
