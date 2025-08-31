package com.promiseservice.repository;

import com.promiseservice.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * 약속 정보 데이터 접근을 위한 Repository 인터페이스
 * 이유: 약속 데이터를 데이터베이스에서 조회, 저장, 수정하기 위한
 * 데이터 접근 계층을 제공하고, 알림 스케줄링에 필요한 쿼리 메서드들을 정의하기 위해
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {







    /**
     * 특정 호스트가 생성한 약속 목록 조회 (최신순)
     * 이유: 호스트가 자신이 생성한 약속들을 확인하고 관리할 수 있도록 하기 위해
     * 
     * @param hostUserId 호스트 사용자 ID
     * @return 해당 호스트가 생성한 약속 목록
     */
    List<Appointment> findByHostUserIdOrderByStartAtDesc(Long hostUserId);

    /**
     * 특정 사용자가 참여한 약속 목록 조회
     * 이유: 사용자가 참여 중인 모든 약속을 확인할 수 있도록 하기 위해
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자가 참여한 약속 목록
     */
    @Query("SELECT a FROM Appointment a JOIN AppointmentParticipant p ON a.id = p.appointmentId WHERE p.userId = :userId ORDER BY a.startAt DESC")
    List<Appointment> findAppointmentsByParticipantUserId(@Param("userId") Long userId);
}
