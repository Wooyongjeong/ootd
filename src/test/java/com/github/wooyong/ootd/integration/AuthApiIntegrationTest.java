package com.github.wooyong.ootd.integration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.wooyong.ootd.auth.JwtTokenProvider;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.dto.auth.AccessTokenResponse;
import com.github.wooyong.ootd.dto.auth.TokenResponse;
import com.github.wooyong.ootd.repository.UserRepository;
import com.github.wooyong.ootd.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 인증 API 통합 테스트입니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void kakaoLogin_returnsTokens() throws Exception {
        when(authService.loginWithKakao("code", "myapp://oauth"))
                .thenReturn(new TokenResponse("access", "refresh", "Bearer", 1800L, 1L, "tester"));

        mockMvc.perform(post("/api/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorizationCode":"code",
                                  "redirectUri":"myapp://oauth"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON-201"))
                .andExpect(jsonPath("$.data.accessToken").value("access"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh"));
    }

    @Test
    void refresh_returnsNewTokens() throws Exception {
        when(authService.refresh("refresh"))
                .thenReturn(new AccessTokenResponse("new-access", "new-refresh", "Bearer", 1800L));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON-200"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access"));
    }

    @Test
    void logout_requiresAuthenticationAndUsesLoginUser() throws Exception {
        User user = userRepository.save(User.of(1L, "tester"));
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON-200"));

        verify(authService).logout(1L);
    }
}
