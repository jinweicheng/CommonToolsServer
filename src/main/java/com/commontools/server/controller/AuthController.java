package com.commontools.server.controller;

import com.commontools.server.dto.AuthResponse;
import com.commontools.server.dto.LoginRequest;
import com.commontools.server.dto.RegisterRequest;
import com.commontools.server.service.AuthService;
import com.commontools.server.service.RefreshTokenService;
import com.commontools.server.dto.RefreshRequest;
import com.commontools.server.dto.LogoutRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final com.commontools.server.util.JwtUtil jwtUtil;
    private final com.commontools.server.security.JwtBlacklistService jwtBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            return authService.refresh(request.getRefreshToken())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.badRequest().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        try {
            refreshTokenService.deleteByPlainToken(request.getRefreshToken());
            // also blacklist access token if provided
            String authHeader = null;
            try {
                authHeader = ((jakarta.servlet.http.HttpServletRequest)org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
                        .resolveReference(org.springframework.web.context.request.RequestAttributes.REFERENCE_REQUEST))
                        .getHeader("Authorization");
            } catch (Exception ignored) {}

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String jti = jwtUtil.getJtiFromToken(token);
                    java.util.Date exp = jwtUtil.getExpirationDateFromToken(token);
                    long ttl = Math.max(0, (exp.getTime() - System.currentTimeMillis()) / 1000);
                    if (ttl > 0) jwtBlacklistService.blacklist(jti, ttl);
                } catch (Exception ignored) {}
            }

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

