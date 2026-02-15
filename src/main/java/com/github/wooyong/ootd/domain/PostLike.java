package com.github.wooyong.ootd.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시물 좋아요 이력 엔티티입니다.
 * (user_id, post_id) 유니크 제약으로 중복 좋아요를 방지합니다.
 */
@Getter
@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_likes_user_post", columnNames = {"user_id", "post_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public static PostLike of(User user, Post post) {
        return new PostLike(user, post);
    }
}
