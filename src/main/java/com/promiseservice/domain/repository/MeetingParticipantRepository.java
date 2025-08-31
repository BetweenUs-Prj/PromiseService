package com.promiseservice.domain.repository;

import com.promiseservice.domain.entity.MeetingParticipant;
import com.promiseservice.domain.entity.MeetingParticipant.ResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MeetingParticipant 엔티티를 위한 JPA Repository
 * 이유: 약속 참여자 정보에 대한 데이터베이스 접근 계층을 제공하여 참여자 관리 기능을 구현하기 위해
 */
@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    /**
     * 특정 약속의 참여자 목록 조회
     * 이유: 약속 참여자들의 상태를 확인하고 관리하기 위해
     */
    List<MeetingParticipant> findByMeetingId(Long meetingId);

    /**
     * 특정 사용자의 특정 약속 참여 정보 조회
     * 이유: 사용자의 특정 약속에 대한 참여 상태를 확인하기 위해
     */
    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

    /**
     * 특정 사용자가 참여한 모든 약속의 참여 정보 조회
     * 이유: 사용자의 모든 약속 참여 이력을 조회하기 위해
     */
    List<MeetingParticipant> findByUserId(Long userId);

    /**
     * 특정 응답 상태의 참여자 목록 조회
     * 이유: 초대됨, 수락됨, 거부됨 등 상태별 참여자를 관리하기 위해
     */
    List<MeetingParticipant> findByMeetingIdAndResponse(Long meetingId, ResponseStatus response);

    /**
     * 특정 약속의 수락한 참여자 수 조회
     * 이유: 실제 참여자 수를 확인하여 최대 인원 제한 검증에 활용
     */
    @Query("SELECT COUNT(p) FROM MeetingParticipant p " +
           "WHERE p.meetingId = :meetingId AND p.response = 'ACCEPTED'")
    long countAcceptedParticipantsByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 특정 약속의 초대된 참여자 수 조회
     * 이루: 초대 상태인 참여자 수를 확인하여 알림 및 관리에 활용
     */
    @Query("SELECT COUNT(p) FROM MeetingParticipant p " +
           "WHERE p.meetingId = :meetingId AND p.response = 'INVITED'")
    long countInvitedParticipantsByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 특정 약속의 거부한 참여자 수 조회
     * 이유: 거부 상태인 참여자 수를 확인하여 통계 및 분석에 활용
     */
    @Query("SELECT COUNT(p) FROM MeetingParticipant p " +
           "WHERE p.meetingId = :meetingId AND p.response = 'REJECTED'")
    long countRejectedParticipantsByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 특정 약속의 전체 참여자 수 조회
     * 이유: 총 초대된 참여자 수를 확인하기 위해
     */
    long countByMeetingId(Long meetingId);

    /**
     * 특정 사용자의 수락한 약속 목록 조회
     * 이유: 사용자가 실제로 참여할 약속들만 조회하기 위해
     */
    @Query("SELECT p FROM MeetingParticipant p " +
           "WHERE p.userId = :userId AND p.response = 'ACCEPTED' " +
           "ORDER BY p.meeting.meetingTime ASC")
    List<MeetingParticipant> findAcceptedMeetingsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 초대받은 약속 목록 조회
     * 이유: 사용자가 응답하지 않은 초대를 확인하기 위해
     */
    @Query("SELECT p FROM MeetingParticipant p " +
           "WHERE p.userId = :userId AND p.response = 'INVITED' " +
           "ORDER BY p.invitedAt DESC")
    List<MeetingParticipant> findPendingInvitationsByUserId(@Param("userId") Long userId);

    /**
     * 특정 약속의 방장 정보 조회
     * 이유: 가장 먼저 초대된(생성한) 참여자를 방장으로 판단
     */
    @Query("SELECT p FROM MeetingParticipant p " +
           "WHERE p.meetingId = :meetingId " +
           "AND p.invitedAt = (SELECT MIN(p2.invitedAt) FROM MeetingParticipant p2 WHERE p2.meetingId = :meetingId)")
    Optional<MeetingParticipant> findHostByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 특정 약속에서 특정 사용자가 방장인지 확인
     * 이유: 권한 검증을 위해 사용자가 방장인지 확인
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM MeetingParticipant p " +
           "WHERE p.meetingId = :meetingId AND p.userId = :userId " +
           "AND p.invitedAt = (SELECT MIN(p2.invitedAt) FROM MeetingParticipant p2 WHERE p2.meetingId = :meetingId)")
    boolean isHost(@Param("meetingId") Long meetingId, @Param("userId") Long userId);

    /**
     * 특정 약속의 초대 순서대로 참여자 목록 조회
     * 이유: 초대 순서를 유지하여 방장 및 참여자 순서를 관리하기 위해
     */
    List<MeetingParticipant> findByMeetingIdOrderByInvitedAtAsc(Long meetingId);

    /**
     * 사용자가 참여한 특정 상태의 약속 수 조회
     * 이유: 사용자의 약속 참여 통계를 제공하기 위해
     */
    @Query("SELECT COUNT(p) FROM MeetingParticipant p " +
           "WHERE p.userId = :userId AND p.response = :response")
    long countByUserIdAndResponse(@Param("userId") Long userId, @Param("response") ResponseStatus response);

    /**
     * 특정 사용자가 방장인 약속의 참여자 목록 조회
     * 이유: 방장이 관리하는 약속들의 참여자를 조회하기 위해
     */
    @Query("SELECT p FROM MeetingParticipant p " +
           "WHERE p.meetingId IN (" +
           "  SELECT p2.meetingId FROM MeetingParticipant p2 " +
           "  WHERE p2.userId = :hostId " +
           "  AND p2.invitedAt = (SELECT MIN(p3.invitedAt) FROM MeetingParticipant p3 WHERE p3.meetingId = p2.meetingId)" +
           ") " +
           "ORDER BY p.meetingId DESC, p.invitedAt ASC")
    List<MeetingParticipant> findParticipantsByHostId(@Param("hostId") Long hostId);

    /**
     * 실제 참여한 참여자들만 조회 (joinedAt이 있는 경우)
     * 이유: 약속에 실제로 참석한 참여자 통계를 위해
     */
    @Query("SELECT p FROM MeetingParticipant p " +
           "WHERE p.meetingId = :meetingId AND p.joinedAt IS NOT NULL " +
           "ORDER BY p.joinedAt ASC")
    List<MeetingParticipant> findActualParticipantsByMeetingId(@Param("meetingId") Long meetingId);
}