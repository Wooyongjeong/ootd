package com.github.wooyong.ootd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.wooyong.ootd.auth.JwtTokenProvider;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.dto.auth.AccessTokenResponse;
import com.github.wooyong.ootd.dto.auth.TokenResponse;
import com.github.wooyong.ootd.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 인증 서비스 단위 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginWithKakao_success() {
        when(kakaoOAuthClient.exchangeAuthorizationCode("code", "http://localhost/callback"))
                .thenReturn("kakaoAccessToken");
        when(kakaoOAuthClient.fetchProfile("kakaoAccessToken"))
                .thenReturn(new KakaoOAuthClient.KakaoProfile(100L, "kakao-user"));
        when(userRepository.findById(100L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(User.of(100L, "kakao-user"));
        when(jwtTokenProvider.createAccessToken(100L)).thenReturn("access");
        when(jwtTokenProvider.createRefreshToken(100L)).thenReturn("refresh");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(1800L);
        when(jwtTokenProvider.getRefreshTokenExpirationSeconds()).thenReturn(1209600L);

        TokenResponse response = authService.loginWithKakao("code", "http://localhost/callback");

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.userId()).isEqualTo(100L);
        verify(refreshTokenService).save(100L, "refresh", 1209600L);
    }

    @Test
    void refresh_success() {
        when(jwtTokenProvider.validateToken("refresh")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("refresh")).thenReturn(true);
        when(jwtTokenProvider.getUserId("refresh")).thenReturn(1L);
        when(refreshTokenService.matches(1L, "refresh")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("new-access");
        when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("new-refresh");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(1800L);
        when(jwtTokenProvider.getRefreshTokenExpirationSeconds()).thenReturn(1209600L);

        AccessTokenResponse response = authService.refresh("refresh");

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenService).save(1L, "new-refresh", 1209600L);
    }

    @Test
    void refresh_invalidToken_throwsUnauthorized() {
        when(jwtTokenProvider.validateToken("invalid")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("invalid"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
