package com.commontools.server.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StatisticsRequest {
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotEmpty(message = "统计数据不能为空")
    @Valid
    private List<StatisticsItem> statistics;

    @Data
    public static class StatisticsItem {
        @NotBlank(message = "模块名称不能为空")
        private String module;

        @NotBlank(message = "操作名称不能为空")
        private String action;

        @NotBlank(message = "端点路径不能为空")
        private String endpoint;

        private String ipAddress;
        private String userAgent;
        private String deviceType;
        private String browser;
        private String os;

        @NotBlank(message = "统计日期时间不能为空")
        private String statDate; // 格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd (默认00:00:00)
    }
}
