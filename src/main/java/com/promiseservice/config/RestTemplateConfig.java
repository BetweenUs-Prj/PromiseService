package com.promiseservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 설정 클래스
 * 이유: 카카오 API 호출을 위한 RestTemplate을 설정하고 타임아웃 등의 공통 기능을 제공하기 위해
 */
@Configuration
@Slf4j
public class RestTemplateConfig {

    /**
     * 카카오 API 호출용 RestTemplate 빈 생성
     * 이유: HTTP 클라이언트 설정을 중앙화하고 타임아웃 등의 공통 기능을 제공하기 위해
     * Spring Boot 3.x의 최신 ClientHttpRequestFactory 설정 방식 사용
     * 
     * @param builder RestTemplateBuilder
     * @return 설정된 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        try {
            // Spring Boot 3.x 최신 방식: ClientHttpRequestFactory 직접 설정
            RestTemplate restTemplate = builder
                    .requestFactory(() -> {
                        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
                        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());   // 5초
                        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());     // 30초
                        return factory;
                    })
                    .build();
            
            log.info("RestTemplate 설정 완료 (최신 방식) - Connect timeout: 5s, Read timeout: 30s");
            return restTemplate;
            
            
        } catch (Exception e) {
            // 최신 방식 실패 시 기본 RestTemplate 반환
            log.warn("최신 RestTemplate 설정 실패, 기본 설정 사용: {}", e.getMessage());
            return builder.build();
        }
    }
}