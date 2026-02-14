package com.commontools.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false, length = 200)
    private String subject; // 反馈主题

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 反馈内容

    @Column(columnDefinition = "TEXT")
    private String message; // optional raw message from clients (some clients send `message` instead of `content`)

    @Column(length = 50)
    private String category; // 反馈分类：bug, feature, suggestion, other

    @Column(length = 100)
    private String name; // submitter name

    @Column(length = 200)
    private String email; // submitter email

    @Column(name = "client_user_id", length = 100)
    private String clientUserId; // client-provided user id (optional)

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "device_type", length = 100)
    private String deviceType;

    @Column(length = 200)
    private String browser;

    @Column(length = 200)
    private String os;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "client_timestamp")
    private String clientTimestamp; // keep as string to avoid parsing issues from client

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackStatus status = FeedbackStatus.PENDING; // 处理状态

    @Column(length = 500)
    private String adminReply; // 管理员回复

    @Column(nullable = false)
    private Boolean emailSent = false; // 是否已发送邮件

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum FeedbackStatus {
        PENDING,    // 待处理
        PROCESSING, // 处理中
        RESOLVED,   // 已解决
        CLOSED      // 已关闭
    }
}
