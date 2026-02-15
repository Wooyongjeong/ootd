package com.github.wooyong.ootd.dto;

/**
 * 인기 게시글 응답 DTO입니다.
 * 게시글 본문 정보와 Redis 점수를 함께 제공합니다.
 */
public record PopularPostResponse(
        PostResponse post,
        double score
) {
}
