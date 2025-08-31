package com.promiseservice.repository;

import com.promiseservice.entity.AppointmentParticipant;
import com.promiseservice.enums.ParticipantState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 약속 참여자 정보 데이터 접근을 위한 Repository 인터페이스
 * 이유: 약속 참여자 데이터를 데이터베이스에서 조회, 저장, 수정하기 위한
 * 데이터 접근 계층을 제공하고, 기본적인 참여자 관리 기능을 지원하기 위해
 */
@Repository
public interface AppointmentParticipantRepository extends JpaRepository<AppointmentParticipant, Long> {

    /**
     * 특정 약속의 모든 참여자 조회
     * 이유: 호스트가 해당 약속의 전체 참여자 현황을 파악하고 관리할 수 있도록 지원하기 위해
     * 
     * @param appointmentId 약속 ID
     * @return 해당 약속의 모든 참여자 목록
     */
    List<AppointmentParticipant> findByAppointmentId(Long appointmentId);

    /**
     * 특정 약속의 특정 상태 참여자들 조회
     * 이유: 약속별로 수락, 거절, 대기 중인 참여자들을 구분하여 조회하고,
     * 상태별로 다른 처리 로직을 적용할 수 있도록 지원하기 위해
     * 
     * @param appointmentId 약속 ID
     * @param state 참여자 상태
     * @return 해당 약속의 특정 상태 참여자 목록
     */
    List<AppointmentParticipant> findByAppointmentIdAndState(Long appointmentId, ParticipantState state);

    /**
     * 특정 사용자의 모든 참여 약속 조회
     * 이유: 개별 사용자가 참여하고 있는 모든 약속을 조회하여
     * 개인별 일정 관리에 활용하기 위해
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 모든 참여 정보 목록
     */
    List<AppointmentParticipant> findByUserId(Long userId);

    /**
     * 특정 사용자의 특정 상태 참여 약속들 조회
     * 이유: 사용자별로 수락한 약속, 대기 중인 초대 등을 구분하여 조회하기 위해
     * 
     * @param userId 사용자 ID
     * @param state 참여자 상태
     * @return 해당 사용자의 특정 상태 참여 정보 목록
     */
    List<AppointmentParticipant> findByUserIdAndState(Long userId, ParticipantState state);

    /**
     * 특정 약속에서 특정 사용자 정보 조회
     * 이유: 약속과 사용자의 조합으로 고유한 참여자 정보를 조회하여
     * 중복 초대 방지나 개별 참여자 관리에 활용하기 위해
     * 
     * @param appointmentId 약속 ID
     * @param userId 사용자 ID
     * @return 해당 약속의 특정 사용자 참여 정보
     */
    Optional<AppointmentParticipant> findByAppointmentIdAndUserId(Long appointmentId, Long userId);

    /**
     * 특정 약속의 참여자 수 조회
     * 이유: 약속별 총 참여자 수를 빠르게 조회하여 용량 관리나 제한 검증에 활용하기 위해
     * 
     * @param appointmentId 약속 ID
     * @return 해당 약속의 총 참여자 수
     */
    long countByAppointmentId(Long appointmentId);

    /**
     * 특정 약속의 수락한 참여자 수 조회
     * 이유: 약속에 실제로 참여할 예정인 인원 수를 조회하여
     * 최종 참여 인원 확인과 장소 준비에 활용하기 위해
     * 
     * @param appointmentId 약속 ID
     * @param state 참여자 상태
     * @return 해당 약속의 특정 상태 참여자 수
     */
    long countByAppointmentIdAndState(Long appointmentId, ParticipantState state);

    /**
     * 특정 약속과 사용자 조합의 참여 정보 존재 여부 확인
     * 이유: 사용자가 특정 약속에 이미 참여하고 있는지 빠르게 확인하여
     * 중복 초대 방지와 권한 검증에 활용하기 위해
     * 
     * @param appointmentId 약속 ID
     * @param userId 사용자 ID
     * @return 해당 사용자의 참여 정보 존재 여부
     */
    boolean existsByAppointmentIdAndUserId(Long appointmentId, Long userId);

    /**
     * 특정 약속의 수락한 참여자들의 userId 목록 조회
     * 이유: 카카오톡 알림 요청 시 수락한 참여자들의 userId만 전달하여
     * 정확한 대상에게만 확정 알림을 발송하기 위해
     * 
     * @param appointmentId 약속 ID
     * @return 수락한 참여자들의 userId 목록
     */
    @Query("SELECT ap.userId FROM AppointmentParticipant ap WHERE ap.appointmentId = :appointmentId AND ap.state = 'ACCEPTED'")
    List<Long> findAcceptedUserIdsByAppointmentId(@Param("appointmentId") Long appointmentId);
}