package com.promiseservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * CORS 설정 클래스 (Spring Security 없이 사용)
 * 이유: UserService(8081)와 PromiseService(8080) 간의 크로스 도메인 요청을 허용하기 위해
 * Spring Security 의존성 없이도 CORS 정책을 설정하여 브라우저에서 안전하게 API 호출이 가능하도록 지원
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 전역 CORS 설정
     * 이유: 모든 컨트롤러 엔드포인트에 대해 일관된 CORS 정책을 적용하여
     * UserService에서 PromiseService API를 자유롭게 호출할 수 있도록 지원하기 위해
     * 
     * @param registry CORS 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로에 대해 CORS 허용
                // 허용할 오리진 설정
                // 이유: UserService와 자체 호출을 명시적으로 허용
                .allowedOrigins(
                    "http://localhost:8081",  // UserService
                    "http://localhost:8080"   // 자체 호출
                )
                // 허용할 HTTP 메서드 설정
                // 이유: REST API의 모든 표준 메서드와 OPTIONS(프리플라이트) 지원
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                // 허용할 헤더 설정
                // 이유: JWT 인증 헤더와 표준 HTTP 헤더, Kakao ID 헤더 허용
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "X-User-ID", "X-Kakao-Id")
                // 노출할 헤더 설정
                // 이유: 클라이언트에서 응답 헤더 정보를 읽을 수 있도록 지원
                .exposedHeaders("Location", "Content-Disposition")
                // 자격 증명 허용 설정
                // 이유: JWT는 헤더로 전송하므로 쿠키 인증은 사용하지 않음
                .allowCredentials(false)
                // 프리플라이트 캐시 시간
                // 이유: 브라우저가 프리플라이트 결과를 캐싱하여 성능 향상
                .maxAge(3600);
    }

    /**
     * CORS 설정 소스 빈 (필터 방식)
     * 이유: WebMvcConfigurer 방식과 함께 이중 보장하여
     * 어떤 상황에서도 CORS가 정상 작동하도록 지원하기 위해
     * 
     * @return CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 오리진 설정
        configuration.setAllowedOrigins(List.of(
            "http://localhost:8081",  // UserService
            "http://localhost:8080"   // 자체 호출
        ));
        
        // 허용할 메서드 설정
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // 허용할 헤더 설정
        configuration.setAllowedHeaders(List.of(
            "Authorization", "Content-Type", "X-Requested-With", "X-User-ID", 
            "X-Kakao-Id"
        ));
        
        // 노출할 헤더 설정
        configuration.setExposedHeaders(List.of(
            "Location", "Content-Disposition"
        ));
        
        // 자격 증명 설정
        configuration.setAllowCredentials(false);
        
        // 프리플라이트 캐시 시간
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * 보안 헤더 필터 (H2 콘솔 지원)
     * 이유: Spring Security 없이도 기본적인 보안 헤더를 설정하고
     * H2 콘솔이 iframe에서 정상 작동할 수 있도록 X-Frame-Options 설정
     * 
     * @return 보안 헤더 필터 등록 빈
     */
    @Bean
    public FilterRegistrationBean<Filter> securityHeadersFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                
                // H2 콘솔을 위한 X-Frame-Options 설정
                // 이유: H2 웹 콘솔이 iframe을 사용하므로 동일 오리진에서 프레임 허용
                httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
                
                // 기본 보안 헤더 설정
                // 이유: XSS 공격 방지와 콘텐츠 타입 스니핑 방지를 위한 기본 보안 강화
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");
                httpResponse.setHeader("Referrer-Policy", "no-referrer");
                httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
                
                chain.doFilter(request, response);
            }
        });
        
        // 모든 URL에 적용
        registrationBean.addUrlPatterns("/*");
        
        // CORS 필터보다 먼저 실행되도록 높은 우선순위 설정
        // 이유: 보안 헤더가 먼저 설정된 후 CORS 헤더가 추가되도록 순서 보장
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        
        registrationBean.setName("securityHeadersFilter");
        
        return registrationBean;
    }
}
