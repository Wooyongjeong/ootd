package com.github.wooyong.ootd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * OOTD 백엔드 애플리케이션의 진입점입니다.
 * 스케줄러를 활성화하여 Redis Write-Back 작업이 주기적으로 실행되도록 합니다.
 */
@SpringBootApplication
@EnableScheduling
public class OotdApplication {

    /**
     * Spring Boot 애플리케이션을 시작합니다.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(OotdApplication.class, args);
    }

}
