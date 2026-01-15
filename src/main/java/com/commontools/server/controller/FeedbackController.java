package com.commontools.server.controller;

import com.commontools.server.dto.FeedbackRequest;
import com.commontools.server.model.Feedback;
import com.commontools.server.model.User;
import com.commontools.server.service.FeedbackService;
import com.commontools.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(
            @Valid @RequestBody FeedbackRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        Feedback feedback = feedbackService.createFeedback(request, user);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Feedback>> getMyFeedbacks(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Feedback> feedbacks = feedbackService.getUserFeedbacks(user);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<Feedback> getFeedback(
            @PathVariable Long feedbackId,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        Feedback feedback = feedbackService.getFeedbackById(feedbackId, user);
        return ResponseEntity.ok(feedback);
    }

    // 管理员接口
    @GetMapping("/admin/all")
    public ResponseEntity<List<Feedback>> getAllFeedbacks(Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (!user.getRole().equals(User.UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Feedback> feedbacks = feedbackService.getAllFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/admin/status/{status}")
    public ResponseEntity<List<Feedback>> getFeedbacksByStatus(
            @PathVariable String status,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (!user.getRole().equals(User.UserRole.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Feedback.FeedbackStatus feedbackStatus = Feedback.FeedbackStatus.valueOf(status.toUpperCase());
        List<Feedback> feedbacks = feedbackService.getFeedbacksByStatus(feedbackStatus);
        return ResponseEntity.ok(feedbacks);
    }

    @PutMapping("/admin/{feedbackId}/status")
    public ResponseEntity<Feedback> updateFeedbackStatus(
            @PathVariable Long feedbackId,
            @RequestParam String status,
            @RequestParam(required = false) String adminReply,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        Feedback.FeedbackStatus feedbackStatus = Feedback.FeedbackStatus.valueOf(status.toUpperCase());
        Feedback feedback = feedbackService.updateFeedbackStatus(feedbackId, feedbackStatus, adminReply, user);
        return ResponseEntity.ok(feedback);
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
