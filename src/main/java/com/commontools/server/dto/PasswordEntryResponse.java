package com.commontools.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordEntryResponse {
    private Long id;
    private String title;
    private String website;
    private String username;  // 解密后的用户名
    private String password;  // 解密后的密码
    private String notes;     // 解密后的备注
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 注意：这个DTO不包含敏感的用户信息，只有ID用于关联
    private Long userId;
}

