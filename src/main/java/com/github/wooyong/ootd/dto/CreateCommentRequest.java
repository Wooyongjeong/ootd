package com.github.wooyong.ootd.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 댓글 작성 요청 DTO입니다.
 */
public record CreateCommentRequest(
        @NotBlank String content
) {
}
