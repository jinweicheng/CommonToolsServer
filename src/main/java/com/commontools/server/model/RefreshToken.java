package com.commontools.server.model;

import jakarta.persistence.*;
        import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Column(nullable = false, length = 500)
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}