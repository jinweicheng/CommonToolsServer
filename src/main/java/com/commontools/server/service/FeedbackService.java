package com.commontools.server.service;

import com.commontools.server.dto.FeedbackRequest;
import com.commontools.server.model.Feedback;
import com.commontools.server.model.User;
import com.commontools.server.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final EmailService emailService;

    @Transactional
    public Feedback createFeedback(FeedbackRequest request, User user) {
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setSubject(request.getSubject());
        // prefer `content`, fall back to `message` for compatibility with external callers
        feedback.setContent(request.getContent() != null ? request.getContent() : request.getMessage());
        feedback.setCategory(request.getCategory() != null ? request.getCategory() : "other");
        feedback.setStatus(Feedback.FeedbackStatus.PENDING);
        feedback.setEmailSent(false);

        // map optional client metadata
        feedback.setName(request.getName());
        feedback.setEmail(request.getEmail());
        feedback.setMessage(request.getMessage());
        feedback.setClientUserId(request.getUserId());
        feedback.setUserAgent(request.getUserAgent());
        feedback.setDeviceType(request.getDeviceType());
        feedback.setBrowser(request.getBrowser());
        feedback.setOs(request.getOs());
        feedback.setIpAddress(request.getIpAddress());
        feedback.setClientTimestamp(request.getTimestamp());

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // 发送邮件给管理员
        try {
            String contactEmail = (user != null ? user.getEmail() : request.getEmail());
            String contactName = (user != null ? user.getUsername() : request.getName());
            String contentForMail = feedback.getContent();
            emailService.sendFeedbackToAdmin(
                contactEmail,
                contactName,
                request.getSubject(),
                contentForMail
            );
            // 发送确认邮件给用户（当有邮箱时）
            if (contactEmail != null && !contactEmail.isBlank()) {
            emailService.sendFeedbackConfirmation(
                contactEmail,
                contactName,
                request.getSubject()
            );
            }
            savedFeedback.setEmailSent(true);
            feedbackRepository.save(savedFeedback);
        } catch (Exception e) {
            // 记录日志但不影响反馈创建
            System.err.println("邮件发送失败: " + e.getMessage());
        }

        return savedFeedback;
    }

    @Transactional
    public Feedback createFeedbackFromContact(com.commontools.server.dto.ContactRequest request) {
        Feedback feedback = new Feedback();
        feedback.setUser(null);
        feedback.setSubject(request.getSubject());
        feedback.setContent(request.getMessage());
        feedback.setCategory("contact");
        feedback.setStatus(Feedback.FeedbackStatus.PENDING);
        feedback.setEmailSent(false);

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // send email to admin with contact info
        try {
            emailService.sendFeedbackToAdmin(
                    request.getEmail(),
                    request.getName(),
                    request.getSubject(),
                    request.getMessage()
            );
            emailService.sendFeedbackConfirmation(
                    request.getEmail(),
                    request.getName(),
                    request.getSubject()
            );
            savedFeedback.setEmailSent(true);
            feedbackRepository.save(savedFeedback);
        } catch (Exception e) {
            // log but continue
        }

        return savedFeedback;
    }

    public List<Feedback> getUserFeedbacks(User user) {
        return feedbackRepository.findByUser(user);
    }

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public List<Feedback> getFeedbacksByStatus(Feedback.FeedbackStatus status) {
        return feedbackRepository.findByStatus(status);
    }

    public Feedback getFeedbackById(Long feedbackId, User user) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("反馈不存在"));
        // 用户只能查看自己的反馈，管理员可以查看所有
        if (!feedback.getUser().getId().equals(user.getId()) && 
            !user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("无权访问该反馈");
        }
        return feedback;
    }

    @Transactional
    public Feedback updateFeedbackStatus(Long feedbackId, Feedback.FeedbackStatus status, String adminReply, User admin) {
        if (!admin.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("只有管理员可以更新反馈状态");
        }

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("反馈不存在"));
        feedback.setStatus(status);
        if (adminReply != null) {
            feedback.setAdminReply(adminReply);
        }
        return feedbackRepository.save(feedback);
    }
}
