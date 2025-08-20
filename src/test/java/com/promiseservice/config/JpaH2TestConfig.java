package com.promiseservice.config;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * JPA H2 테스트 전용 설정
 * 이유: 테스트 환경에서 추가 설정이 필요한 경우를 위한 설정 클래스
 */
@TestConfiguration
public class JpaH2TestConfig {
    // application-test.yml의 DataSource 설정을 사용
    // 이유: Bean 정의 오버라이드 오류를 방지하고 통일된 설정 사용
}
