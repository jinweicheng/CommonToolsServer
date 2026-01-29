package com.commontools.server.controller;

import com.commontools.server.dto.ApiResponse;
import com.commontools.server.dto.FrontErrorRequest;
import com.commontools.server.model.FrontError;
import com.commontools.server.service.FrontErrorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/front-errors")
@RequiredArgsConstructor
public class FrontErrorController {
    private final FrontErrorService frontErrorService;

    /**
     * 前端错误上报
     * POST /api/front-errors
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> report(@Valid @RequestBody FrontErrorRequest request) {
        try {
            FrontError saved = frontErrorService.record(request);
            return ResponseEntity.ok(ApiResponse.success("Recorded", saved.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Internal server error", e.getMessage()));
        }
    }
}

