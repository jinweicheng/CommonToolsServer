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
        } catch (org.springframework.mail.MailAuthenticationException e) {
            throw new RuntimeException("邮件发送失败: 认证失败，请检查邮件服务器配置（用户名、密码或应用专用密码）", e);
        } catch (org.springframework.mail.MailException e) {
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
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

    /**
     * 发送联系表单邮件给管理员
     */
    public void sendContactEmailToAdmin(String name, String email, String subject, String message) {
        String adminContent = String.format(
                "收到来自联系表单的消息：\n\n" +
                "姓名：%s\n" +
                "邮箱：%s\n" +
                "主题：%s\n\n" +
                "消息内容：\n%s\n\n" +
                "请及时处理。",
                name, email, subject, message
        );

        sendFeedbackEmail(adminEmail, "【CommonTools】联系表单：" + subject, adminContent);
    }

    /**
     * 发送联系表单确认邮件给用户
     */
    public void sendContactConfirmation(String userEmail, String name, String subject) {
        String content = String.format(
                "尊敬的 %s，\n\n" +
                "感谢您联系 CommonTools！我们已收到您的消息：\n" +
                "主题：%s\n\n" +
                "我们会尽快回复您。\n\n" +
                "此邮件为自动发送，请勿回复。\n\n" +
                "CommonTools 团队",
                name, subject
        );

        sendFeedbackEmail(userEmail, "【CommonTools】联系确认", content);
    }
}
