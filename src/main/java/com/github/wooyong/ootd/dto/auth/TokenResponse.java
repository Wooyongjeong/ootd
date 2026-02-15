package com.github.wooyong.ootd.dto.auth;

/**
 * 로그인 직후 반환되는 토큰/사용자 정보 응답 DTO입니다.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long userId,
        String nickname
) {
}
