package com.promiseservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 테스트를 위한 설정 클래스
 * 이유: 테스트 실행 시 외부 서비스 의존성 설정을 오버라이드하여 독립적인 테스트 환경을 구성하기 위해
 */
@TestConfiguration
public class TestConfig {

    /**
     * 테스트 환경 설정값 통일 오버라이드 
     * 이유: 모든 테스트가 동일한 DB와 외부 서비스 설정을 사용하여 일관성을 보장하기 위해
     */
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        // 통일된 H2 테스트 데이터베이스 설정
        // 이유: 모든 테스트 클래스가 동일한 DB 인스턴스를 사용하여 충돌과 drop 오류를 방지하기 위해
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:unified_test_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
        
        // JPA 설정 통일
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.H2Dialect");
        r.add("spring.jpa.open-in-view", () -> "false");
        
        // UserService 관련 기본값
        r.add("userservice.base-url", () -> "http://localhost:8081");
        r.add("userservice.api.users", () -> "/api/users");
        r.add("userservice.api.profiles", () -> "/api/users/profiles");
        
        // NotificationService 관련 기본값
        r.add("notificationservice.base-url", () -> "http://localhost:8083");
        r.add("notificationservice.api.send", () -> "/api/notifications/send");
        
        // 로깅 레벨 설정
        r.add("logging.level.org.springframework.boot.context.properties", () -> "DEBUG");
    }
}
