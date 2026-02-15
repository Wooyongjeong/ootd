package com.github.wooyong.ootd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OOTD 게시물 엔티티입니다.
 * 피드 최적화를 위해 날씨/지역 기반 복합 인덱스를 포함합니다.
 */
@Getter
@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_posts_weather_region_created", columnList = "weatherType,region,createdAt,id"),
                @Index(name = "idx_posts_weather_region_like", columnList = "weatherType,region,likeCount,id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WeatherType weatherType;

    @Column(nullable = false, length = 100)
    private String region;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private long likeCount;

    @Column(nullable = false)
    private long viewCount;

    @Column(nullable = false)
    private long commentCount;

    @Version
    private Long version;

    private Post(User author, WeatherType weatherType, String region, String content, String imageUrl) {
        this.author = author;
        this.weatherType = weatherType;
        this.region = region;
        this.content = content;
        this.imageUrl = imageUrl;
        this.likeCount = 0L;
        this.viewCount = 0L;
        this.commentCount = 0L;
    }

    public static Post of(User author, WeatherType weatherType, String region, String content, String imageUrl) {
        return new Post(author, weatherType, region, content, imageUrl);
    }

    /**
     * Redis에 누적된 참여 지표 증분값을 DB 카운터에 반영합니다.
     * 음수 반영 시에도 카운터가 0 미만으로 내려가지 않도록 보호합니다.
     */
    public void applyEngagementDelta(long likeDelta, long viewDelta, long commentDelta) {
        this.likeCount = Math.max(0, this.likeCount + likeDelta);
        this.viewCount = Math.max(0, this.viewCount + viewDelta);
        this.commentCount = Math.max(0, this.commentCount + commentDelta);
    }
}
