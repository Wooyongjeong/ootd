package com.github.wooyong.ootd.dto;

import com.github.wooyong.ootd.domain.WeatherType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 게시글 생성 요청 DTO입니다.
 */
public record CreatePostRequest(
        @NotNull WeatherType weatherType,
        @NotBlank String region,
        @NotBlank String content,
        String imageUrl
) {
}
