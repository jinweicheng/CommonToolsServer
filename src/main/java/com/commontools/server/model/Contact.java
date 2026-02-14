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
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Deprecated
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 姓名

    @Column(nullable = false, length = 255)
    private String email; // 邮箱

    @Column(nullable = false, length = 200)
    private String subject; // 主题

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message; // 消息内容

    @Column(nullable = false)
    private Boolean emailSent = false; // 是否已发送邮件

    @Column(length = 500)
    private String errorMessage; // 发送失败时的错误信息

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
