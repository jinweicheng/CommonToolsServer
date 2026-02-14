package com.commontools.server.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FrontErrorRequest {
    // accept both camelCase and snake_case from clients
    @JsonAlias({"user_id"})
    @Size(max = 100, message = "userId长度不能超过100")
    private String userId;

    @Size(max = 50, message = "module长度不能超过50")
    private String module;

    // action can be empty from some clients; we'll default in service
    @Size(max = 50, message = "action长度不能超过50")
    private String action;

    @Size(max = 200, message = "endpoint长度不能超过200")
    private String endpoint;

    @JsonAlias({"ip_address"})
    @Size(max = 50, message = "ipAddress长度不能超过50")
    private String ipAddress;

    @JsonAlias({"device_type"})
    @Size(max = 20, message = "deviceType长度不能超过20")
    private String deviceType;

    @Size(max = 50, message = "browser长度不能超过50")
    private String browser;

    @Size(max = 50, message = "os长度不能超过50")
    private String os;

    @JsonAlias({"error_message"})
    @Size(max = 5000, message = "errorMessage长度不能超过5000")
    private String errorMessage;
}

