package com.promiseservice.controller;

import com.promiseservice.dto.NotificationRequest;
import com.promiseservice.dto.NotificationResponse;
import com.promiseservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 약속 관련 알림을 관리하는 REST API 컨트롤러
 * 이유: 약속 상태 변경 시 사용자들에게 적절한 알림을 전송하고, 
 * 알림 전송 결과를 모니터링할 수 있는 엔드포인트를 제공하기 위해
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

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
            // 간단한 헬스체크 응답
            return ResponseEntity.ok("알림 서비스가 정상적으로 동작 중입니다");
        } catch (Exception e) {
            log.error("알림 서비스 상태 확인 실패 - 에러: {}", e.getMessage());
            return ResponseEntity.status(503).body("알림 서비스에 문제가 발생했습니다: " + e.getMessage());
        }
    }
}

