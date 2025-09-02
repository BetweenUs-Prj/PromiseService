package com.promiseservice.repository;

import com.promiseservice.model.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 약속 참가자 데이터 접근을 위한 리포지토리
 * 이유: 약속 참가자 엔티티의 CRUD 작업을 위한 데이터 접근 계층을 제공하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    /**
     * 특정 약속의 참가자 목록을 조회
     * 이유: 약속에 참가한 사용자들의 목록을 조회하기 위해
     *
     * @param meetingId 약속 ID
     * @return 참가자 목록
     */
    List<MeetingParticipant> findByMeetingId(Long meetingId);

    /**
     * 특정 사용자가 참가한 약속 목록을 조회
     * 이유: 사용자가 참가한 약속들을 조회하기 위해
     *
     * @param userId 사용자 ID
     * @return 참가한 약속 목록
     */
    List<MeetingParticipant> findByUserId(Long userId);

    /**
     * 특정 약속과 사용자로 참가자 정보를 조회
     * 이유: 특정 약속에 특정 사용자가 참가하고 있는지 확인하기 위해
     *
     * @param meetingId 약속 ID
     * @param userId 사용자 ID
     * @return 참가자 정보 (Optional)
     */
    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);


    /**
     * 특정 약속의 특정 응답을 가진 참가자 목록을 조회
     * 이유: 특정 응답 상태의 참가자들을 조회하기 위해
     *
     * @param meetingId 약속 ID
     * @param response 참가자 응답 상태
     * @return 해당 응답 상태의 참가자 목록
     */
    List<MeetingParticipant> findByMeetingIdAndResponse(Long meetingId, String response);
}
