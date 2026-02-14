package com.commontools.server.service;

import com.commontools.server.dto.AuthResponse;
import com.commontools.server.dto.LoginRequest;
import com.commontools.server.dto.RegisterRequest;
import com.commontools.server.model.User;
import com.commontools.server.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());

        User savedUser = userService.register(user);
        return tokenService.createAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("账户不可用");
        }

        return tokenService.createAuthResponse(user);
    }

    public java.util.Optional<AuthResponse> refresh(String refreshToken) {
        java.util.Optional<com.commontools.server.model.RefreshToken> rtOpt = refreshTokenService.findByPlainToken(refreshToken);
        if (rtOpt.isEmpty()) return java.util.Optional.empty();
        com.commontools.server.model.RefreshToken rt = rtOpt.get();
        if (rt.getExpiresAt() != null && rt.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            refreshTokenService.deleteByPlainToken(refreshToken);
            return java.util.Optional.empty();
        }

        User user = userService.findById(rt.getUserId()).orElseThrow(() -> new RuntimeException("用户不存在"));

        // rotate: delete old and create new
        refreshTokenService.deleteByPlainToken(refreshToken);
        AuthResponse resp = tokenService.createAuthResponse(user);
        return java.util.Optional.of(resp);
    }
}

