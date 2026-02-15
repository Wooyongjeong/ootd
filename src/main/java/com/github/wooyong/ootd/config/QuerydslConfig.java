package com.github.wooyong.ootd.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 사용을 위한 {@link JPAQueryFactory} 빈 구성 클래스입니다.
 */
@Configuration
public class QuerydslConfig {

    /**
     * JPA EntityManager를 기반으로 QueryDSL 팩토리를 생성합니다.
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
