package com.commontools.server.controller;

import com.commontools.server.dto.ApiResponse;
import com.commontools.server.dto.ContactRequest;
import com.commontools.server.model.Contact;
import com.commontools.server.service.ContactService;
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
@RequestMapping("/email")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendContactEmail(@Valid @RequestBody ContactRequest request) {
        log.info("收到联系表单请求 - 姓名: {}, 邮箱: {}, 主题: {}", request.getName(), request.getEmail(), request.getSubject());
        
        try {
            // 保存联系记录并发送邮件
            Contact contact = contactService.createContact(request);
            
            if (contact.getEmailSent()) {
                log.info("联系表单处理成功 - ID: {}, 邮箱: {}", contact.getId(), contact.getEmail());
                return ResponseEntity.ok(ApiResponse.success("Email sent successfully", null));
            } else {
                log.warn("联系表单已保存但邮件发送失败 - ID: {}, 邮箱: {}, 错误: {}", 
                        contact.getId(), contact.getEmail(), contact.getErrorMessage());
                return ResponseEntity.ok(ApiResponse.error("Failed to send email", contact.getErrorMessage()));
            }
        } catch (Exception e) {
            log.error("联系表单处理失败 - 邮箱: {}, 错误: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("Failed to process contact form", e.getMessage()));
        }
    }
}
