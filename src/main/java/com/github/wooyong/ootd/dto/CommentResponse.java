package com.github.wooyong.ootd.dto;

import com.github.wooyong.ootd.domain.Comment;
import java.time.LocalDateTime;

/**
 * 댓글 응답 DTO입니다.
 */
public record CommentResponse(
        Long commentId,
        Long userId,
        String nickname,
        String content,
        LocalDateTime createdAt
) {

    /**
     * Comment 엔티티를 API 응답 객체로 매핑합니다.
     */
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
