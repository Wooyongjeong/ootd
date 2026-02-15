package com.github.wooyong.ootd.repository;

import com.github.wooyong.ootd.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 저장소입니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
