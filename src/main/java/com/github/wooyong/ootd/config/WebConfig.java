package com.github.wooyong.ootd.config;

import com.github.wooyong.ootd.auth.LoginUserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 계층 확장 설정 클래스입니다.
 * 커스텀 메서드 파라미터 리졸버를 등록해 {@code @LoginUser}를 활성화합니다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginUserArgumentResolver loginUserArgumentResolver;

    /**
     * 컨트롤러 파라미터 해석기 목록에 LoginUser 리졸버를 추가합니다.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}
