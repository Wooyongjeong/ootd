package com.github.wooyong.ootd.controller;

import com.github.wooyong.ootd.auth.LoginUser;
import com.github.wooyong.ootd.common.ApiResponse;
import com.github.wooyong.ootd.common.ResponseCode;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.dto.auth.AccessTokenResponse;
import com.github.wooyong.ootd.dto.auth.KakaoLoginRequest;
import com.github.wooyong.ootd.dto.auth.RefreshTokenRequest;
import com.github.wooyong.ootd.dto.auth.TokenResponse;
import com.github.wooyong.ootd.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication API controller.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login or sign up with Kakao authorization code and issue JWT tokens.
     */
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        ResponseCode.CREATED,
                        authService.loginWithKakao(request.authorizationCode(), request.redirectUri())
                ));
    }

    /**
     * Reissue access and refresh tokens.
     */
    @PostMapping("/refresh")
    public ApiResponse<AccessTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(ResponseCode.OK, authService.refresh(request.refreshToken()));
    }

    /**
     * Logout current user by deleting refresh token.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@LoginUser User user) {
        authService.logout(user.getId());
        return ApiResponse.success(ResponseCode.OK);
    }
}

