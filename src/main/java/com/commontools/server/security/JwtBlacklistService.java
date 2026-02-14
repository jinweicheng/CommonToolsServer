package com.commontools.server.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtBlacklistService {
    private final StringRedisTemplate redisTemplate;
    private final Map<String, Long> localBlacklist = new ConcurrentHashMap<>();

    public JwtBlacklistService(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    public void blacklist(String jti, long ttlSeconds) {
        if (jti == null || jti.isEmpty()) return;
        String key = keyFor(jti);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(ttlSeconds));
            return;
        }
        localBlacklist.put(key, System.currentTimeMillis() + ttlSeconds * 1000);
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) return false;
        String key = keyFor(jti);
        if (redisTemplate != null) {
            String v = redisTemplate.opsForValue().get(key);
            return v != null;
        }
        Long expireAt = localBlacklist.get(key);
        if (expireAt == null) {
            return false;
        }
        if (expireAt <= System.currentTimeMillis()) {
            localBlacklist.remove(key);
            return false;
        }
        return true;
    }

    private String keyFor(String jti) {
        return "jwt:blacklist:" + jti;
    }
}
