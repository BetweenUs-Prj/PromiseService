package com.promiseservice.service;

import com.promiseservice.entity.Appointment;
import com.promiseservice.entity.AppointmentParticipant;
import com.promiseservice.enums.AppointmentStatus;
import com.promiseservice.enums.ParticipantState;
import com.promiseservice.repository.AppointmentRepository;
import com.promiseservice.repository.AppointmentParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 약속 확정 및 알림 발송을 담당하는 비즈니스 서비스
 * 이유: 약속 확정 프로세스와 참여자들에게 카카오톡 알림을 발송하는 비즈니스 로직을 관리하여
 * 데이터 일관성을 보장하고 간단한 알림 발송을 지원하기 위해
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentNotifyService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentParticipantRepository participantRepository;


    /**
     * 약속 확정 및 참여자 전원에게 알림 발송
     * 이유: 호스트가 약속을 확정할 때 상태를 변경하고 수락한 참여자들에게
     * 확정 알림을 발송하여 모든 참여자가 약속 확정 소식을 받을 수 있도록 지원하기 위해
     * 
     * @param appointmentId 확정할 약속 ID
     * @param hostUserId 요청한 사용자 ID (호스트 권한 확인용)
     * @return 처리 결과 메시지
     */
    @Transactional
    public String confirmAndNotify(Long appointmentId, Long hostUserId) {
        
        log.info("약속 확정 및 알림 발송 시작 - 약속 ID: {}, 호스트 ID: {}", appointmentId, hostUserId);

        // 약속 정보 조회 및 검증
        // 이유: 존재하는 약속인지 확인하고 호스트 권한을 검증하기 위해
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("약속을 찾을 수 없습니다: " + appointmentId));

        // 호스트 권한 확인
        // 이유: 약속을 생성한 호스트만 확정할 수 있도록 권한을 제한하여 보안성 확보
        if (hostUserId != null && appointment.getHostUserId() != null 
                && !appointment.getHostUserId().equals(hostUserId)) {
            throw new RuntimeException("FORBIDDEN: 약속을 확정할 권한이 없습니다. 호스트만 확정할 수 있습니다.");
        }

        // 이미 확정된 약속인지 확인
        // 이유: 중복 확정 처리를 방지하고 불필요한 알림 발송을 막기 위해
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            log.info("이미 확정된 약속 - 약속 ID: {}", appointmentId);
            return "이미 확정된 약속입니다.";
        }

        // 약속 상태를 확정으로 변경
        // 이유: 약속이 최종 확정되었음을 시스템과 사용자들에게 알리기 위해
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        // 수락한 참여자들에게 카카오톡 알림 발송
        // 이유: 약속 확정 소식을 실제 참여할 예정인 참여자들에게 전달하기 위해
        List<AppointmentParticipant> acceptedParticipants = 
            participantRepository.findByAppointmentIdAndState(appointmentId, ParticipantState.ACCEPTED);

        int successCount = 0;
        int failureCount = 0;

        for (AppointmentParticipant participant : acceptedParticipants) {
            try {
                // TODO: 실제 카카오톡 발송 로직 구현 필요
                // 현재는 로그만 출력
                log.info("약속 확정 알림 발송 대상: 사용자 ID {}", participant.getUserId());
                successCount++;
            } catch (Exception e) {
                log.error("알림 발송 실패 - 사용자 ID: {}, 오류: {}", participant.getUserId(), e.getMessage());
                failureCount++;
            }
        }

        String result = String.format("약속이 확정되었습니다. 알림 발송: 성공 %d건, 실패 %d건", 
                                    successCount, failureCount);
        log.info("약속 확정 완료 - 약속 ID: {}, 결과: {}", appointmentId, result);
        
        return result;
    }

    /**
     * 약속 취소 및 참여자들에게 취소 알림 발송
     * 이유: 호스트가 약속을 취소할 때 상태를 변경하고 참여자들에게
     * 취소 알림을 발송하여 일정 변경을 알리기 위해
     * 
     * @param appointmentId 취소할 약속 ID
     * @param hostUserId 요청한 사용자 ID (호스트 권한 확인용)
     * @return 처리 결과 메시지
     */
    @Transactional
    public String cancelAppointment(Long appointmentId, Long hostUserId) {
        
        log.info("약속 취소 시작 - 약속 ID: {}, 호스트 ID: {}", appointmentId, hostUserId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("약속을 찾을 수 없습니다: " + appointmentId));

        // 호스트 권한 확인
        if (hostUserId != null && appointment.getHostUserId() != null 
                && !appointment.getHostUserId().equals(hostUserId)) {
            throw new RuntimeException("FORBIDDEN: 약속을 취소할 권한이 없습니다. 호스트만 취소할 수 있습니다.");
        }

        // 약속 상태를 취소로 변경
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);

        // 모든 참여자들에게 취소 알림 발송
        List<AppointmentParticipant> allParticipants = 
            participantRepository.findByAppointmentId(appointmentId);

        int successCount = 0;
        int failureCount = 0;

        for (AppointmentParticipant participant : allParticipants) {
            try {
                // TODO: 실제 카카오톡 발송 로직 구현 필요
                log.info("약속 취소 알림 발송 대상: 사용자 ID {}", participant.getUserId());
                successCount++;
            } catch (Exception e) {
                log.error("취소 알림 발송 실패 - 사용자 ID: {}, 오류: {}", participant.getUserId(), e.getMessage());
                failureCount++;
            }
        }

        String result = String.format("약속이 취소되었습니다. 알림 발송: 성공 %d건, 실패 %d건", 
                                    successCount, failureCount);
        log.info("약속 취소 완료 - 약속 ID: {}, 결과: {}", appointmentId, result);
        
        return result;
    }

    /**
     * 참여자 응답 처리 (수락/거절)
     * 이유: 초대받은 참여자가 약속에 대해 수락 또는 거절 응답을 할 수 있도록 하기 위해
     * 
     * @param appointmentId 약속 ID
     * @param userId 참여자 사용자 ID
     * @param accept 수락 여부 (true: 수락, false: 거절)
     * @return 처리 결과 메시지
     */
    @Transactional
    public String respondToInvitation(Long appointmentId, Long userId, boolean accept) {
        
        log.info("참여자 응답 처리 - 약속 ID: {}, 사용자 ID: {}, 수락: {}", appointmentId, userId, accept);

        AppointmentParticipant participant = participantRepository
            .findByAppointmentIdAndUserId(appointmentId, userId)
            .orElseThrow(() -> new IllegalArgumentException("참여자 정보를 찾을 수 없습니다."));

        if (participant.getState() != ParticipantState.INVITED) {
            throw new IllegalArgumentException("이미 응답한 초대입니다.");
        }

        // 참여자 상태 변경
        participant.setState(accept ? ParticipantState.ACCEPTED : ParticipantState.DECLINED);
        participantRepository.save(participant);

        String result = accept ? "약속 참여를 수락했습니다." : "약속 참여를 거절했습니다.";
        log.info("참여자 응답 완료 - {}", result);
        
        return result;
    }
}