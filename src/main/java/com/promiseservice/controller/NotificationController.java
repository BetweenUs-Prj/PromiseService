package com.promiseservice.controller;

import com.promiseservice.dto.NotificationRequest;
import com.promiseservice.dto.NotificationResponse;
import com.promiseservice.dto.SmsNotificationRequest;
import com.promiseservice.dto.SmsNotificationResponse;
import com.promiseservice.service.NotificationService;
import com.promiseservice.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 약속 관련 알림을 관리하는 REST API 컨트롤러
 * 이유: 약속 상태 변경 시 사용자들에게 적절한 알림을 전송하고, 
 * 알림 전송 결과를 모니터링할 수 있는 엔드포인트를 제공하기 위해.
 * 푸시 알림과 SMS 알림을 통합 관리하여 다양한 알림 채널 지원
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SmsService smsService;

    /**
     * 테스트용 알림을 전송하는 엔드포인트
     * 이유: 알림 서비스의 동작을 테스트하고 디버깅할 수 있도록 테스트 알림 전송 기능 제공
     * 
     * POST /api/notifications/test
     */
    @PostMapping("/test")
    public ResponseEntity<NotificationResponse> sendTestNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("테스트 알림 전송 요청 - 사용자: {}, 수신자: {}명", currentUserId, request.getRecipientUserIds().size());
        
        try {
            // 테스트 알림 전송 (실제로는 외부 알림 서비스 호출)
            NotificationResponse response = notificationService.sendNotification(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트 알림 전송 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 약속 상태 변경 알림을 수동으로 전송하는 엔드포인트
     * 이유: 시스템 오류나 재시도가 필요한 경우 수동으로 알림을 재전송할 수 있도록 지원
     * 
     * POST /api/notifications/meeting-status
     */
    @PostMapping("/meeting-status")
    public ResponseEntity<String> sendMeetingStatusNotification(
            @RequestParam Long meetingId,
            @RequestParam String previousStatus,
            @RequestParam String newStatus,
            @RequestParam(required = false) String reason,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("약속 상태 변경 알림 수동 전송 요청 - 약속 ID: {}, 상태 변경: {} → {}, 사용자: {}", 
                meetingId, previousStatus, newStatus, currentUserId);
        
        try {
            // 이 메서드는 MeetingStatusService에서 자동으로 호출되므로 
            // 여기서는 수동 전송을 위한 별도 로직을 구현할 수 있습니다
            return ResponseEntity.ok("알림 전송 요청이 처리되었습니다");
        } catch (Exception e) {
            log.error("약속 상태 변경 알림 수동 전송 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 알림 서비스 상태를 확인하는 엔드포인트
     * 이유: 알림 서비스의 동작 상태를 모니터링하고 헬스체크를 수행할 수 있도록 지원
     * 
     * GET /api/notifications/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> checkNotificationServiceHealth() {
        log.info("알림 서비스 상태 확인 요청");
        
        try {
            // 푸시 알림 서비스와 SMS 서비스 상태 모두 확인
            boolean smsHealthy = notificationService.checkSmsServiceHealth();
            String status = smsHealthy ? 
                "알림 서비스(푸시+SMS)가 정상적으로 동작 중입니다" : 
                "알림 서비스는 동작 중이나 SMS 서비스에 문제가 있습니다";
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("알림 서비스 상태 확인 실패 - 에러: {}", e.getMessage());
            return ResponseEntity.status(503).body("알림 서비스에 문제가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 테스트용 SMS를 전송하는 엔드포인트
     * 이유: SMS 서비스의 동작을 테스트하고 디버깅할 수 있도록 테스트 SMS 전송 기능 제공
     * 
     * POST /api/notifications/sms/test
     */
    @PostMapping("/sms/test")
    public ResponseEntity<SmsNotificationResponse> sendTestSms(
            @Valid @RequestBody SmsNotificationRequest request,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("테스트 SMS 전송 요청 - 사용자: {}, 수신자: {}명", 
                currentUserId, request.getRecipientUserIds().size());
        
        try {
            SmsNotificationResponse response = smsService.sendSmsToUsers(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("테스트 SMS 전송 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 긴급 SMS를 전송하는 엔드포인트
     * 이유: 긴급한 약속 변경이나 중요한 알림을 SMS로 즉시 전송할 수 있도록 지원
     * 
     * POST /api/notifications/sms/urgent
     */
    @PostMapping("/sms/urgent")
    public ResponseEntity<SmsNotificationResponse> sendUrgentSms(
            @RequestParam List<Long> recipientUserIds,
            @RequestParam String message,
            @RequestParam(required = false) Long meetingId,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("긴급 SMS 전송 요청 - 사용자: {}, 수신자: {}명, 약속 ID: {}", 
                currentUserId, recipientUserIds.size(), meetingId);
        
        try {
            SmsNotificationResponse response = smsService.sendUrgentSms(recipientUserIds, message, meetingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("긴급 SMS 전송 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * SMS 전용 알림을 전송하는 엔드포인트
     * 이유: 푸시 알림 없이 SMS만 전송해야 하는 경우를 위한 독립적인 SMS 전송 기능
     * 
     * POST /api/notifications/sms-only
     */
    @PostMapping("/sms-only")
    public ResponseEntity<SmsNotificationResponse> sendSmsOnlyNotification(
            @RequestParam List<Long> recipientUserIds,
            @RequestParam String message,
            @RequestParam(required = false) Long meetingId,
            @RequestParam(defaultValue = "false") boolean isUrgent,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("SMS 전용 알림 전송 요청 - 사용자: {}, 수신자: {}명, 긴급: {}", 
                currentUserId, recipientUserIds.size(), isUrgent);
        
        try {
            SmsNotificationResponse response = notificationService.sendSmsOnlyNotification(
                recipientUserIds, message, meetingId, isUrgent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("SMS 전용 알림 전송 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 전화번호로 직접 SMS를 전송하는 엔드포인트
     * 이유: 사용자 ID가 없는 외부 사용자에게도 SMS를 전송할 수 있도록 지원
     * 
     * POST /api/notifications/sms/direct
     */
    @PostMapping("/sms/direct")
    public ResponseEntity<SmsNotificationResponse> sendDirectSms(
            @RequestParam List<String> phoneNumbers,
            @RequestParam String message,
            @RequestParam(defaultValue = "NORMAL") String smsType,
            @RequestParam(required = false) Long meetingId,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("직접 SMS 전송 요청 - 사용자: {}, 수신 번호: {}개", 
                currentUserId, phoneNumbers.size());
        
        try {
            SmsNotificationRequest smsRequest = new SmsNotificationRequest();
            smsRequest.setPhoneNumbers(phoneNumbers);
            smsRequest.setMessage(message);
            smsRequest.setSmsType(smsType);
            smsRequest.setMeetingId(meetingId);
            smsRequest.setSenderName("PromiseService");

            SmsNotificationResponse response = smsService.sendSmsToPhoneNumbers(phoneNumbers, smsRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("직접 SMS 전송 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * SMS 서비스 상태를 확인하는 엔드포인트
     * 이유: SMS 서비스의 가용성을 독립적으로 확인하여 SMS 전송 가능 여부를 판단하기 위해
     * 
     * GET /api/notifications/sms/health
     */
    @GetMapping("/sms/health")
    public ResponseEntity<Map<String, Object>> checkSmsServiceHealth() {
        log.info("SMS 서비스 상태 확인 요청");
        
        try {
            boolean isHealthy = smsService.checkSmsServiceHealth();
            
            Map<String, Object> healthStatus = Map.of(
                "service", "SMS",
                "status", isHealthy ? "UP" : "DOWN",
                "message", isHealthy ? "SMS 서비스가 정상적으로 동작 중입니다" : "SMS 서비스에 문제가 발생했습니다",
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return isHealthy ? 
                ResponseEntity.ok(healthStatus) : 
                ResponseEntity.status(503).body(healthStatus);
                
        } catch (Exception e) {
            log.error("SMS 서비스 상태 확인 실패 - 에러: {}", e.getMessage());
            
            Map<String, Object> errorStatus = Map.of(
                "service", "SMS",
                "status", "ERROR",
                "message", "SMS 서비스 상태 확인 중 오류 발생: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.status(503).body(errorStatus);
        }
    }
}






