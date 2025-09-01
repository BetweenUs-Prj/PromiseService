package com.promiseservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 알림 전송 로그 엔티티
 * 이유: 카카오톡, SMS, 이메일 등 알림 전송 결과를 기록하여 전송 상태 추적 및 디버깅 지원
 */
@Entity
@Table(name = "notification_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationLog {

    /**
     * 로그 ID (자동 증가)
     * 이유: 각 전송 기록을 고유하게 식별하기 위해
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약속 ID
     * 이유: 어떤 약속에 대한 알림인지 추적하기 위해
     */
    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    /**
     * 사용자 ID
     * 이유: 알림을 받은 사용자를 식별하기 위해
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 알림 채널 (KAKAO, SMS, EMAIL)
     * 이유: 어떤 방식으로 알림을 보냈는지 구분하기 위해
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 32)
    private NotificationChannel channel;

    /**
     * 전송한 페이로드 JSON
     * 이유: 실제로 보낸 메시지 내용을 기록하여 디버깅 및 재전송 시 참고하기 위해
     */
    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    /**
     * HTTP 상태 코드
     * 이유: API 호출 결과를 기록하여 네트워크 레벨 문제 파악하기 위해
     */
    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    /**
     * 서비스별 결과 코드 (예: 카카오 result_code)
     * 이유: 각 서비스의 성공/실패 상태를 정확히 기록하기 위해
     */
    @Column(name = "result_code")
    private Integer resultCode;

    /**
     * 에러 응답 JSON
     * 이유: 실패 시 상세한 에러 정보를 기록하여 문제 해결에 활용하기 위해
     */
    @Column(name = "error_json", columnDefinition = "TEXT")
    private String errorJson;

    /**
     * 추적 ID (상관관계 ID)
     * 이유: 동일한 약속에 대한 여러 알림을 그룹핑하고 멱등성을 보장하기 위해
     */
    @Column(name = "trace_id", nullable = false, length = 64)
    private String traceId;

    /**
     * 생성 시간
     * 이유: 알림 전송 시점을 기록하여 시간순 조회 및 성능 분석에 활용하기 위해
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 성공 여부 판단 메서드
     * 이유: 로그 조회 시 성공/실패를 쉽게 판단할 수 있도록 편의 메서드 제공
     * 
     * @return HTTP 200대이고 result_code가 0이면 성공
     */
    public boolean isSuccess() {
        return httpStatus != null && httpStatus >= 200 && httpStatus < 300 
               && (resultCode == null || resultCode == 0);
    }

    /**
     * 알림 채널 열거형
     * 이유: 지원하는 알림 방식을 명확히 정의하고 타입 안전성 보장
     */
    public enum NotificationChannel {
        KAKAO,  // 카카오톡 메시지
        SMS,    // SMS 문자 메시지  
        EMAIL   // 이메일
    }
}
