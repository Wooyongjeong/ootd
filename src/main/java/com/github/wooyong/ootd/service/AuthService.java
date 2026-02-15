package com.github.wooyong.ootd.service;

import com.github.wooyong.ootd.auth.JwtTokenProvider;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.dto.auth.AccessTokenResponse;
import com.github.wooyong.ootd.dto.auth.TokenResponse;
import com.github.wooyong.ootd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
/**
 * 인증 비즈니스 로직 서비스입니다.
 * 카카오 로그인, JWT 발급/재발급, 로그아웃(리프레시 토큰 폐기)을 처리합니다.
 */
public class AuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * 카카오 인가 코드를 사용자 정보로 교환한 뒤 내부 사용자와 토큰을 발급합니다.
     */
    @Transactional
    public TokenResponse loginWithKakao(String authorizationCode, String redirectUri) {
        String kakaoAccessToken = kakaoOAuthClient.exchangeAuthorizationCode(authorizationCode, redirectUri);
        KakaoOAuthClient.KakaoProfile profile = kakaoOAuthClient.fetchProfile(kakaoAccessToken);

        User user = userRepository.findById(profile.kakaoUserId())
                .map(existing -> {
                    existing.updateNickname(profile.nickname());
                    return existing;
                })
                .orElseGet(() -> userRepository.save(User.of(profile.kakaoUserId(), profile.nickname())));

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenService.save(user.getId(), refreshToken, jwtTokenProvider.getRefreshTokenExpirationSeconds());

        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                user.getId(),
                user.getNickname()
        );
    }

    /**
     * Refresh 토큰을 검증하고 토큰 회전을 수행합니다.
     */
    @Transactional
    public AccessTokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        if (!refreshTokenService.matches(userId, refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token mismatch");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshTokenService.save(userId, newRefreshToken, jwtTokenProvider.getRefreshTokenExpirationSeconds());
        return new AccessTokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
    }

    /**
     * 사용자 로그아웃 시 Redis에 저장된 Refresh 토큰을 제거합니다.
     */
    public void logout(Long userId) {
        refreshTokenService.delete(userId);
    }
}
