package com.promiseservice.config;

import com.promiseservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 서블릿 필터 등록 설정
 * 이유: Spring Security 없이도 JWT 인증 필터를 등록하여
 * 모든 HTTP 요청에 대해 JWT 토큰 검증과 사용자 인증을 처리하기 위해
 * 필터 순서를 제어하여 CORS와 JWT 인증이 올바른 순서로 작동하도록 보장
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * JWT 인증 필터 등록
     * 이유: 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 사용자 정보를 요청 속성에 설정하기 위해
     * CORS 필터 다음에 실행되도록 순서를 조정하여 인증과 CORS가 충돌하지 않도록 보장
     * 
     * @return JWT 필터 등록 빈
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistrationBean() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = 
            new FilterRegistrationBean<>();
        
        // JWT 필터 설정
        registrationBean.setFilter(jwtAuthenticationFilter);
        
        // 모든 URL 패턴에 적용
        // 이유: API 엔드포인트뿐만 아니라 정적 리소스에서도 선택적으로 인증 정보를 사용할 수 있도록 지원
        registrationBean.addUrlPatterns("/*");
        
        // 필터 순서 설정 (CORS 다음에 실행)
        // 이유: CORS 헤더가 먼저 처리된 후 JWT 인증을 수행하여 프리플라이트 요청과 충돌 방지
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE - 1);
        
        // 필터 이름 설정
        registrationBean.setName("jwtAuthenticationFilter");
        
        return registrationBean;
    }
}






