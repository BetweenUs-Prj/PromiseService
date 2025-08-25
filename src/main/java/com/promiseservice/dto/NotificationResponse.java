package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 응답을 위한 DTO
 * 이유: 알림 전송 결과와 상태 정보를 체계적으로 제공하여 알림 서비스의 동작 상태를 추적하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class NotificationResponse {

    // 알림 전송 성공한 사용자 ID 목록
    // 이유: 성공적으로 알림을 받은 사용자들을 명확히 하여 전송 결과 추적
    private List<Long> successfullyNotified;

    // 알림 전송 실패한 사용자 ID 목록
    // 이유: 알림 전송에 실패한 사용자들을 파악하여 재전송이나 대체 방법 모색
    private List<Long> failedToNotify;

    // 알림 전송 시간
    // 이유: 알림이 언제 전송되었는지 기록하여 알림 이력 관리 및 디버깅 지원
    private LocalDateTime sentAt;

    // 알림 전송 결과 메시지
    // 이유: 전송 결과를 사용자 친화적으로 표현하여 알림 서비스 상태를 명확히 전달
    private String message;

    // 총 전송 대상 사용자 수
    // 이유: 알림 전송 규모를 파악하여 서비스 성능 및 사용량 분석 지원
    private int totalRecipients;

    // 성공적으로 전송된 알림 수
    // 이유: 알림 전송 성공률을 계산하여 서비스 품질 모니터링 지원
    private int successCount;

    // 전송 실패한 알림 수
    // 이유: 알림 전송 실패율을 계산하여 서비스 개선 포인트 파악 지원
    private int failureCount;

    /**
     * 알림 응답 생성자
     * 이유: 알림 전송 결과를 한 번에 설정하여 응답 객체를 효율적으로 초기화하기 위해
     */
    public NotificationResponse(List<Long> successfullyNotified, List<Long> failedToNotify) {
        this.successfullyNotified = successfullyNotified;
        this.failedToNotify = failedToNotify;
        this.sentAt = LocalDateTime.now();
        this.totalRecipients = successfullyNotified.size() + failedToNotify.size();
        this.successCount = successfullyNotified.size();
        this.failureCount = failedToNotify.size();
        
        // 결과 메시지 생성
        StringBuilder sb = new StringBuilder();
        if (!successfullyNotified.isEmpty()) {
            sb.append("성공적으로 전송된 알림: ").append(successCount).append("건");
        }
        if (!failedToNotify.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("전송 실패한 알림: ").append(failureCount).append("건");
        }
        this.message = sb.toString();
    }
}







