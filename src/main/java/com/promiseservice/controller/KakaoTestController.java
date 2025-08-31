package com.promiseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 카카오톡 메시지 전송 테스트 컨트롤러
 * 이유: 개발 중 카카오톡 API 연동 상태를 빠르게 확인하기 위해
 */
@Slf4j
@RestController
@RequestMapping("/api/kakao/test")
public class KakaoTestController {

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    /**
     * 카카오톡 메시지 전송 요청 DTO
     * 이유: 액세스 토큰, 메시지 텍스트, 링크 URL을 안전하게 전달하기 위해
     */
    public record SendReq(String accessToken, String text, String linkUrl) {}

    /**
     * 카카오톡 "나와의 채팅"으로 테스트 메시지 전송 (JSON 방식)
     * 이유: curl, Postman JSON 요청과 프론트엔드 호환성을 위해 /send와 /send/custom 둘 다 지원
     * 
     * @param req 액세스 토큰과 메시지 텍스트
     * @return 항상 JSON 형태의 응답 (에러 포함)
     */
    @PostMapping(
        value = {"/send", "/send/custom"},
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> sendJson(@RequestBody SendReq req) { 
        return callKakao(req); 
    }

    /**
     * 카카오톡 "나와의 채팅"으로 테스트 메시지 전송 (Form 방식)
     * 이유: HTML 폼, Postman x-www-form-urlencoded 요청과 프론트엔드 호환성을 위해 /send와 /send/custom 둘 다 지원
     * 
     * @param accessToken 카카오 액세스 토큰
     * @param text 전송할 메시지 텍스트
     * @return 항상 JSON 형태의 응답 (에러 포함)
     */
    @PostMapping(
        value = {"/send", "/send/custom"},
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> sendForm(@RequestParam String accessToken,
                                      @RequestParam String text,
                                      @RequestParam(required = false) String linkUrl) {
        return callKakao(new SendReq(accessToken, text, linkUrl));
    }

    /**
     * 실제 카카오 API 호출 로직 (ObjectMapper 기반 안전한 JSON 생성)
     * 이유: 문자열 포맷팅 대신 ObjectMapper로 안전한 JSON을 생성하여 파싱 오류 방지
     * 
     * @param req 액세스 토큰, 메시지 텍스트, 링크 URL
     * @return 항상 JSON 형태의 응답 (성공/실패 모두)
     */
    private ResponseEntity<?> callKakao(SendReq req) {
        try {
            // ObjectMapper로 안전한 JSON 템플릿 생성
            // 이유: 문자열 포맷팅으로 인한 JSON 파싱 오류를 방지하고 정확한 구조 보장
            var templateObj = om.createObjectNode();
            templateObj.put("object_type", "text");
            templateObj.put("text", req.text());
            
            // 링크 URL이 있으면 추가
            // 이유: 사용자가 메시지에서 클릭할 수 있는 링크 제공
            if (req.linkUrl() != null && !req.linkUrl().isBlank()) {
                var linkObj = om.createObjectNode();
                linkObj.put("web_url", req.linkUrl());
                linkObj.put("mobile_web_url", req.linkUrl());
                templateObj.set("link", linkObj);
            } else {
                // 기본 링크 설정
                var linkObj = om.createObjectNode();
                linkObj.put("web_url", "https://developers.kakao.com");
                templateObj.set("link", linkObj);
            }
            
            String templateJson = om.writeValueAsString(templateObj);
            log.debug("생성된 template_object: {}", templateJson);

            // 카카오 API 헤더 설정
            // 이유: Bearer 토큰 인증과 form-urlencoded 전송 형식 지정
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(req.accessToken());
            h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Form 데이터로 전송
            // 이유: 카카오 API가 x-www-form-urlencoded 형식을 요구하기 때문
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("template_object", templateJson);

            // 카카오 "나와의 채팅" API 호출
            // 이유: 실제 카카오톡으로 메시지를 전송하여 연동 상태 확인
            var res = rest.postForEntity(
                "https://kapi.kakao.com/v2/api/talk/memo/default/send",
                new HttpEntity<>(form, h),
                String.class
            );

            // 성공 응답 처리
            // 이유: 카카오 응답을 안전하게 JSON 구조로 변환
            String kakaoBody = res.getBody();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("ok", true);
            payload.put("status", res.getStatusCode().value());
            
            if (kakaoBody != null && !kakaoBody.isBlank()) {
                try {
                    payload.put("kakao", om.readValue(kakaoBody, Map.class));
                } catch (Exception ignore) {
                    payload.put("kakaoRaw", kakaoBody);
                }
            }
            
            return ResponseEntity.status(res.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload);

        } catch (HttpStatusCodeException e) {
            // 카카오 에러 응답을 JSON 구조로 변환
            // 이유: 4xx/5xx 에러도 프론트엔드에서 안전하게 파싱할 수 있도록 JSON 통일
            String bodyText = e.getResponseBodyAsString();
            var headers = e.getResponseHeaders();
            String wwwAuth = headers != null ? headers.getFirst("WWW-Authenticate") : null;
            
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("ok", false);
            err.put("status", e.getStatusCode().value());
            
            if (wwwAuth != null) {
                err.put("wwwAuth", wwwAuth);
            }
            
            if (bodyText != null && !bodyText.isBlank()) {
                try {
                    err.put("error", om.readValue(bodyText, Map.class));
                } catch (Exception ignore) {
                    err.put("errorRaw", bodyText);
                }
            } else {
                err.put("error", "empty_response");
            }
            
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(err);
        } catch (Exception e) {
            // 기타 시스템 에러도 JSON으로 변환
            // 이유: JSON 생성 오류나 connection 문제도 일관된 응답 제공
            Map<String, Object> sysErr = new LinkedHashMap<>();
            sysErr.put("ok", false);
            sysErr.put("status", 502);
            sysErr.put("error", "upstream_error");
            sysErr.put("message", e.getMessage());
            
            return ResponseEntity.status(502)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(sysErr);
        }
    }
}

