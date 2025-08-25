package com.promiseservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.promiseservice.service.UserService;

/**
 * PromiseService 애플리케이션 통합 테스트
 * 이유: 전체 애플리케이션 컨텍스트가 올바르게 로드되고 모든 빈들이 정상적으로 주입되는지 검증하기 위해
 */
@SpringBootTest
@ActiveProfiles("test")
class PromiseServiceApplicationTests {

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("스프링 컨텍스트 로드 테스트")
    // 테스트 이유: 애플리케이션의 모든 구성 요소가 올바르게 설정되고 의존성 주입이 정상적으로 이루어지는지 검증
    void should_LoadContext_When_ApplicationStarts() {
        // 스프링 컨텍스트가 성공적으로 로드되면 테스트 통과
        // 모든 @Component, @Service, @Repository, @Controller 빈들이 정상적으로 생성되는지 확인
    }

}






