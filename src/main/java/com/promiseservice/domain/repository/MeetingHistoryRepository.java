package com.promiseservice.domain.repository;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.entity.MeetingHistory;
import com.promiseservice.domain.entity.MeetingHistory.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MeetingHistory 엔티티를 위한 JPA Repository
 * 이유: 약속 히스토리 정보에 대한 데이터베이스 접근 계층을 제공하여 활동 추적 및 알림 기능을 구현하기 위해
 */
@Repository
public interface MeetingHistoryRepository extends JpaRepository<MeetingHistory, Long> {

    /**
     * 특정 약속의 히스토리 조회 (최신 순)
     * 이유: 약속의 모든 활동 이력을 시간 순으로 조회하여 사용자에게 타임라인 제공
     */
    List<MeetingHistory> findByMeetingOrderByTimestampDesc(Meeting meeting);

    /**
     * 특정 사용자의 히스토리 조회 (최신 순)
     * 이유: 특정 사용자가 수행한 모든 활동을 추적하기 위해
     */
    List<MeetingHistory> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * 특정 액션 타입의 히스토리 조회 (최신 순)
     * 이유: 특정 타입의 활동만 필터링하여 분석 및 통계에 활용
     */
    List<MeetingHistory> findByActionOrderByTimestampDesc(ActionType action);

    /**
     * 특정 기간의 히스토리 조회
     * 이유: 특정 기간 동안의 활동을 분석하여 통계 데이터 제공
     */
    @Query("SELECT h FROM MeetingHistory h " +
           "WHERE h.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY h.timestamp DESC")
    List<MeetingHistory> findByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 특정 약속의 특정 액션 히스토리 조회
     * 이유: 약속에서 특정 액션만 조회하여 세부 분석 지원
     */
    List<MeetingHistory> findByMeetingAndActionOrderByTimestampDesc(Meeting meeting, ActionType action);

    /**
     * 특정 사용자의 특정 약속 히스토리 조회
     * 이유: 사용자가 특정 약속에서 수행한 활동만 조회
     */
    List<MeetingHistory> findByUserIdAndMeetingOrderByTimestampDesc(Long userId, Meeting meeting);

    /**
     * 특정 약속의 히스토리 조회 (페이지네이션 지원)
     * 이유: 대량의 히스토리 데이터를 효율적으로 조회하기 위해
     */
    Page<MeetingHistory> findByMeetingOrderByTimestampDesc(Meeting meeting, Pageable pageable);

    /**
     * 특정 사용자의 최근 활동 조회
     * 이유: 사용자의 최근 활동을 제한된 개수로 조회하여 빠른 피드백 제공
     */
    @Query("SELECT h FROM MeetingHistory h " +
           "WHERE h.userId = :userId " +
           "ORDER BY h.timestamp DESC")
    List<MeetingHistory> findRecentActivitiesByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 액션 타입의 발생 횟수 조회
     * 이유: 각 액션 타입별 통계 정보를 제공하기 위해
     */
    long countByAction(ActionType action);

    /**
     * 특정 기간 동안 특정 액션 타입의 발생 횟수 조회
     * 이유: 기간별 활동 통계를 제공하기 위해
     */
    @Query("SELECT COUNT(h) FROM MeetingHistory h " +
           "WHERE h.action = :action " +
           "AND h.timestamp BETWEEN :startTime AND :endTime")
    long countByActionAndTimeRange(
        @Param("action") ActionType action,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 특정 사용자의 총 활동 횟수 조회
     * 이유: 사용자의 활동 통계를 제공하기 위해
     */
    long countByUserId(Long userId);

    /**
     * 특정 약속의 총 활동 횟수 조회
     * 이유: 약속의 활성도를 측정하기 위해
     */
    long countByMeeting(Meeting meeting);

    /**
     * 최근 생성된 약속들의 히스토리 조회
     * 이유: 최근 활동이 활발한 약속들을 추적하기 위해
     */
    @Query("SELECT h FROM MeetingHistory h " +
           "WHERE h.action = 'CREATED' " +
           "AND h.timestamp >= :since " +
           "ORDER BY h.timestamp DESC")
    List<MeetingHistory> findRecentCreatedMeetings(@Param("since") LocalDateTime since);

    /**
     * 특정 약속의 최근 활동 조회
     * 이유: 약속의 최근 변경 사항을 빠르게 확인하기 위해
     */
    @Query("SELECT h FROM MeetingHistory h " +
           "WHERE h.meeting.id = :meetingId " +
           "ORDER BY h.timestamp DESC")
    List<MeetingHistory> findRecentActivitiesByMeetingId(@Param("meetingId") Long meetingId, Pageable pageable);

    /**
     * 특정 사용자가 참여한 약속들의 모든 히스토리 조회
     * 이유: 사용자와 관련된 모든 약속 활동을 조회하기 위해
     */
    @Query("SELECT h FROM MeetingHistory h " +
           "WHERE h.meeting.id IN (" +
           "  SELECT p.meeting.id FROM MeetingParticipant p WHERE p.userId = :userId" +
           ") " +
           "ORDER BY h.timestamp DESC")
    List<MeetingHistory> findHistoriesByParticipantUserId(@Param("userId") Long userId);

    /**
     * 오늘 발생한 모든 활동 조회
     * 이유: 일일 활동 요약을 제공하기 위해
     */
    @Query("SELECT h FROM MeetingHistory h " +
           "WHERE CAST(h.timestamp AS DATE) = CAST(CURRENT_TIMESTAMP AS DATE) " +
           "ORDER BY h.timestamp DESC")
    List<MeetingHistory> findTodayActivities();
}