package com.promiseservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 알림 로그 응답 DTO
 * 이유: API 응답 형태를 표준화하고 프론트엔드에서 사용하기 쉬운 구조 제공
 */
@Data
@Builder
public class NotificationLogResponse {
    
    /**
     * 로그 ID
     */
    private Long id;
    
    /**
     * 약속 ID
     */
    private Long meetingId;
    
    /**
     * 사용자 ID
     */
    private Long userId;
    
    /**
     * 알림 채널 (KAKAO, SMS, EMAIL)
     */
    private String channel;
    
    /**
     * HTTP 상태 코드
     */
    private Integer httpStatus;
    
    /**
     * 서비스별 결과 코드
     */
    private Integer resultCode;
    
    /**
     * 성공 여부
     */
    private Boolean success;
    
    /**
     * 에러 메시지
     */
    private String errorMessage;
    
    /**
     * 추적 ID
     */
    private String traceId;
    
    /**
     * 전송 시간
     */
    private LocalDateTime sentAt;
}
