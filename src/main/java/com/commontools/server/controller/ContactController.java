package com.commontools.server.controller;

import com.commontools.server.dto.ApiResponse;
import com.commontools.server.dto.ContactRequest;
import com.commontools.server.model.Feedback;
import com.commontools.server.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Deprecated
@RequestMapping("/legacy/contact")
@RequiredArgsConstructor
public class ContactController {
    private final FeedbackService feedbackService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendContactEmail(@Valid @RequestBody ContactRequest request) {
        log.info("收到联系表单请求 - 姓名: {}, 邮箱: {}, 主题: {}", request.getName(), request.getEmail(), request.getSubject());
        
        try {
            // 已弃用：保存为 feedbacks 表记录并发送通知（建议使用 /feedback/public）
            Feedback feedback = feedbackService.createFeedbackFromContact(request);

            if (feedback.getEmailSent() != null && feedback.getEmailSent()) {
                log.info("联系表单已保存为反馈 - ID: {}", feedback.getId());
                return ResponseEntity.ok(ApiResponse.success("Email sent successfully", null));
            } else {
                log.warn("联系表单已保存为反馈但邮件发送可能失败 - ID: {}", feedback.getId());
                return ResponseEntity.ok(ApiResponse.error("Failed to send email", null));
            }
        } catch (Exception e) {
            log.error("联系表单处理失败 - 邮箱: {}, 错误: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to process contact form", e.getMessage()));
        }
    }
}
