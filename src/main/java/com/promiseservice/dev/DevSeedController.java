package com.promiseservice.dev;

import com.promiseservice.entity.Appointment;
import com.promiseservice.entity.AppointmentParticipant;
import com.promiseservice.repository.AppointmentRepository;
import com.promiseservice.repository.AppointmentParticipantRepository;
import com.promiseservice.enums.AppointmentStatus;
import com.promiseservice.enums.ParticipantState;
import com.promiseservice.enums.NotifyStatus;
import com.promiseservice.service.AppointmentNotifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/dev")
public class DevSeedController {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentParticipantRepository participantRepository;
    private final AppointmentNotifyService notifyService;

    public DevSeedController(AppointmentRepository appointmentRepository,
                             AppointmentParticipantRepository participantRepository,
                             AppointmentNotifyService notifyService) {
        this.appointmentRepository = appointmentRepository;
        this.participantRepository = participantRepository;
        this.notifyService = notifyService;
    }

    /** 약속 하나 빠르게 만들어서 ID만 돌려줌 */
    @PostMapping("/seed-appointment")
    public Map<String, Object> seedAppointment(HttpServletRequest req) {
        Long currentUserId = (Long) req.getAttribute("userId"); // 필터에서 넣어둔 값
        if (currentUserId == null) currentUserId = 1L; // 기본값

        Instant now = Instant.now();
        Appointment a = new Appointment();
        a.setTitle("개발용 테스트 약속");
        a.setPlace("카페");
        a.setHostUserId(currentUserId);   // ★ 현재 사용자 = 호스트
        a.setStartAt(now.plus(30, ChronoUnit.MINUTES));
        a.setRemindAt(now.plus(20, ChronoUnit.MINUTES));
        a.setStatus(AppointmentStatus.DRAFT);
        a.setSent(false);
        a.setDetailUrl("https://example.com/detail");

        appointmentRepository.save(a);

        // ★ 호스트를 자동으로 참여자로 추가 (수락 상태)
        // 이유: 개발 테스트 시 호스트가 자동으로 참여자로 포함되어 알림 발송 테스트가 가능하도록 지원
        ensureHostIsParticipant(a.getId(), currentUserId);

        return Map.of(
                "success", true,
                "appointmentId", a.getId(),
                "hostUserId", currentUserId,
                "message", "호스트가 자동으로 참여자로 추가되었습니다"
        );
    }

    /** 방금 만든 약속을 곧바로 확정+알림 */
    @PostMapping("/seed-and-confirm")
    public Map<String, Object> seedAndConfirm(HttpServletRequest req) {
        // 1) 약속 생성
        var seeded = seedAppointment(req);
        Long id = ((Number) seeded.get("appointmentId")).longValue();
        Long currentUserId = (Long) seeded.get("hostUserId");

        // 2) 즉시 확정+알림 (같은 사용자이므로 권한 문제 없음)
        notifyService.confirmAndNotify(id, currentUserId);

        return Map.of(
                "success", true,
                "appointmentId", id,
                "hostUserId", currentUserId,
                "message", "약속 생성 및 즉시 확정 완료! 카카오톡을 확인해보세요! 📱"
        );
    }

    /** 현재 JWT에서 추출된 사용자 정보 확인용 */
    @GetMapping("/whoami")
    public Map<String, Object> whoami(HttpServletRequest req) {
        // 이유: 현재 JWT 토큰에서 추출되는 사용자 ID와 권한 정보를 확인하여 테스트 시 참고하기 위해
        
        Long userId = (Long) req.getAttribute("userId");
        String userRole = (String) req.getAttribute("userRole");
        Boolean authenticated = (Boolean) req.getAttribute("authenticated");
        
        // Authorization 헤더도 함께 표시 (디버깅용)
        String authHeader = req.getHeader("Authorization");
        String tokenPreview = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 토큰의 앞 20자와 뒤 10자만 표시 (보안상)
            if (token.length() > 30) {
                tokenPreview = token.substring(0, 20) + "..." + token.substring(token.length() - 10);
            } else {
                tokenPreview = token.substring(0, Math.min(10, token.length())) + "...";
            }
        }

        return Map.of(
                "userId", userId != null ? userId : "null",
                "userRole", userRole != null ? userRole : "null",
                "authenticated", authenticated != null ? authenticated : false,
                "tokenPreview", tokenPreview != null ? tokenPreview : "no token",
                "message", "이 userId가 약속의 hostUserId로 설정됩니다"
        );
    }

    /**
     * 호스트를 해당 약속의 참여자로 자동 추가하는 헬퍼 메서드
     * 이유: 개발 및 테스트 환경에서 호스트가 자동으로 참여자가 되어 알림 수신 테스트가 가능하도록 지원하기 위해
     * 
     * @param appointmentId 약속 ID
     * @param hostUserId 호스트 사용자 ID
     */
    private void ensureHostIsParticipant(Long appointmentId, Long hostUserId) {
        // 이미 참여자로 등록되어 있는지 확인
        // 이유: 중복 참여자 등록을 방지하고 데이터 무결성 유지
        boolean alreadyParticipant = participantRepository.existsByAppointmentIdAndUserId(appointmentId, hostUserId);
        
        if (alreadyParticipant) {
            return; // 이미 참여자로 등록되어 있음
        }

        // 호스트를 수락 상태의 참여자로 추가
        // 이유: 호스트는 자신의 약속이므로 자동으로 수락 상태로 설정하여 알림 대상에 포함
        AppointmentParticipant hostParticipant = new AppointmentParticipant();
        hostParticipant.setAppointmentId(appointmentId);
        hostParticipant.setUserId(hostUserId);
        hostParticipant.setKakaoId(hostUserId);
        hostParticipant.setState(ParticipantState.ACCEPTED);       // 호스트는 자동 수락
        hostParticipant.setNotifyStatus(NotifyStatus.PENDING);     // 알림 대기 상태
        hostParticipant.setCreatedAt(Instant.now());
        hostParticipant.setUpdatedAt(Instant.now());

        participantRepository.save(hostParticipant);
    }
}