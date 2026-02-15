package com.github.wooyong.ootd.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 컨트롤러 메서드 파라미터에 현재 인증된 {@code User} 엔티티를 주입하기 위한 마커 어노테이션입니다.
 * {@link com.github.wooyong.ootd.auth.LoginUserArgumentResolver}가 실제 값을 해석합니다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}
