package com.github.wooyong.ootd.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 카카오 로그인 요청 DTO입니다.
 */
public record KakaoLoginRequest(
        @NotBlank String authorizationCode,
        @NotBlank String redirectUri
) {
}
