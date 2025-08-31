package com.promiseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promiseservice.dto.KakaoNotifyResponse;
import com.promiseservice.dto.NotifyKakaoRequest;
import com.promiseservice.service.KakaoNotifyService;
import com.promiseservice.service.NotificationService;

import com.promiseservice.service.notification.UnifiedNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NotificationController의 카카오톡 알림 기능 테스트
 * 이유: 카카오톡 알림 API 엔드포인트의 정상 동작과 예외 처리를 검증하기 위해
 */
@WebMvcTest(NotificationController.class)
class NotificationControllerKakaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KakaoNotifyService kakaoNotifyService;

    @MockBean
    private NotificationService notificationService;



    @MockBean
    private UnifiedNotificationService unifiedNotificationService;

    @Test
    void sendKakaoNotification_정상요청시_200응답() throws Exception {
        // Given
        Long currentUserId = 1L;
        NotifyKakaoRequest request = new NotifyKakaoRequest(123L, List.of(2L, 3L));
        
        KakaoNotifyResponse mockResponse = new KakaoNotifyResponse(2, 2);
        when(kakaoNotifyService.sendKakaoNotification(eq(currentUserId), eq(123L), anyList()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/notifications/kakao")
                        .header("X-User-ID", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sentCount").value(2))
                .andExpect(jsonPath("$.totalCount").value(2));
    }

    @Test
    void sendKakaoNotification_동의없음시_409응답() throws Exception {
        // Given
        Long currentUserId = 1L;
        NotifyKakaoRequest request = new NotifyKakaoRequest(123L, List.of(2L, 3L));
        
        when(kakaoNotifyService.sendKakaoNotification(eq(currentUserId), eq(123L), anyList()))
                .thenThrow(new IllegalStateException("카카오 기능 사용에 동의하지 않았습니다"));

        // When & Then
        mockMvc.perform(post("/api/notifications/kakao")
                        .header("X-User-ID", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("CONSENT_REQUIRED"))
                .andExpect(jsonPath("$.guide").exists());
    }

    @Test
    void sendKakaoNotification_잘못된파라미터시_400응답() throws Exception {
        // Given
        Long currentUserId = 1L;
        NotifyKakaoRequest request = new NotifyKakaoRequest(123L, List.of(2L, 3L));
        
        when(kakaoNotifyService.sendKakaoNotification(eq(currentUserId), eq(123L), anyList()))
                .thenThrow(new IllegalArgumentException("존재하지 않는 약속입니다"));

        // When & Then
        mockMvc.perform(post("/api/notifications/kakao")
                        .header("X-User-ID", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"));
    }

    @Test
    void testKakaoNotification_정상요청시_200응답() throws Exception {
        // Given
        Long currentUserId = 1L;
        Long meetingId = 123L;
        
        KakaoNotifyResponse mockResponse = new KakaoNotifyResponse(1, 1);
        when(kakaoNotifyService.sendKakaoNotification(eq(currentUserId), eq(meetingId), isNull()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/notifications/kakao/test")
                        .header("X-User-ID", currentUserId)
                        .param("meetingId", meetingId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testResult").value("completed"))
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void checkKakaoNotificationAvailability_모든조건만족시_사용가능응답() throws Exception {
        // Given
        Long currentUserId = 1L;
        
        when(kakaoNotifyService.checkUserConsent(currentUserId)).thenReturn(true);
        when(kakaoNotifyService.checkKakaoInfo(currentUserId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/notifications/kakao/availability")
                        .header("X-User-ID", currentUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.hasConsent").value(true))
                .andExpect(jsonPath("$.hasKakaoInfo").value(true))
                .andExpect(jsonPath("$.message").value("카카오톡 알림 전송이 가능합니다"));
    }

    @Test
    void checkKakaoNotificationAvailability_동의없음시_사용불가응답() throws Exception {
        // Given
        Long currentUserId = 1L;
        
        when(kakaoNotifyService.checkUserConsent(currentUserId)).thenReturn(false);
        when(kakaoNotifyService.checkKakaoInfo(currentUserId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/notifications/kakao/availability")
                        .header("X-User-ID", currentUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.hasConsent").value(false))
                .andExpect(jsonPath("$.hasKakaoInfo").value(true))
                .andExpect(jsonPath("$.message").value("카카오톡 메시지 전송에 동의해주세요"));
    }

    @Test
    void sendKakaoNotification_필수헤더없음시_400응답() throws Exception {
        // Given
        NotifyKakaoRequest request = new NotifyKakaoRequest(123L, List.of(2L, 3L));

        // When & Then - X-User-ID 헤더 없이 요청
        mockMvc.perform(post("/api/notifications/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendKakaoNotification_필수필드없음시_400응답() throws Exception {
        // Given
        Long currentUserId = 1L;
        String invalidRequestJson = "{\"receiverIds\": [2, 3]}"; // meetingId 누락

        // When & Then
        mockMvc.perform(post("/api/notifications/kakao")
                        .header("X-User-ID", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }
}
