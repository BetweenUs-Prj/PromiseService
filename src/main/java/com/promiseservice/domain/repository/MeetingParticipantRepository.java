package com.promiseservice.domain.repository;

import com.promiseservice.domain.entity.MeetingParticipant;
import com.promiseservice.domain.entity.MeetingParticipant.ResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    // 특정 약속의 참여자 목록 조회
    List<MeetingParticipant> findByMeetingId(Long meetingId);

    // 특정 사용자의 특정 약속 참여 정보 조회
    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

    // 특정 사용자가 참여한 모든 약속의 참여 정보 조회
    List<MeetingParticipant> findByUserId(Long userId);

    // 특정 응답 상태의 참여자 목록 조회
    List<MeetingParticipant> findByMeetingIdAndResponse(Long meetingId, ResponseStatus response);

    // 특정 약속의 수락한 참여자 수 조회
    @Query("SELECT COUNT(p) FROM MeetingParticipant p " +
           "WHERE p.meeting.id = :meetingId AND p.response = 'ACCEPTED'")
    long countAcceptedParticipantsByMeetingId(@Param("meetingId") Long meetingId);

    // 특정 약속의 초대된 참여자 수 조회
    @Query("SELECT COUNT(p) FROM MeetingParticipant p " +
           "WHERE p.meeting.id = :meetingId AND p.response = 'INVITED'")
    long countInvitedParticipantsByMeetingId(@Param("meetingId") Long meetingId);

    // 특정 사용자가 방장인 약속의 참여자 목록 조회
    @Query("SELECT p FROM MeetingParticipant p " +
           "JOIN p.meeting m " +
           "WHERE m.hostId = :hostId " +
           "ORDER BY p.meeting.id DESC, p.invitedAt ASC")
    List<MeetingParticipant> findParticipantsByHostId(@Param("hostId") Long hostId);

    // 특정 약속의 참여자 수 조회
    long countByMeetingId(Long meetingId);
}
