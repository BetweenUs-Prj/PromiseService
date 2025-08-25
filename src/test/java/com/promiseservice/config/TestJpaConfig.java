package com.promiseservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 테스트용 JPA 설정
 * 이유: Repository 테스트에서 JPA Auditing과 엔티티 스캔을 활성화하기 위해
 */
@TestConfiguration
@EnableJpaAuditing
public class TestJpaConfig {
    // JPA Auditing 활성화를 위한 설정 클래스
    // 이유: @CreatedDate, @LastModifiedDate 어노테이션이 정상 동작하도록 하기 위해
}






