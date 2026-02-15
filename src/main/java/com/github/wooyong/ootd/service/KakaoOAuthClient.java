package com.github.wooyong.ootd.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;


/**
 * 카카오 OAuth 서버와 통신하는 HTTP 클라이언트입니다.
 */
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final RestClient restClient = RestClient.builder().build();

    @Value("${app.oauth.kakao.client-id}")
    private String clientId;

    @Value("${app.oauth.kakao.client-secret:}")
    private String clientSecret;

    /**
     * 인가 코드를 카카오 액세스 토큰으로 교환합니다.
     */
    public String exchangeAuthorizationCode(String authorizationCode, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        if (!clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }
        form.add("redirect_uri", redirectUri);
        form.add("code", authorizationCode);

        JsonNode tokenResponse = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);

        if (tokenResponse == null || tokenResponse.get("access_token") == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Failed to get Kakao access token");
        }

        return tokenResponse.get("access_token").asText();
    }

    /**
     * 카카오 액세스 토큰으로 사용자 프로필을 조회합니다.
     */
    public KakaoProfile fetchProfile(String kakaoAccessToken) {
        JsonNode userInfo = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(JsonNode.class);

        if (userInfo == null || userInfo.get("id") == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Failed to get Kakao user profile");
        }

        long kakaoUserId = userInfo.get("id").asLong();
        String nickname = userInfo.path("properties").path("nickname").asText("kakao-" + kakaoUserId);
        return new KakaoProfile(kakaoUserId, nickname);
    }

    /**
     * 카카오 사용자 최소 프로필 정보입니다.
     */
    public record KakaoProfile(Long kakaoUserId, String nickname) {
    }
}
