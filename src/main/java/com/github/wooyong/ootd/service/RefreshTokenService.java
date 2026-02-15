package com.github.wooyong.ootd.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Refresh 토큰의 저장/검증/삭제를 Redis로 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "ootd:auth:refresh:";
    private final StringRedisTemplate redisTemplate;

    /**
     * 사용자별 Refresh 토큰을 TTL과 함께 저장합니다.
     */
    public void save(Long userId, String refreshToken, long expirationSeconds) {
        redisTemplate.opsForValue().set(key(userId), refreshToken, Duration.ofSeconds(expirationSeconds));
    }

    /**
     * 전달된 토큰이 Redis에 저장된 최신 토큰과 일치하는지 확인합니다.
     */
    public boolean matches(Long userId, String refreshToken) {
        String saved = redisTemplate.opsForValue().get(key(userId));
        return refreshToken.equals(saved);
    }

    /**
     * 사용자 Refresh 토큰을 삭제합니다.
     */
    public void delete(Long userId) {
        redisTemplate.delete(key(userId));
    }

    /**
     * Redis 키 생성 규칙입니다.
     */
    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
