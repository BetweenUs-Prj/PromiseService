package com.promiseservice.client;

import com.promiseservice.dto.TemplatePayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 카카오 API 클라이언트
 * 이유: 카카오 메시지 전송 API를 호출하여 실제 카카오톡 메시지를 전송하기 위해
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoClient {

    private final RestTemplate restTemplate;

    @Value("${kakao.api.base-url:https://kapi.kakao.com}")
    private String kakaoApiBaseUrl;

    @Value("${kakao.api.talk.memo:/v2/api/talk/memo/default/send}")
    private String kakaoTalkMemoPath;

        /**
     * 각 사용자의 "나와의 채팅"으로 메시지를 전송하는 메서드
     * 이유: 카카오톡에서 친구 관계가 없어도 각 사용자가 본인의 "나와의 채팅"으로 알림을 받을 수 있도록 하기 위해
     *
     * @param participantTokens 참여자별 액세스 토큰 맵 (userId -> accessToken)
     * @param templatePayload 메시지 템플릿 데이터
     * @return 전송 결과
     */
    public CompletableFuture<KakaoSendResult> sendToMemo(
            Map<Long, String> participantTokens,
            TemplatePayload templatePayload) {

        log.info("카카오 '나와의 채팅' 메시지 전송 시작 - 대상: {}명", participantTokens.size());

        return CompletableFuture.supplyAsync(() -> {
            try {
                int successCount = 0;
                int failureCount = 0;

                // 각 참여자의 "나와의 채팅"으로 개별 전송
                for (Map.Entry<Long, String> entry : participantTokens.entrySet()) {
                    Long userId = entry.getKey();
                    String accessToken = entry.getValue();
                    
                    try {
                        boolean success = sendToSingleMemo(accessToken, templatePayload, userId);
                        if (success) {
                            successCount++;
                        } else {
                            failureCount++;
                        }
                    } catch (Exception e) {
                        log.error("개별 메모 전송 실패 - 사용자 ID: {}, 오류: {}", userId, e.getMessage());
                        failureCount++;
                    }
                }

                log.info("카카오 메모 전송 완료 - 성공: {}, 실패: {}", successCount, failureCount);

                return new KakaoSendResult(
                    successCount > 0,
                    successCount,
                    participantTokens.size(),
                    String.format("전송 완료 - 성공: %d, 실패: %d", successCount, failureCount)
                );

            } catch (Exception e) {
                log.error("카카오 메모 전송 중 전체 오류", e);
                return new KakaoSendResult(
                    false,
                    0,
                    participantTokens.size(),
                    "전송 실패: " + e.getMessage()
                );
            }
        });
    }

        /**
     * 단일 사용자의 "나와의 채팅"으로 메시지를 전송하는 메서드
     * 이유: 각 사용자가 본인의 카카오톡 "나와의 채팅"으로 알림을 받도록 하기 위해
     *
     * @param accessToken 해당 사용자의 카카오 액세스 토큰
     * @param templatePayload 메시지 템플릿 데이터
     * @param userId 사용자 ID (로깅용)
     * @return 전송 성공 여부
     */
    private boolean sendToSingleMemo(String accessToken, TemplatePayload templatePayload, Long userId) {
        try {
            String apiUrl = kakaoApiBaseUrl + kakaoTalkMemoPath;

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 템플릿 객체 생성
            Map<String, Object> templateObject = createTemplateObject(templatePayload);
            String templateJson = convertToJson(templateObject);

            // Form 데이터 구성 ("나와의 채팅"은 receiver_uuids 불필요)
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("template_object", templateJson);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            // 카카오 API 호출
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("카카오 '나와의 채팅' 메시지 전송 성공 - 사용자 ID: {}", userId);
                return true;
            } else {
                log.error("카카오 '나와의 채팅' 메시지 전송 실패 - 사용자 ID: {}, 상태: {}", userId, response.getStatusCode());
                return false;
            }

        } catch (HttpStatusCodeException e) {
            log.error("카카오 메모 API 호출 오류 - 사용자 ID: {}, 상태: {}, 응답: {}",
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("메모 전송 중 예외 발생 - 사용자 ID: {}", userId, e);
            return false;
        }
    }

    /**
     * 템플릿 객체를 생성하는 메서드
     * 이유: 카카오 API에서 요구하는 템플릿 형식에 맞춰 객체를 구성하기 위해
     */
    private Map<String, Object> createTemplateObject(TemplatePayload payload) {
        Map<String, Object> template = new HashMap<>();
        template.put("object_type", "text");
        
        // 메시지 텍스트 구성
        StringBuilder text = new StringBuilder();
        text.append("🎉 ").append(payload.getInviter()).append("님의 약속 초대\n\n");
        
        if (payload.getTitle() != null && !payload.getTitle().trim().isEmpty()) {
            text.append("📋 ").append(payload.getTitle()).append("\n");
        }
        
        text.append("🕒 ").append(payload.getDate()).append("\n");
        text.append("📍 ").append(payload.getPlace()).append("\n\n");
        text.append("약속 준비 완료! 😊");
        
        template.put("text", text.toString());
        
        // 링크 정보 (선택사항)
        if (payload.getMeetingUrl() != null && !payload.getMeetingUrl().trim().isEmpty()) {
            Map<String, Object> link = new HashMap<>();
            link.put("web_url", payload.getMeetingUrl());
            link.put("mobile_web_url", payload.getMeetingUrl());
            template.put("link", link);
        }
        
        return template;
    }

    /**
     * 객체를 JSON 문자열로 변환하는 메서드
     * 이유: 카카오 API는 template_object를 JSON 형태로 받기 때문에
     */
    private String convertToJson(Map<String, Object> object) {
        try {
            // 간단한 JSON 변환 (ObjectMapper 없이)
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                if (!first) json.append(",");
                first = false;
                
                json.append("\"").append(entry.getKey()).append("\":");
                
                Object value = entry.getValue();
                if (value instanceof String) {
                    json.append("\"").append(escapeJson((String) value)).append("\"");
                } else if (value instanceof Map) {
                    json.append(convertToJson((Map<String, Object>) value));
                } else {
                    json.append("\"").append(value.toString()).append("\"");
                }
            }
            
            json.append("}");
            return json.toString();
            
        } catch (Exception e) {
            log.error("JSON 변환 중 오류", e);
            return "{}";
        }
    }

    /**
     * JSON 문자열 이스케이프 처리
     * 이유: JSON 특수문자를 올바르게 이스케이프하기 위해
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 카카오 메시지 전송 결과를 담는 클래스
     * 이유: 카카오 API 호출 결과를 구조화하여 관리하기 위해
     */
    @Getter
    public static class KakaoSendResult {
        private final boolean success;
        private final int sentCount;
        private final int totalCount;
        private final String message;

        public KakaoSendResult(boolean success, int sentCount, int totalCount, String message) {
            this.success = success;
            this.sentCount = sentCount;
            this.totalCount = totalCount;
            this.message = message;
        }

        /**
         * 부분 성공 여부를 확인하는 메서드
         * 이유: 일부는 성공하고 일부는 실패한 경우를 구분하기 위해
         */
        public boolean isPartialSuccess() {
            return success && sentCount > 0 && sentCount < totalCount;
        }

        /**
         * 완전 실패 여부를 확인하는 메서드
         * 이유: 모든 전송이 실패한 경우를 구분하기 위해
         */
        public boolean isCompleteFailure() {
            return !success || sentCount == 0;
        }
    }
}
