package com.github.wooyong.ootd.dto;

import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.domain.WeatherType;
import java.time.LocalDateTime;

/**
 * 게시글 응답 DTO입니다.
 */
public record PostResponse(
        Long postId,
        Long authorId,
        String authorNickname,
        WeatherType weatherType,
        String region,
        String content,
        String imageUrl,
        long likeCount,
        long viewCount,
        long commentCount,
        LocalDateTime createdAt
) {

    /**
     * Post 엔티티를 API 응답 객체로 매핑합니다.
     */
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getWeatherType(),
                post.getRegion(),
                post.getContent(),
                post.getImageUrl(),
                post.getLikeCount(),
                post.getViewCount(),
                post.getCommentCount(),
                post.getCreatedAt()
        );
    }
}
