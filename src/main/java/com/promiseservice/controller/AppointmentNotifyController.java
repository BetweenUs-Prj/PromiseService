package com.promiseservice.controller;

import com.promiseservice.service.AppointmentNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 약속 확정 및 알림 관리를 위한 REST API 컨트롤러
 * 이유: 호스트가 약속을 확정하고 참여자들에게 알림을 발송하는 기능과
 * 참여자가 약속 초대에 응답하는 기능을 제공하기 위해
 */
@Slf4j
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentNotifyController {

    private final AppointmentNotifyService appointmentNotifyService;

    /**
     * 약속 확정 및 참여자들에게 알림 발송
     * 이유: 호스트가 약속을 최종 확정하고 수락한 참여자들에게 확정 알림을 전송하기 위해
     * 
     * @param appointmentId 확정할 약속 ID
     * @param request HTTP 요청 (현재 사용자 정보 확인용)
     * @return 확정 처리 결과
     */
    @PostMapping("/{appointmentId}/confirm")
    public ResponseEntity<?> confirmAppointment(
            @PathVariable Long appointmentId,
            HttpServletRequest request) {
        
        try {
            Long currentUserId = (Long) request.getAttribute("userId");
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            String result = appointmentNotifyService.confirmAndNotify(appointmentId, currentUserId);
            log.info("약속 확정 성공: 약속 ID {}, 호스트 ID {}", appointmentId, currentUserId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "appointmentId", appointmentId,
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("약속 확정 실패: 약속 ID {}, 오류: {}", appointmentId, e.getMessage());
            
            if (e.getMessage().contains("FORBIDDEN")) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "권한이 없습니다. 호스트만 약속을 확정할 수 있습니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "약속 확정 중 오류가 발생했습니다.",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 약속 취소 및 참여자들에게 취소 알림 발송
     * 이유: 호스트가 약속을 취소하고 모든 참여자들에게 취소 알림을 전송하기 위해
     * 
     * @param appointmentId 취소할 약속 ID
     * @param request HTTP 요청 (현재 사용자 정보 확인용)
     * @return 취소 처리 결과
     */
    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId,
            HttpServletRequest request) {
        
        try {
            Long currentUserId = (Long) request.getAttribute("userId");
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            String result = appointmentNotifyService.cancelAppointment(appointmentId, currentUserId);
            log.info("약속 취소 성공: 약속 ID {}, 호스트 ID {}", appointmentId, currentUserId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "appointmentId", appointmentId,
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("약속 취소 실패: 약속 ID {}, 오류: {}", appointmentId, e.getMessage());
            
            if (e.getMessage().contains("FORBIDDEN")) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "권한이 없습니다. 호스트만 약속을 취소할 수 있습니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "약속 취소 중 오류가 발생했습니다.",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 약속 초대에 대한 응답 (수락/거절)
     * 이유: 초대받은 참여자가 약속에 대해 수락 또는 거절할 수 있도록 하기 위해
     * 
     * @param appointmentId 약속 ID
     * @param responseData 응답 데이터 (accept: true/false)
     * @param request HTTP 요청 (현재 사용자 정보 확인용)
     * @return 응답 처리 결과
     */
    @PostMapping("/{appointmentId}/respond")
    public ResponseEntity<?> respondToInvitation(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, Object> responseData,
            HttpServletRequest request) {
        
        try {
            Long currentUserId = (Long) request.getAttribute("userId");
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            Boolean accept = (Boolean) responseData.get("accept");
            if (accept == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "응답 여부(accept)를 지정해주세요.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            String result = appointmentNotifyService.respondToInvitation(appointmentId, currentUserId, accept);
            log.info("초대 응답 성공: 약속 ID {}, 사용자 ID {}, 수락: {}", appointmentId, currentUserId, accept);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "appointmentId", appointmentId,
                "accepted", accept,
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("초대 응답 실패: 약속 ID {}, 오류: {}", appointmentId, e.getMessage());
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "응답 처리 중 오류가 발생했습니다.",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}