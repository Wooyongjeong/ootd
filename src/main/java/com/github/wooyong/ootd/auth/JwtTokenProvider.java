package com.github.wooyong.ootd.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 생성/검증을 담당하는 유틸리티 컴포넌트입니다.
 * Access/Refresh 토큰을 동일한 서명키로 발급하되, {@code token_type} 클레임으로 용도를 구분합니다.
 */
@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final SecretKey secretKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-seconds:1800}") long accessTokenExpirationSeconds,
            @Value("${app.jwt.refresh-token-expiration-seconds:1209600}") long refreshTokenExpirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    /**
     * Access 토큰을 발급합니다.
     */
    public String createAccessToken(Long userId) {
        return createToken(userId, ACCESS_TOKEN_TYPE, accessTokenExpirationSeconds);
    }

    /**
     * Refresh 토큰을 발급합니다.
     */
    public String createRefreshToken(Long userId) {
        return createToken(userId, REFRESH_TOKEN_TYPE, refreshTokenExpirationSeconds);
    }

    /**
     * 토큰 서명/만료를 검증합니다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 토큰 subject에서 사용자 식별자를 읽어옵니다.
     */
    public Long getUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Access 토큰 여부를 판별합니다.
     */
    public boolean isAccessToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    /**
     * Refresh 토큰 여부를 판별합니다.
     */
    public boolean isRefreshToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    /**
     * Refresh 토큰 만료 시간(초)을 반환합니다.
     */
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationSeconds;
    }

    /**
     * Access 토큰 만료 시간(초)을 반환합니다.
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }

    /**
     * 공통 토큰 생성 로직입니다.
     */
    private String createToken(Long userId, String tokenType, long expirationSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey)
                .compact();
    }
}
