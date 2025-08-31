package com.promiseservice.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 통합 알림 서비스
 * 이유: 다양한 알림 채널을 통해 사용자에게 알림을 전송하기 위해
 * 
 * 전송 전략:
 * 1. 알림톡 우선 시도 (템플릿 기반)
 * 2. 다른 채널로 대체발송
 * 3. 전송 결과 통합 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedNotificationService {

    private final AlimtalkPort alimtalkPort;


    /**
     * 통합 알림 전송
     * 이유: 알림톡을 통해 사용자에게 알림을 전송하기 위해
     * 
     * @param to 수신자 전화번호
     * @param templateCode 알림톡 템플릿 코드
     * @param variables 템플릿 변수
     * @param fallbackText 대체발송용 텍스트
     * @return 최종 전송 결과
     */
    public NotificationPort.SendResult sendNotice(String to, String templateCode, 
                                                 Map<String, String> variables, String fallbackText) {
        log.info("통합 알림 전송 시작 - 수신자: {}, 템플릿: {}", to, templateCode);
        
        try {
            // 알림톡 전송 시도
            log.info("알림톡 전송 시도 - 수신자: {}", to);
            NotificationPort.SendResult alimtalkResult = alimtalkPort.sendTemplate(to, templateCode, variables);
            
            if (alimtalkResult.isSuccess()) {
                log.info("알림톡 전송 성공 - 수신자: {}", to);
                return alimtalkResult;
            } else {
                log.error("알림톡 전송 실패 - 수신자: {}, 실패사유: {}", to, alimtalkResult.getMessage());
                return new NotificationPort.SendResult(
                    false,
                    "알림톡 전송 실패",
                    "ALIMTALK_FAILED",
                    List.of(),
                    List.of(to)
                );
            }
            
        } catch (Exception e) {
            log.error("통합 알림 전송 중 예외 발생 - 수신자: {}, 에러: {}", to, e.getMessage());
            return new NotificationPort.SendResult(
                false,
                "통합 알림 전송 중 오류 발생: " + e.getMessage(),
                "UNIFIED_NOTIFICATION_EXCEPTION",
                List.of(),
                List.of(to)
            );
        }
    }

    /**
     * 다중 수신자 통합 알림 전송
     * 이유: 여러 사용자에게 동시에 알림을 전송하여 약속 참여자들에게 일괄 알림 제공
     * 
     * @param recipients 수신자 전화번호 목록
     * @param templateCode 알림톡 템플릿 코드
     * @param variables 템플릿 변수
     * @param fallbackText 대체발송용 텍스트
     * @return 통합 전송 결과
     */
    public NotificationPort.SendResult sendBulkNotice(List<String> recipients, String templateCode,
                                                     Map<String, String> variables, String fallbackText) {
        log.info("다중 수신자 통합 알림 전송 시작 - 수신자: {}명, 템플릿: {}", recipients.size(), templateCode);
        
        List<String> totalSuccess = new java.util.ArrayList<>();
        List<String> totalFailed = new java.util.ArrayList<>();
        
        for (String recipient : recipients) {
            NotificationPort.SendResult result = sendNotice(recipient, templateCode, variables, fallbackText);
            
            if (result.isSuccess()) {
                totalSuccess.addAll(result.getSuccessRecipients());
            } else {
                totalFailed.addAll(result.getFailedRecipients());
            }
        }
        
        boolean overallSuccess = !totalSuccess.isEmpty();
        String message = String.format("전체 %d명 중 성공 %d명, 실패 %d명", 
                recipients.size(), totalSuccess.size(), totalFailed.size());
        
        log.info("다중 수신자 통합 알림 전송 완료 - {}", message);
        
        return new NotificationPort.SendResult(
            overallSuccess,
            message,
            overallSuccess ? null : "BULK_SEND_PARTIAL_FAILED",
            totalSuccess,
            totalFailed
        );
    }

    /**
     * 알림 채널별 상태 확인
     * 이유: 각 알림 채널의 상태를 확인하여 서비스 가용성 모니터링
     * 
     * @return 채널별 상태 정보
     */
    public Map<String, Boolean> checkChannelStatus() {
        Map<String, Boolean> status = new java.util.HashMap<>();
        
        try {
            // 알림톡 상태 확인
            boolean alimtalkStatus = alimtalkPort.checkAlimtalkServiceHealth();
            status.put("ALIMTALK", alimtalkStatus);
            
            log.info("알림 채널 상태 확인 완료 - 알림톡: {}", alimtalkStatus);
            
        } catch (Exception e) {
            log.error("알림 채널 상태 확인 중 오류 발생: {}", e.getMessage());
            status.put("ALIMTALK", false);
            // 다른 채널 상태는 기본적으로 false
        }
        
        return status;
    }

    /**
     * 약속별 템플릿 변수 생성
     * 이유: 약속 정보를 알림톡 템플릿에 맞는 변수로 변환하여 일관된 알림 제공
     * 
     * @param meetingTitle 약속 제목
     * @param meetingTime 약속 시간
     * @param meetingLocation 약속 장소
     * @param reason 사유 (선택사항)
     * @return 템플릿 변수 맵
     */
    public Map<String, String> createMeetingVariables(String meetingTitle, String meetingTime, 
                                                     String meetingLocation, String reason) {
        Map<String, String> variables = new java.util.HashMap<>();
        variables.put("title", meetingTitle != null ? meetingTitle : "약속");
        variables.put("time", meetingTime != null ? meetingTime : "시간 미정");
        variables.put("location", meetingLocation != null ? meetingLocation : "장소 미정");
        
        if (reason != null && !reason.trim().isEmpty()) {
            variables.put("reason", reason);
        }
        
        return variables;
    }

    /**
     * 대체발송용 텍스트 생성
     * 이유: 알림톡 실패 시 사용할 텍스트를 자동 생성하여 일관된 메시지 제공
     * 
     * @param templateCode 템플릿 코드
     * @param variables 템플릿 변수
     * @return 대체발송용 텍스트
     */
    public String createFallbackText(String templateCode, Map<String, String> variables) {
        StringBuilder text = new StringBuilder();
        
        switch (templateCode) {
            case "MEETING_INVITATION":
                text.append("약속 초대: ").append(variables.getOrDefault("title", "새로운 약속"))
                    .append("\n시간: ").append(variables.getOrDefault("time", ""))
                    .append("\n장소: ").append(variables.getOrDefault("location", ""));
                break;
                
            case "MEETING_CONFIRMED":
                text.append(variables.getOrDefault("title", "약속")).append("이 확정되었습니다.")
                    .append("\n시간: ").append(variables.getOrDefault("time", ""))
                    .append("\n장소: ").append(variables.getOrDefault("location", ""));
                break;
                
            case "MEETING_CANCELLED":
                text.append(variables.getOrDefault("title", "약속")).append("이 취소되었습니다.");
                if (variables.containsKey("reason")) {
                    text.append("\n사유: ").append(variables.get("reason"));
                }
                break;
                
            default:
                text.append("약속 알림: ").append(variables.getOrDefault("title", ""));
                variables.forEach((key, value) -> {
                    if (!"title".equals(key)) {
                        text.append("\n").append(key).append(": ").append(value);
                    }
                });
        }
        
        return text.toString();
    }
}




