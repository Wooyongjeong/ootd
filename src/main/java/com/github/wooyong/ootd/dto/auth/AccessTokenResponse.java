package com.github.wooyong.ootd.dto.auth;

/**
 * 토큰 재발급 응답 DTO입니다.
 */
public record AccessTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
