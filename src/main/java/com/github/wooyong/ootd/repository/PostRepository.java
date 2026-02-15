package com.github.wooyong.ootd.repository;

import com.github.wooyong.ootd.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 게시글 기본 CRUD + 커스텀 피드 조회를 제공하는 저장소입니다.
 */
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
