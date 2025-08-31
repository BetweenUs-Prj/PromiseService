package com.promiseservice.controller;

import com.promiseservice.entity.Appointment;
import com.promiseservice.entity.AppointmentParticipant;
import com.promiseservice.enums.AppointmentStatus;
import com.promiseservice.enums.ParticipantState;
import com.promiseservice.enums.NotifyStatus;
import com.promiseservice.repository.AppointmentRepository;
import com.promiseservice.repository.AppointmentParticipantRepository;
import com.promiseservice.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 약속 관리 REST API 컨트롤러
 * 이유: 약속 생성, 조회, 참여자 관리 등의 기능을 제공하여
 * 사용자가 친구들과 약속을 만들고 관리할 수 있도록 지원하기 위해
 */
@Slf4j
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentParticipantRepository appointmentParticipantRepository;
    private final FriendService friendService;

    /**
     * 새 약속 생성
     * 이유: 호스트가 새로운 약속을 생성하고 친구들을 초대할 수 있도록 하기 위해
     * 
     * @param request HTTP 요청 (현재 사용자 정보 확인용)
     * @param appointmentData 약속 생성 데이터
     * @return 생성된 약속 정보
     */
    @PostMapping
    public ResponseEntity<?> createAppointment(
            HttpServletRequest request,
            @RequestBody Map<String, Object> appointmentData) {
        
        try {
            Long currentUserId = (Long) request.getAttribute("userId");
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            // 약속 정보 생성
            // 이유: 사용자가 입력한 정보로 새 약속을 생성하기 위해
            Appointment appointment = new Appointment();
            appointment.setTitle((String) appointmentData.get("title"));
            appointment.setPlace((String) appointmentData.get("place"));
            appointment.setRecommendedPlace((String) appointmentData.get("recommendedPlace"));
            appointment.setHostUserId(currentUserId);
            appointment.setStatus(AppointmentStatus.DRAFT);
            
            // 좌표 정보 설정
            // 이유: 지도 서비스 연동을 위해 정확한 위치 정보를 저장하기 위해
            if (appointmentData.get("latitude") != null) {
                appointment.setLatitude(((Number) appointmentData.get("latitude")).doubleValue());
            }
            if (appointmentData.get("longitude") != null) {
                appointment.setLongitude(((Number) appointmentData.get("longitude")).doubleValue());
            }
            
            // 최대 인원 설정
            // 이루: 약속 규모를 관리하기 위해
            if (appointmentData.get("maxParticipants") != null) {
                appointment.setMaxParticipants(((Number) appointmentData.get("maxParticipants")).intValue());
            }

            // 약속 시간 설정
            // 이유: 약속 일시를 정확히 저장하기 위해
            if (appointmentData.get("startAt") != null) {
                String startAtStr = (String) appointmentData.get("startAt");
                appointment.setStartAt(Instant.parse(startAtStr));
            }

            // 상세 URL 설정 (선택적)
            // 이유: 추가 정보나 외부 링크 제공을 위해
            if (appointmentData.get("detailUrl") != null) {
                appointment.setDetailUrl((String) appointmentData.get("detailUrl"));
            }

            Appointment savedAppointment = appointmentRepository.save(appointment);

            // 호스트를 자동으로 참여자로 추가
            // 이유: 약속을 생성한 호스트는 자동으로 참여자가 되어야 하기 위해
            AppointmentParticipant hostParticipant = new AppointmentParticipant();
            hostParticipant.setAppointmentId(savedAppointment.getId());
            hostParticipant.setUserId(currentUserId);
            hostParticipant.setState(ParticipantState.ACCEPTED);
            hostParticipant.setNotifyStatus(NotifyStatus.PENDING);
            appointmentParticipantRepository.save(hostParticipant);

            log.info("약속 생성 완료: {} (호스트: {})", savedAppointment.getId(), currentUserId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "appointment", savedAppointment,
                "message", "약속이 생성되었습니다.",
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("약속 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "약속 생성 중 오류가 발생했습니다.",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 약속에 친구 초대
     * 이유: 호스트가 친구 목록에서 참여자를 선택하여 약속에 초대할 수 있도록 하기 위해
     * 
     * @param appointmentId 약속 ID
     * @param inviteData 초대할 친구 정보
     * @param request HTTP 요청 (현재 사용자 정보 확인용)
     * @return 초대 결과
     */
    @PostMapping("/{appointmentId}/invite")
    public ResponseEntity<?> inviteFriend(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, Object> inviteData,
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

            Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("약속을 찾을 수 없습니다."));

            // 호스트 권한 확인
            // 이유: 약속 생성자만 참여자를 초대할 수 있도록 권한을 제어하기 위해
            if (!appointment.getHostUserId().equals(currentUserId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "호스트만 참여자를 초대할 수 있습니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            Long friendUserId = ((Number) inviteData.get("friendUserId")).longValue();

            // 친구 관계 확인
            // 이유: 친구가 아닌 사용자를 초대하는 것을 방지하기 위해
            if (!friendService.areFriends(currentUserId, friendUserId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "친구만 초대할 수 있습니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            // 이미 초대된 참여자인지 확인
            // 이유: 중복 초대를 방지하기 위해
            boolean alreadyInvited = appointmentParticipantRepository
                .existsByAppointmentIdAndUserId(appointmentId, friendUserId);
            
            if (alreadyInvited) {
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "이미 초대된 참여자입니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            // 최대 인원 확인
            // 이유: 약속에 설정된 최대 인원을 초과하지 않도록 제한하기 위해
            if (appointment.getMaxParticipants() != null) {
                long currentParticipants = appointmentParticipantRepository.countByAppointmentId(appointmentId);
                if (currentParticipants >= appointment.getMaxParticipants()) {
                    return ResponseEntity.status(400).body(Map.of(
                        "success", false,
                        "message", "최대 참여 인원을 초과했습니다.",
                        "timestamp", System.currentTimeMillis()
                    ));
                }
            }

            // 참여자 초대
            // 이유: 새로운 참여자를 약속에 추가하기 위해
            AppointmentParticipant participant = new AppointmentParticipant();
            participant.setAppointmentId(appointmentId);
            participant.setUserId(friendUserId);
            participant.setState(ParticipantState.INVITED);
            participant.setNotifyStatus(NotifyStatus.PENDING);
            appointmentParticipantRepository.save(participant);

            log.info("참여자 초대 완료: 약속 {} -> 사용자 {}", appointmentId, friendUserId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "친구를 초대했습니다.",
                "appointmentId", appointmentId,
                "invitedUserId", friendUserId,
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("참여자 초대 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "초대 중 오류가 발생했습니다.",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 내가 생성한 약속 목록 조회
     * 이유: 사용자가 자신이 호스트인 약속들을 확인하고 관리할 수 있도록 하기 위해
     * 
     * @param request HTTP 요청 (현재 사용자 정보 확인용)
     * @return 약속 목록
     */
    @GetMapping("/my-hosted")
    public ResponseEntity<?> getMyHostedAppointments(HttpServletRequest request) {
        
        try {
            Long currentUserId = (Long) request.getAttribute("userId");
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            List<Appointment> appointments = appointmentRepository.findByHostUserIdOrderByStartAtDesc(currentUserId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "appointments", appointments,
                "count", appointments.size(),
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("내 호스팅 약속 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "약속 목록 조회 중 오류가 발생했습니다.",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 내가 참여한 약속 목록 조회
     * 이유: 사용자가 자신이 참여하는 약속들을 확인할 수 있도록 하기 위해
     * 
     * @param request HTTP 요청 (현재 사용자 정보 확인용)
     * @return 약속 목록
     */
    @GetMapping("/my-participated")
    public ResponseEntity<?> getMyParticipatedAppointments(HttpServletRequest request) {
        
        try {
            Long currentUserId = (Long) request.getAttribute("userId");
            if (currentUserId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다.",
                    "timestamp", System.currentTimeMillis()
                ));
            }

            List<Appointment> appointments = appointmentRepository.findAppointmentsByParticipantUserId(currentUserId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "appointments", appointments,
                "count", appointments.size(),
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("내 참여 약속 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "약속 목록 조회 중 오류가 발생했습니다.",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}




