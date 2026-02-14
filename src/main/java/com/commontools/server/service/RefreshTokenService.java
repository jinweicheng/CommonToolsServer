package com.commontools.server.service;

import com.commontools.server.model.RefreshToken;
import com.commontools.server.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import com.commontools.server.exception.AppException;
import com.commontools.server.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${auth.refresh-expiration-days:30}")
    private int refreshExpirationDays;

    private final SecureRandom secureRandom = new SecureRandom();

    public String createRefreshToken(Long userId) {
        byte[] random = new byte[64];
        secureRandom.nextBytes(random);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);

        String hashed = hash(token);

        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setToken(hashed);
        rt.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        rt.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(refreshExpirationDays));

        refreshTokenRepository.save(rt);
        return token;
    }

    public Optional<RefreshToken> findByPlainToken(String token) {
        String hashed = hash(token);
        return refreshTokenRepository.findByToken(hashed);
    }

    public void deleteByPlainToken(String token) {
        String hashed = hash(token);
        refreshTokenRepository.deleteByToken(hashed);
    }

    @Transactional
    public void revokeByPlainToken(String token) {
        try {
            String hashed = hash(token);
            RefreshToken rt = refreshTokenRepository.findByToken(hashed)
                    .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID, "刷新令牌不存在"));
            refreshTokenRepository.delete(rt);
        } catch (DataAccessException dae) {
            throw new AppException(ErrorCode.LOGOUT_FAILED, "刷新令牌删除失败");
        } catch (AppException ae) {
            throw ae;
        } catch (Exception ex) {
            throw new AppException(ErrorCode.SERVER_ERROR, "服务器错误: " + ex.getMessage());
        }
    }

    private String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
