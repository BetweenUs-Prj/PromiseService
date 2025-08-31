package com.promiseservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 서버 상태 확인용 헬스체크 컨트롤러
 * 이유: 서버가 정상 부팅되었는지 즉시 확인할 수 있는 간단한 엔드포인트 제공
 * HikariCP나 DB 연결 오류와 관계없이 Spring Boot 자체 상태만 확인
 */
@RestController
public class HealthController {

    /**
     * 서버 상태 확인 (핑)
     * 이유: 가장 기본적인 HTTP 요청 응답으로 서버 생존 여부 확인
     * 
     * @return 서버 상태 정보
     */
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "service", "PromiseService",
            "port", 8080,
            "message", "서버 정상 동작",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 상세 헬스체크
     * 이유: 서버 부팅 후 각 컴포넌트의 준비 상태 확인
     * 
     * @return 상세 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "PromiseService",
            "components", Map.of(
                "web", "UP",
                "cors", "UP", 
                "jwt-filter", "UP",
                "controllers", "UP"
            ),
            "endpoints", Map.of(
                "ping", "/ping",
                "health", "/health",
                "h2-console", "/h2-console",
                "dev-seed", "/api/dev/seed-appointment",
                "appointments", "/api/appointments/{id}/confirm"
            ),
            "timestamp", System.currentTimeMillis()
        ));
    }
}






