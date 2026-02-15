package com.github.wooyong.ootd.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wooyong.ootd.auth.JwtAuthenticationFilter;
import com.github.wooyong.ootd.common.ApiResponse;
import com.github.wooyong.ootd.common.ResponseCode;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for stateless JWT authentication.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    /**
     * Configure security filter chain and endpoint authorization rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeErrorResponse(response, ResponseCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeErrorResponse(response, ResponseCode.FORBIDDEN))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/kakao/login", "/api/auth/refresh", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/feed", "/api/posts/popular", "/api/posts/*/comments")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/views").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeErrorResponse(jakarta.servlet.http.HttpServletResponse response, ResponseCode responseCode)
            throws IOException {
        response.setStatus(responseCode.httpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error(responseCode, responseCode.defaultMessage())
        ));
    }
}

