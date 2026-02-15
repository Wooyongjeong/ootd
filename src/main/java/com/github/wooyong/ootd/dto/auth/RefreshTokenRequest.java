package com.github.wooyong.ootd.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Access 토큰 재발급 요청 DTO입니다.
 */
public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {
}
