package com.commontools.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FrontErrorRequest {
    @NotBlank(message = "userId不能为空")
    @Size(max = 100, message = "userId长度不能超过100")
    private String userId;

    @Size(max = 500, message = "errorMessage长度不能超过500")
    private String errorMessage;
}

