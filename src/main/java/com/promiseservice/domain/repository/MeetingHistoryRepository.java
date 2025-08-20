package com.promiseservice.domain.repository;

import com.promiseservice.domain.entity.MeetingHistory;
import com.promiseservice.domain.entity.MeetingHistory.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingHistoryRepository extends JpaRepository<MeetingHistory, Long> {

    // 특정 약속의 히스토리 조회
    List<MeetingHistory> findByMeetingIdOrderByTimestampDesc(Long meetingId);

    // 특정 사용자의 히스토리 조회
    List<MeetingHistory> findByUserIdOrderByTimestampDesc(Long userId);

    // 특정 액션 타입의 히스토리 조회
    List<MeetingHistory> findByActionOrderByTimestampDesc(ActionType action);

    // 특정 기간의 히스토리 조회
    @Query("SELECT h FROM MeetingHistory h " +
           "WHERE h.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY h.timestamp DESC")
    List<MeetingHistory> findByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // 특정 약속의 특정 액션 히스토리 조회
    List<MeetingHistory> findByMeetingIdAndActionOrderByTimestampDesc(Long meetingId, ActionType action);

    // 특정 사용자의 특정 약속 히스토리 조회
    List<MeetingHistory> findByUserIdAndMeetingIdOrderByTimestampDesc(Long userId, Long meetingId);
}

