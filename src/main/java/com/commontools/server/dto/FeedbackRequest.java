package com.commontools.server.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {
    @Size(max = 200, message = "主题长度不能超过200个字符")
    private String subject;

    // `content` is preferred; some clients send `message` instead.
    // Do not mark `content` as @NotBlank so we can accept `message` as fallback.
    @Size(max = 5000, message = "内容长度不能超过5000个字符")
    private String content;

    private String category; // bug, feature, suggestion, other

    // optional client metadata (from frontend/contact form)
    private String name; // submitter name (may be anonymous when authenticated)
    private String email; // submitter email
    @Size(max = 5000, message = "内容长度不能超过5000个字符")
    private String message; // alternative to `content` used by some clients
    private String userId; // client-side user id (optional)
    private String userAgent;
    private String deviceType;
    private String browser;
    private String os;
    private String ipAddress;
    private String timestamp; // client-side timestamp as string

    @AssertTrue(message = "反馈内容不能为空")
    public boolean isContentOrMessagePresent() {
        return (content != null && !content.isBlank()) || (message != null && !message.isBlank());
    }
}
