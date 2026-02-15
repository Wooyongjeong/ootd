package com.github.wooyong.ootd.repository;

import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.domain.WeatherType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * QueryDSL 기반 게시글 피드 조회 커스텀 저장소 인터페이스입니다.
 */
public interface PostRepositoryCustom {
    /**
     * 날씨/지역 조건 기반 피드를 페이지 단위로 조회합니다.
     */
    Page<Post> findFeed(WeatherType weatherType, String region, Pageable pageable);
}
