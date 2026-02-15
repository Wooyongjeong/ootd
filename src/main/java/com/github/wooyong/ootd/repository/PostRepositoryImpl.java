package com.github.wooyong.ootd.repository;

import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.domain.QPost;
import com.github.wooyong.ootd.domain.WeatherType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;


/**
 * QueryDSL을 이용한 게시글 피드 조회 구현체입니다.
 * 조회 쿼리와 카운트 쿼리를 분리해 페이징 성능을 확보합니다.
 */
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 조건이 존재할 때만 where 절을 동적으로 추가합니다.
     */
    @Override
    public Page<Post> findFeed(WeatherType weatherType, String region, Pageable pageable) {
        QPost post = QPost.post;
        BooleanBuilder condition = new BooleanBuilder();

        if (weatherType != null) {
            condition.and(post.weatherType.eq(weatherType));
        }
        if (StringUtils.hasText(region)) {
            condition.and(post.region.eq(region));
        }

        List<Post> content = queryFactory
                .selectFrom(post)
                .where(condition)
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}
