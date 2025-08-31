package com.promiseservice.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 카카오 알림톡 포트 구현체
 * 이유: 카카오 알림톡 서비스를 NotificationPort 인터페이스에 맞춰 구현하여 
 * 다른 알림 채널과 함께 대체발송 체계에서 사용할 수 있도록 함
 * 
 * 주의사항: 실제 운영 시 필요한 사전 작업
 * - 카카오 비즈니스 계정 개설
 * - 알림톡 템플릿 등록 및 승인
 * - OAuth2 토큰 발급 및 관리
 * - 발신프로필 등록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlimtalkPort implements NotificationPort {

    private final RestTemplate restTemplate;

    // 카카오 알림톡 서비스 URL
    @Value("${alimtalk.base-url:http://localhost:8085}")
    private String alimtalkBaseUrl;

    // 카카오 알림톡 API 경로
    @Value("${alimtalk.api.send:/api/alimtalk/send}")
    private String alimtalkSendPath;

    // OAuth2 토큰 (실제로는 토큰 관리 서비스에서 동적으로 가져와야 함)
    @Value("${alimtalk.oauth.token:demo-oauth-token}")
    private String oauthToken;

    // 발신프로필 키
    @Value("${alimtalk.profile.key:demo-profile-key}")
    private String profileKey;

    @Override
    public SendResult send(String to, String text) {
        // 알림톡은 템플릿 기반이므로 일반 텍스트 전송은 지원하지 않음
        // 이유: 카카오 알림톡은 사전 승인된 템플릿만 사용 가능
        log.warn("알림톡은 템플릿 기반 전송만 지원합니다. sendTemplate() 메서드를 사용하세요.");
        
        return new SendResult(
            false,
            "알림톡은 템플릿 기반 전송만 지원",
            "ALIMTALK_TEMPLATE_REQUIRED",
            List.of(),
            List.of(to)
        );
    }

    @Override
    public SendResult sendTemplate(String to, String templateCode, Map<String, String> variables) {
        try {
            // 알림톡 전송 요청 데이터 생성
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("to", to);
            requestData.put("templateCode", templateCode);
            requestData.put("variables", variables);
            requestData.put("profileKey", profileKey);

            // HTTP 헤더 설정 (OAuth2 토큰 포함)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(oauthToken);

            // HTTP 요청 엔티티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

            // 알림톡 서비스에 요청 전송
            String url = alimtalkBaseUrl + alimtalkSendPath;
            Map<String, Object> response = restTemplate.postForObject(url, requestEntity, Map.class);

            // 응답 처리
            if (response != null && "success".equals(response.get("status"))) {
                log.info("알림톡 전송 성공 - 수신자: {}, 템플릿: {}", to, templateCode);
                return new SendResult(
                    true,
                    "알림톡 전송 성공",
                    null,
                    List.of(to),
                    List.of()
                );
            } else {
                String errorCode = response != null ? (String) response.get("errorCode") : "UNKNOWN_ERROR";
                String errorMessage = response != null ? (String) response.get("message") : "알림톡 전송 실패";
                
                log.warn("알림톡 전송 실패 - 수신자: {}, 템플릿: {}, 에러: {}", to, templateCode, errorMessage);
                return new SendResult(
                    false,
                    errorMessage,
                    errorCode,
                    List.of(),
                    List.of(to)
                );
            }

        } catch (Exception e) {
            log.error("알림톡 전송 중 예외 발생 - 수신자: {}, 템플릿: {}, 에러: {}", to, templateCode, e.getMessage());
            
            // 예외 유형에 따른 에러 코드 결정
            String errorCode = "ALIMTALK_EXCEPTION";
            if (e.getMessage().contains("OAuth") || e.getMessage().contains("token")) {
                errorCode = "ALIMTALK_AUTH_FAILED";
            } else if (e.getMessage().contains("template")) {
                errorCode = "ALIMTALK_TEMPLATE_ERROR";
            }
            
            return new SendResult(
                false,
                "알림톡 전송 중 오류 발생: " + e.getMessage(),
                errorCode,
                List.of(),
                List.of(to)
            );
        }
    }

    @Override
    public String getChannelName() {
        return "ALIMTALK";
    }

    @Override
    public int getPriority() {
        return 1; // 다른 채널보다 높은 우선순위 (알림톡 우선 시도)
    }

    /**
     * 알림톡 서비스 상태 확인
     * 이유: 알림톡 서비스의 가용성을 확인하여 대체발송 여부를 결정하기 위해
     * 
     * @return 서비스 상태
     */
    public boolean checkAlimtalkServiceHealth() {
        try {
            String url = alimtalkBaseUrl + "/health";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(oauthToken);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            boolean isHealthy = response != null && "UP".equals(response.get("status"));
            log.info("알림톡 서비스 상태 확인 - 상태: {}", isHealthy ? "정상" : "오류");
            
            return isHealthy;
            
        } catch (Exception e) {
            log.error("알림톡 서비스 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }
}




