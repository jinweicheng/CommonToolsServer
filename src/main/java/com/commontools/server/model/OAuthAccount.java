package com.commontools.server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "oauth_accounts")
@Data
public class OAuthAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 50, nullable = false)
    private String provider;

    @Column(length = 255, nullable = false)
    private String providerUserId;

    @Lob
    private String accessToken;

    @Lob
    private String refreshToken;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
}
