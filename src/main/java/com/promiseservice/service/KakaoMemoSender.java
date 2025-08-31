package com.promiseservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 카카오톡 메모 발송 전용 컴포넌트
 * 이유: 카카오 "나에게 보내기" API를 직접 호출하여 간단하고 확실한 메시지 발송을 제공하기 위해
 * 복잡한 템플릿 빌더 없이도 텍스트 메시지를 빠르게 발송할 수 있도록 지원
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMemoSender {

    private final RestTemplate restTemplate;
    
    // 카카오 API 엔드포인트
    private static final String KAKAO_MEMO_API = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

    /**
     * 간단한 텍스트 메모를 카카오톡으로 발송
     * 이유: 사용자의 액세스 토큰을 사용하여 해당 사용자에게 직접 텍스트 메시지를 발송하기 위해
     * 최소한의 파라미터로 빠른 테스트와 실제 사용이 가능하도록 설계
     * 
     * @param accessToken 사용자의 카카오 액세스 토큰
     * @param text 발송할 텍스트 내용
     * @param url 클릭 시 이동할 URL
     * @return 발송 성공 여부
     */
    public boolean sendText(String accessToken, String text, String url) {
        
        log.info("카카오톡 메모 발송 시작 - 텍스트 길이: {}자", text != null ? text.length() : 0);
        
        try {
            // 템플릿 JSON 생성
            // 이유: 카카오톡 API에서 요구하는 템플릿 형식에 맞춰 JSON 문자열 생성
            String templateJson = createTextTemplate(text, url);
            log.debug("템플릿 JSON 생성 완료: {}", templateJson);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Form 데이터 구성
            // 이유: 카카오 API는 template_object를 form-urlencoded 형태로 전송받음
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("template_object", templateJson);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
            
            // 카카오 API 호출
            var response = restTemplate.postForEntity(KAKAO_MEMO_API, requestEntity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.info("카카오톡 메모 발송 성공 - 응답: {}", responseBody);
                return true;
                
            } else {
                log.error("카카오톡 메모 발송 실패 - 상태코드: {}, 응답: {}", 
                        response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // HTTP 오류 상황별 로깅
            handleKakaoApiError(e);
            return false;
            
        } catch (Exception e) {
            log.error("카카오톡 메모 발송 중 예외 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 약속 확정 알림용 메시지 발송
     * 이유: 약속 관련 정보를 포함한 구조화된 메시지를 발송하여
     * 사용자에게 약속 확정 소식을 명확히 전달하기 위해
     * 
     * @param accessToken 사용자의 카카오 액세스 토큰
     * @param title 약속 제목
     * @param startAt 약속 일시
     * @param place 약속 장소
     * @param detailUrl 약속 상세 URL
     * @return 발송 성공 여부
     */
    public boolean sendAppointmentConfirmed(String accessToken, String title, String startAt, String place, String detailUrl) {
        
        // 약속 확정 메시지 구성
        // 이유: 약속의 핵심 정보를 포함한 사용자 친화적인 메시지 생성
        StringBuilder message = new StringBuilder();
        message.append("🎉 약속이 확정되었습니다!\n\n");
        
        if (title != null && !title.trim().isEmpty()) {
            message.append("📋 ").append(title).append("\n");
        }
        
        if (startAt != null && !startAt.trim().isEmpty()) {
            message.append("🕒 ").append(startAt).append("\n");
        }
        
        if (place != null && !place.trim().isEmpty()) {
            message.append("📍 ").append(place).append("\n");
        }
        
        message.append("\n약속 준비 완료! 😊");
        
        String url = (detailUrl != null && !detailUrl.trim().isEmpty()) ? detailUrl : "https://example.com";
        
        log.info("약속 확정 알림 발송 - 제목: {}", title);
        return sendText(accessToken, message.toString(), url);
    }

    /**
     * 테스트용 간단 메시지 발송
     * 이유: 개발 및 테스트 환경에서 카카오톡 발송 기능을 빠르게 확인하기 위해
     * 
     * @param accessToken 사용자의 카카오 액세스 토큰
     * @return 발송 성공 여부
     */
    public boolean sendTestMessage(String accessToken) {
        String testMessage = "✅ 카카오톡 연동 테스트\n" +
                           "메시지가 정상적으로 전송되었습니다!\n" +
                           "시간: " + java.time.LocalDateTime.now().toString();
        
        log.info("테스트 메시지 발송 시도");
        return sendText(accessToken, testMessage, "https://example.com");
    }

    /**
     * 텍스트 템플릿 JSON 생성
     * 이유: 카카오톡 API에서 요구하는 템플릿 형식에 맞춰 JSON 문자열을 생성하기 위해
     * JSON 이스케이핑을 포함하여 안전한 템플릿 생성
     * 
     * @param text 메시지 텍스트
     * @param url 링크 URL
     * @return JSON 템플릿 문자열
     */
    private String createTextTemplate(String text, String url) {
        // JSON 안전 처리
        String safeText = jsonEscape(text != null ? text : "메시지");
        String safeUrl = jsonEscape(url != null ? url : "https://example.com");
        
        return String.format("""
            {
              "object_type": "text",
              "text": %s,
              "link": {
                "web_url": %s,
                "mobile_web_url": %s
              }
            }
            """, safeText, safeUrl, safeUrl);
    }

    /**
     * JSON 문자열 이스케이핑
     * 이유: JSON 형식에서 특수 문자로 인한 오류를 방지하기 위해
     * 
     * @param input 원본 문자열
     * @return 이스케이핑된 JSON 문자열
     */
    private String jsonEscape(String input) {
        if (input == null) {
            return "\"\"";
        }
        
        String escaped = input
                .replace("\\", "\\\\")  // 백슬래시
                .replace("\"", "\\\"")  // 따옴표
                .replace("\n", "\\n")   // 새 줄
                .replace("\r", "\\r")   // 캐리지 리턴
                .replace("\t", "\\t");  // 탭
        
        return "\"" + escaped + "\"";
    }

    /**
     * 카카오 API 오류 처리
     * 이유: 카카오 API 오류 상황을 분석하여 구체적인 로그를 남기고 문제 해결에 도움을 주기 위해
     * 
     * @param e HTTP 상태 코드 예외
     */
    private void handleKakaoApiError(org.springframework.web.client.HttpStatusCodeException e) {
        int statusCode = e.getStatusCode().value();
        String responseBody = e.getResponseBodyAsString();
        
        log.error("카카오 API 오류 발생:");
        log.error("- 상태코드: {}", statusCode);
        log.error("- 응답 내용: {}", responseBody);
        
        switch (statusCode) {
            case 401:
                log.error("🚨 인증 오류: 액세스 토큰이 만료되었거나 유효하지 않습니다.");
                log.error("   해결방법: 토큰을 새로 발급받거나 리프레시하세요.");
                break;
                
            case 403:
                if (responseBody != null && (responseBody.contains("insufficient_scope") || responseBody.contains("-5"))) {
                    log.error("🚨 권한 오류: talk_message 스코프에 동의하지 않았습니다.");
                    log.error("   해결방법: 카카오 로그인 시 talk_message 권한에 다시 동의하세요.");
                } else {
                    log.error("🚨 접근 권한 오류: 카카오 API 접근이 거부되었습니다.");
                }
                break;
                
            case 400:
                log.error("🚨 요청 오류: 요청 형식이 올바르지 않습니다.");
                log.error("   확인사항: template_object JSON 형식을 점검하세요.");
                break;
                
            default:
                log.error("🚨 기타 오류: HTTP {} - {}", statusCode, responseBody);
        }
    }
}





