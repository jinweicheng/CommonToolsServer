package com.commontools.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {
    @NotBlank(message = "反馈主题不能为空")
    @Size(max = 200, message = "主题长度不能超过200个字符")
    private String subject;

    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 5000, message = "内容长度不能超过5000个字符")
    private String content;

    private String category; // bug, feature, suggestion, other
}
