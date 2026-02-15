package com.github.wooyong.ootd.repository;

import com.github.wooyong.ootd.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 댓글 저장소입니다.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 최신 댓글 최대 50건을 조회합니다.
     */
    List<Comment> findTop50ByPostIdOrderByCreatedAtDesc(Long postId);
}
