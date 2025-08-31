package com.promiseservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 카카오 API 진단 컨트롤러
 * 이유: 카카오 토큰의 유효성과 사용자 ID를 확인하여 Provider ID 매핑 문제를 진단하기 위해
 * /v2/user/me와 /v1/user/access_token_info API를 호출하여 토큰 정보 검증
 */
@Slf4j
@RestController
@RequestMapping("/api/debug/kakao")
@RequiredArgsConstructor
public class KakaoDiagnosticController {

    private final RestTemplate restTemplate;
    
    @Value("${kakao.api.base-url:https://kapi.kakao.com}")
    private String kakaoApiBaseUrl;

    /**
     * 카카오 사용자 정보 조회
     * 이유: 현재 토큰으로 /v2/user/me를 호출하여 실제 카카오 사용자 ID를 확인하기 위해
     * 
     * @return 카카오 사용자 정보
     */
    @GetMapping("/user/me")
    public ResponseEntity<Map<String, Object>> getKakaoUserInfo() {
        log.info("=== 🔍 카카오 사용자 정보 조회 시작 ===");
        
        try {
            // 환경변수에서 토큰 가져오기
            String accessToken = System.getenv("KAKAO_TEST_ACCESS_TOKEN");
            if (accessToken == null || accessToken.isEmpty()) {
                log.error("카카오 액세스 토큰이 설정되지 않았습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "카카오 액세스 토큰이 설정되지 않았습니다"));
            }
            
            // 카카오 API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            var request = new org.springframework.http.HttpEntity<>(headers);
            
            var response = restTemplate.exchange(
                kakaoApiBaseUrl + "/v2/user/me",
                org.springframework.http.HttpMethod.GET,
                request,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> userInfo = response.getBody();
                log.info("카카오 사용자 정보 조회 성공: {}", userInfo);
                
                Map<String, Object> result = Map.of(
                    "message", "카카오 사용자 정보 조회 성공",
                    "userInfo", userInfo,
                    "timestamp", System.currentTimeMillis()
                );
                
                return ResponseEntity.ok(result);
            } else {
                log.error("카카오 사용자 정보 조회 실패 - HTTP 상태: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "카카오 사용자 정보 조회 실패", "status", response.getStatusCode()));
            }
            
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "카카오 사용자 정보 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 카카오 액세스 토큰 정보 조회
     * 이유: 현재 토큰의 appId와 scope 정보를 확인하여 올바른 앱에서 발급된 토큰인지 검증하기 위해
     * 
     * @return 카카오 액세스 토큰 정보
     */
    @GetMapping("/user/access-token-info")
    public ResponseEntity<Map<String, Object>> getKakaoAccessTokenInfo() {
        log.info("=== 🔍 카카오 액세스 토큰 정보 조회 시작 ===");
        
        try {
            // 환경변수에서 토큰 가져오기
            String accessToken = System.getenv("KAKAO_TEST_ACCESS_TOKEN");
            if (accessToken == null || accessToken.isEmpty()) {
                log.error("카카오 액세스 토큰이 설정되지 않았습니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "카카오 액세스 토큰이 설정되지 않았습니다"));
            }
            
            // 카카오 API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            var request = new org.springframework.http.HttpEntity<>(headers);
            
            var response = restTemplate.exchange(
                kakaoApiBaseUrl + "/v1/user/access_token_info",
                org.springframework.http.HttpMethod.GET,
                request,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> tokenInfo = response.getBody();
                log.info("카카오 액세스 토큰 정보 조회 성공: {}", tokenInfo);
                
                Map<String, Object> result = Map.of(
                    "message", "카카오 액세스 토큰 정보 조회 성공",
                    "tokenInfo", tokenInfo,
                    "timestamp", System.currentTimeMillis()
                );
                
                return ResponseEntity.ok(result);
            } else {
                log.error("카카오 액세스 토큰 정보 조회 실패 - HTTP 상태: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "카카오 액세스 토큰 정보 조회 실패", "status", response.getStatusCode()));
            }
            
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 정보 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "카카오 액세스 토큰 정보 조회 중 오류 발생: " + e.getMessage()));
        }
    }
}
