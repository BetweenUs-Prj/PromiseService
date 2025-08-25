package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * SMS 알림 응답을 위한 DTO
 * 이유: SMS 전송 결과와 상태 정보를 체계적으로 제공하여 SMS 서비스의 동작 상태를 추적하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class SmsNotificationResponse {

    // SMS 전송 성공한 전화번호 목록
    // 이유: 성공적으로 SMS를 받은 전화번호들을 명확히 하여 전송 결과 추적
    private List<String> successfullyNotified;

    // SMS 전송 실패한 전화번호 목록
    // 이유: SMS 전송에 실패한 전화번호들을 파악하여 재전송이나 대체 방법 모색
    private List<String> failedToNotify;

    // 각 전화번호별 전송 실패 사유
    // 이유: 실패 원인을 상세히 파악하여 문제 해결 및 서비스 개선에 활용
    private Map<String, String> failureReasons;

    // SMS 전송 시간
    // 이유: SMS가 언제 전송되었는지 기록하여 SMS 이력 관리 및 디버깅 지원
    private LocalDateTime sentAt;

    // SMS 전송 결과 메시지
    // 이유: 전송 결과를 사용자 친화적으로 표현하여 SMS 서비스 상태를 명확히 전달
    private String message;

    // 총 전송 대상 전화번호 수
    // 이유: SMS 전송 규모를 파악하여 서비스 성능 및 사용량 분석 지원
    private int totalRecipients;

    // 성공적으로 전송된 SMS 수
    // 이유: SMS 전송 성공률을 계산하여 서비스 품질 모니터링 지원
    private int successCount;

    // 전송 실패한 SMS 수
    // 이유: SMS 전송 실패율을 계산하여 서비스 개선 포인트 파악 지원
    private int failureCount;

    // SMS 서비스 제공업체 응답 정보
    // 이유: 외부 SMS 서비스의 응답을 기록하여 문제 발생 시 추적 및 디버깅 지원
    private String providerResponse;

    // 전송 비용 정보 (선택사항)
    // 이유: SMS 전송 비용을 추적하여 서비스 운영 비용 관리 지원
    private Double cost;

    // 메시지 ID (SMS 서비스 제공업체에서 제공하는 고유 ID)
    // 이유: 각 SMS의 고유 식별자를 통해 전송 상태 추적 및 문의 시 참조
    private List<String> messageIds;

    /**
     * SMS 응답 생성자
     * 이유: SMS 전송 결과를 한 번에 설정하여 응답 객체를 효율적으로 초기화하기 위해
     */
    public SmsNotificationResponse(List<String> successfullyNotified, List<String> failedToNotify) {
        this.successfullyNotified = successfullyNotified;
        this.failedToNotify = failedToNotify;
        this.sentAt = LocalDateTime.now();
        this.totalRecipients = successfullyNotified.size() + failedToNotify.size();
        this.successCount = successfullyNotified.size();
        this.failureCount = failedToNotify.size();
        
        // 결과 메시지 생성
        StringBuilder sb = new StringBuilder();
        if (!successfullyNotified.isEmpty()) {
            sb.append("성공적으로 전송된 SMS: ").append(successCount).append("건");
        }
        if (!failedToNotify.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("전송 실패한 SMS: ").append(failureCount).append("건");
        }
        this.message = sb.toString();
    }

    /**
     * 전송 성공률 계산
     * 이유: SMS 전송 성공률을 백분율로 제공하여 서비스 품질 지표 확인
     * 
     * @return 전송 성공률 (0.0 ~ 100.0)
     */
    public double getSuccessRate() {
        if (totalRecipients == 0) {
            return 0.0;
        }
        return (double) successCount / totalRecipients * 100.0;
    }

    /**
     * 전송 실패율 계산
     * 이유: SMS 전송 실패율을 백분율로 제공하여 서비스 개선 필요성 판단
     * 
     * @return 전송 실패율 (0.0 ~ 100.0)
     */
    public double getFailureRate() {
        if (totalRecipients == 0) {
            return 0.0;
        }
        return (double) failureCount / totalRecipients * 100.0;
    }

    /**
     * 전송 결과 요약 정보 생성
     * 이유: 전송 결과를 간결하게 요약하여 빠른 상태 파악 지원
     * 
     * @return 전송 결과 요약
     */
    public String getSummary() {
        return String.format("전체 %d건 중 성공 %d건(%.1f%%), 실패 %d건(%.1f%%)", 
                totalRecipients, successCount, getSuccessRate(), 
                failureCount, getFailureRate());
    }
}

