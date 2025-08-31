package com.promiseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PromiseService 메인 애플리케이션 클래스
 * 이유: Spring Boot 애플리케이션의 진입점을 제공하고,
 * JPA Auditing과 스케줄링 기능을 활성화하여 약속 알림 서비스의 핵심 기능들을 지원하기 위해
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class PromiseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PromiseServiceApplication.class, args);
    }
}
