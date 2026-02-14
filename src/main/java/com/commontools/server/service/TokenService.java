package com.commontools.server.service;

import com.commontools.server.dto.AuthResponse;
import com.commontools.server.model.User;
import com.commontools.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration:86400000}")
    private Long accessTokenExpirationMs;

    public AuthResponse createAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        long expiresInSec = accessTokenExpirationMs / 1000;

        AuthResponse resp = new AuthResponse();
        resp.setToken(accessToken);
        resp.setType("Bearer");
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setRefreshToken(refreshToken);
        resp.setExpiresIn(expiresInSec);
        return resp;
    }
}
