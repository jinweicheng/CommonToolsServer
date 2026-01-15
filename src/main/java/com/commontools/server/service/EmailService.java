package com.commontools.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromEmail;

    @Value("${mail.admin}")
    private String adminEmail;

    public void sendFeedbackEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    public void sendFeedbackToAdmin(String userEmail, String username, String subject, String content) {
        String adminContent = String.format(
                "收到来自用户 %s (%s) 的反馈：\n\n" +
                "主题：%s\n\n" +
                "内容：\n%s\n\n" +
                "请及时处理。",
                username, userEmail, subject, content
        );

        sendFeedbackEmail(adminEmail, "【CommonTools】用户反馈：" + subject, adminContent);
    }

    public void sendFeedbackConfirmation(String userEmail, String username, String subject) {
        String content = String.format(
                "尊敬的 %s 用户，\n\n" +
                "感谢您的反馈！我们已收到您的反馈：\n" +
                "主题：%s\n\n" +
                "我们会尽快处理您的反馈，并在处理完成后通知您。\n\n" +
                "此邮件为自动发送，请勿回复。\n\n" +
                "CommonTools 团队",
                username, subject
        );

        sendFeedbackEmail(userEmail, "【CommonTools】反馈确认", content);
    }
}
