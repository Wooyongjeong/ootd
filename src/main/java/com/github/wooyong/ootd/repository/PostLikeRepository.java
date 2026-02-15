package com.github.wooyong.ootd.repository;

import com.github.wooyong.ootd.domain.PostLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 게시글 좋아요 저장소입니다.
 */
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 특정 사용자의 특정 게시글 좋아요 존재 여부를 확인합니다.
     */
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    /**
     * 특정 사용자의 특정 게시글 좋아요 엔티티를 조회합니다.
     */
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);
}
