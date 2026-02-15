package com.github.wooyong.ootd.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * JWT 발급/파싱 동작 검증 테스트입니다.
 */
class JwtTokenProviderTest {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test-test-test-test-test-test-test-test-secret-key-123456",
            1800,
            1209600
    );

    @Test
    void accessToken_createAndParse() {
        String token = jwtTokenProvider.createAccessToken(1L);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.isAccessToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(1L);
    }

    @Test
    void refreshToken_createAndParse() {
        String token = jwtTokenProvider.createRefreshToken(2L);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(2L);
    }
}
