package com.commontools.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordEntryRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String website;
    
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String notes;
    private String category;
}

