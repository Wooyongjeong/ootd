package com.github.wooyong.ootd.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 서비스 사용자 엔티티입니다.
 * 카카오 사용자 식별자(id)를 내부 PK로 사용합니다.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    private User(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public static User of(Long id, String nickname) {
        return new User(id, nickname);
    }

    /**
     * 소셜 프로필 변경 시 닉네임을 동기화합니다.
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
