package com.promiseservice.controller;

import com.promiseservice.dto.KakaoNotificationRequest;
import com.promiseservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 알림 관련 API 컨트롤러
 * 이유: 카카오톡을 통한 알림 전송 등 알림 서비스의 핵심 기능을 제공하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 카카오 알림 전송 API
     * 이유: 사용자들에게 카카오톡을 통해 알림을 전송할 수 있도록 하기 위해
     * 
     * @param request 알림 전송 요청 데이터
     * @return 전송 결과
     */
    @PostMapping("/kakao/send")
    public ResponseEntity<Object> sendKakaoNotification(@Valid @RequestBody KakaoNotificationRequest request) {
        log.info("카카오 알림 전송 요청 - 템플릿: {}, 대상: {}명", 
                request.getTemplate(), request.getToUserIds().size());

        try {
            Object response = notificationService.sendKakaoNotification(request);
            log.info("카카오 알림 전송 완료 - 템플릿: {}", request.getTemplate());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카카오 알림 전송 실패 - 에러: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 알림 전송 상태 확인 API
     * 이유: 발송된 알림의 전송 상태를 확인할 수 있도록 하기 위해
     * 
     * @param notificationId 알림 ID
     * @return 알림 상태
     */
    @GetMapping("/{notificationId}/status")
    public ResponseEntity<Object> getNotificationStatus(@PathVariable Long notificationId) {
        log.info("알림 상태 조회 요청 - ID: {}", notificationId);

        try {
            Object response = notificationService.getNotificationStatus(notificationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 상태 조회 실패 - ID: {}, 에러: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 알림 전송 이력 조회 API
     * 이유: 사용자별로 발송된 알림의 이력을 조회할 수 있도록 하기 위해
     * 
     * @param userId 사용자 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 알림 이력
     */
    @GetMapping("/history")
    public ResponseEntity<Object> getNotificationHistory(@RequestParam Long userId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        log.info("알림 이력 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}", userId, page, size);

        try {
            Object response = notificationService.getNotificationHistory(userId, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 이력 조회 실패 - 사용자: {}, 에러: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 알림 템플릿 목록 조회 API
     * 이유: 사용 가능한 알림 템플릿 목록을 조회할 수 있도록 하기 위해
     * 
     * @return 템플릿 목록
     */
    @GetMapping("/templates")
    public ResponseEntity<Object> getNotificationTemplates() {
        log.info("알림 템플릿 목록 조회 요청");

        try {
            Object response = notificationService.getNotificationTemplates();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 템플릿 목록 조회 실패 - 에러: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
