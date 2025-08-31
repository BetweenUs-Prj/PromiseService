package com.promiseservice.controller;

import com.promiseservice.dto.NotificationRequest;
import com.promiseservice.dto.NotificationResponse;

import com.promiseservice.service.NotificationService;

import com.promiseservice.service.KakaoNotifyService;
import com.promiseservice.service.notification.UnifiedNotificationService;
import com.promiseservice.service.notification.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import com.promiseservice.dto.NotifyKakaoRequest;
import com.promiseservice.dto.KakaoNotifyResponse;

/**
 * 약속 관련 알림을 관리하는 REST API 컨트롤러
 * 이유: 약속 상태 변경 시 사용자들에게 적절한 알림을 전송하고, 
 * 알림 전송 결과를 모니터링할 수 있는 엔드포인트를 제공하기 위해.
 * 카카오톡 알림과 기타 알림을 통합 관리하여 다양한 알림 채널 지원
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private final KakaoNotifyService kakaoNotifyService;
    private final UnifiedNotificationService unifiedNotificationService;

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



    // =================================================================================
    // 통합 알림 엔드포인트 (알림톡 등)
    // =================================================================================

    /**
     * 통합 알림 전송 엔드포인트
     * 이유: 다양한 알림 채널을 통해 사용자에게 알림을 전송하기 위해
     * 
     * POST /api/notifications/unified
     */
    @PostMapping("/unified")
    public ResponseEntity<NotificationPort.SendResult> sendUnifiedNotification(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        @SuppressWarnings("unchecked")
        List<String> recipients = (List<String>) request.get("recipients");
        String templateCode = (String) request.get("templateCode");
        @SuppressWarnings("unchecked")
        Map<String, String> variables = (Map<String, String>) request.get("variables");
        String fallbackText = (String) request.get("fallbackText");
        
        log.info("통합 알림 전송 요청 - 사용자: {}, 수신자: {}명, 템플릿: {}", 
                currentUserId, recipients.size(), templateCode);
        
        try {
            // fallbackText가 없으면 자동 생성
            if (fallbackText == null || fallbackText.trim().isEmpty()) {
                fallbackText = unifiedNotificationService.createFallbackText(templateCode, variables);
            }
            
            NotificationPort.SendResult result;
            if (recipients.size() == 1) {
                // 단일 수신자
                result = unifiedNotificationService.sendNotice(recipients.get(0), templateCode, variables, fallbackText);
            } else {
                // 다중 수신자
                result = unifiedNotificationService.sendBulkNotice(recipients, templateCode, variables, fallbackText);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("통합 알림 전송 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 약속 관련 통합 알림 전송 엔드포인트
     * 이유: 약속 초대, 확정, 취소 등의 상황에서 알림톡 등을 통해 알림을 전송하기 위해
     * 
     * POST /api/notifications/meeting
     */
    @PostMapping("/meeting")
    public ResponseEntity<NotificationPort.SendResult> sendMeetingNotification(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        @SuppressWarnings("unchecked")
        List<String> recipients = (List<String>) request.get("recipients");
        String meetingTitle = (String) request.get("meetingTitle");
        String meetingTime = (String) request.get("meetingTime");
        String meetingLocation = (String) request.get("meetingLocation");
        String reason = (String) request.get("reason");
        String notificationType = (String) request.get("type"); // INVITATION, CONFIRMED, CANCELLED
        
        log.info("약속 관련 통합 알림 전송 요청 - 사용자: {}, 타입: {}, 수신자: {}명", 
                currentUserId, notificationType, recipients.size());
        
        try {
            // 템플릿 코드 결정
            String templateCode;
            switch (notificationType.toUpperCase()) {
                case "INVITATION":
                    templateCode = "MEETING_INVITATION";
                    break;
                case "CONFIRMED":
                    templateCode = "MEETING_CONFIRMED";
                    break;
                case "CANCELLED":
                    templateCode = "MEETING_CANCELLED";
                    break;
                default:
                    templateCode = "MEETING_NOTIFICATION";
            }
            
            // 템플릿 변수 생성
            Map<String, String> variables = unifiedNotificationService.createMeetingVariables(
                meetingTitle, meetingTime, meetingLocation, reason);
            
            // 대체발송용 텍스트 자동 생성
            String fallbackText = unifiedNotificationService.createFallbackText(templateCode, variables);
            
            // 통합 알림 전송
            NotificationPort.SendResult result = unifiedNotificationService.sendBulkNotice(
                recipients, templateCode, variables, fallbackText);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("약속 관련 통합 알림 전송 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 알림 채널 상태 확인 엔드포인트
     * 이유: 알림톡 등 모든 알림 채널의 상태를 한 번에 확인하기 위해
     * 
     * GET /api/notifications/channels/health
     */
    @GetMapping("/channels/health")
    public ResponseEntity<Map<String, Object>> checkAllChannelsHealth() {
        log.info("모든 알림 채널 상태 확인 요청");
        try {
            Map<String, Boolean> channelStatus = unifiedNotificationService.checkChannelStatus();
            
            Map<String, Object> response = new java.util.HashMap<>(channelStatus);
            response.put("timestamp", System.currentTimeMillis());
            response.put("overallStatus", channelStatus.values().stream().anyMatch(Boolean::booleanValue) ? "UP" : "DOWN");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 채널 상태 확인 실패 - 에러: {}", e.getMessage());
            Map<String, Object> response = Map.of(
                "overallStatus", "DOWN",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.status(503).body(response);
        }
    }

    // =================================================================================
    // 카카오톡 알림 관련 엔드포인트
    // =================================================================================

    /**
     * 카카오톡 알림 전송 엔드포인트
     * 이유: 약속 확정 시 카카오톡으로 알림을 전송하여 참여자들에게 신속하게 정보를 전달하기 위해
     * 
     * POST /api/notifications/kakao
     */
    @PostMapping("/kakao")
    public ResponseEntity<?> sendKakaoNotification(
            @Valid @RequestBody NotifyKakaoRequest request,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("카카오톡 알림 전송 요청 - 발송자: {}, 약속: {}, 수신자: {}명", 
                currentUserId, request.getMeetingId(), request.getReceiverCount());

        try {
            // 카카오톡 알림 전송
            KakaoNotifyResponse response = kakaoNotifyService.sendKakaoNotification(
                    currentUserId, request.getMeetingId(), request.getReceiverIds());

            // 성공/실패에 따른 HTTP 상태 코드 결정
            if (response.isSuccess()) {
                if (response.isPartialSuccess()) {
                    log.warn("카카오톡 알림 부분 성공 - 성공: {}, 실패: {}", 
                            response.getSentCount(), response.getFailedCount());
                }
                return ResponseEntity.ok(response);
            } else {
                log.warn("카카오톡 알림 전송 실패 - 이유: {}", response.getMessage());
                return ResponseEntity.ok(response); // 비즈니스 로직 실패는 200으로 반환
            }

        } catch (IllegalStateException e) {
            // 동의 없음, 토큰 만료 등의 경우
            log.warn("카카오톡 알림 전송 조건 불충족 - 발송자: {}, 이유: {}", currentUserId, e.getMessage());
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "error", "CONSENT_REQUIRED",
                    "message", e.getMessage(),
                    "guide", "카카오 로그인 후 알림 전송 권한을 허용해주세요"
            ));
        } catch (IllegalArgumentException e) {
            // 잘못된 파라미터
            log.warn("카카오톡 알림 전송 파라미터 오류 - 발송자: {}, 이유: {}", currentUserId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "INVALID_PARAMETER",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            // 기타 시스템 오류
            log.error("카카오톡 알림 전송 중 시스템 오류 - 발송자: {}", currentUserId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "SYSTEM_ERROR",
                    "message", "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요"
            ));
        }
    }

    /**
     * 카카오톡 알림 전송 테스트 엔드포인트
     * 이유: 개발 및 테스트 환경에서 카카오톡 알림 기능을 테스트하기 위해
     * 
     * POST /api/notifications/kakao/test
     */
    @PostMapping("/kakao/test")
    public ResponseEntity<?> testKakaoNotification(
            @RequestParam Long meetingId,
            @RequestParam(required = false) List<Long> receiverIds,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("카카오톡 알림 테스트 요청 - 발송자: {}, 약속: {}", currentUserId, meetingId);

        try {
            // NotifyKakaoRequest request = new NotifyKakaoRequest(meetingId, receiverIds);
            KakaoNotifyResponse response = kakaoNotifyService.sendKakaoNotification(
                    currentUserId, meetingId, receiverIds);

            return ResponseEntity.ok(Map.of(
                    "testResult", "completed",
                    "response", response,
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("카카오톡 알림 테스트 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                    "testResult", "failed",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 사용자의 카카오 알림 전송 가능 여부 확인 엔드포인트
     * 이유: 클라이언트에서 카카오 알림 기능 사용 가능 여부를 미리 확인하기 위해
     * 
     * GET /api/notifications/kakao/availability
     */
    @GetMapping("/kakao/availability")
    public ResponseEntity<Map<String, Object>> checkKakaoNotificationAvailability(
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("카카오 알림 사용 가능 여부 확인 - 사용자: {}", currentUserId);

        try {
            // 기본 검증만 수행 (실제 토큰 검증은 생략)
            boolean hasConsent = kakaoNotifyService.checkUserConsent(currentUserId);
            boolean hasKakaoInfo = kakaoNotifyService.checkKakaoInfo(currentUserId);
            
            Map<String, Object> availability = Map.of(
                    "available", hasConsent && hasKakaoInfo,
                    "hasConsent", hasConsent,
                    "hasKakaoInfo", hasKakaoInfo,
                    "message", getAvailabilityMessage(hasConsent, hasKakaoInfo)
            );

            return ResponseEntity.ok(availability);

        } catch (Exception e) {
            log.error("카카오 알림 사용 가능 여부 확인 실패", e);
            return ResponseEntity.ok(Map.of(
                    "available", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 알림 서비스 상태를 확인하는 엔드포인트
     * 이유: 알림 서비스의 동작 상태를 모니터링하고 헬스체크를 수행할 수 있도록 지원
     * 
     * GET /api/notifications/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkNotificationServiceHealth() {
        log.info("알림 서비스 상태 확인 요청");
        
        try {
            Map<String, Boolean> channelStatus = unifiedNotificationService.checkChannelStatus();
            
            Map<String, Object> response = Map.of(
                "service", "NotificationService",
                "status", "UP",
                "channels", channelStatus,
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 서비스 상태 확인 실패 - 에러: {}", e.getMessage());
            Map<String, Object> response = Map.of(
                "service", "NotificationService",
                "status", "DOWN",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * 카카오 알림 사용 가능 여부 메시지를 생성하는 헬퍼 메서드
     * 이유: 사용자에게 카카오 알림 사용 불가 이유를 명확히 안내하기 위해
     * 
     * @param hasConsent 동의 여부
     * @param hasKakaoInfo 카카오 정보 등록 여부
     * @return 안내 메시지
     */
    private String getAvailabilityMessage(boolean hasConsent, boolean hasKakaoInfo) {
        if (!hasConsent && !hasKakaoInfo) {
            return "카카오 로그인 후 알림 전송 권한을 허용해주세요";
        } else if (!hasConsent) {
            return "카카오톡 메시지 전송에 동의해주세요";
        } else if (!hasKakaoInfo) {
            return "카카오 계정 연동이 필요합니다";
        } else {
            return "카카오톡 알림 전송이 가능합니다";
        }
    }
}






