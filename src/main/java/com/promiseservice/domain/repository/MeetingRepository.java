package com.promiseservice.domain.repository;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.entity.Meeting.MeetingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // 방장이 생성한 약속 목록 조회
    List<Meeting> findByHostIdOrderByCreatedAtDesc(Long hostId);

    // 사용자가 참여한 약속 목록 조회
    @Query("SELECT DISTINCT m FROM Meeting m " +
           "JOIN m.participants p " +
           "WHERE p.userId = :userId " +
           "ORDER BY m.meetingTime DESC")
    List<Meeting> findMeetingsByParticipantUserId(@Param("userId") Long userId);

    // 특정 상태의 약속 목록 조회
    List<Meeting> findByStatusOrderByMeetingTimeAsc(MeetingStatus status);

    // 특정 기간의 약속 목록 조회
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.meetingTime BETWEEN :startTime AND :endTime " +
           "ORDER BY m.meetingTime ASC")
    List<Meeting> findMeetingsByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // 페이지네이션을 위한 약속 목록 조회
    Page<Meeting> findByHostId(Long hostId, Pageable pageable);

    // 제목으로 약속 검색
    List<Meeting> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    // 특정 장소 근처의 약속 목록 조회 (좌표 기반)
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.locationCoordinates IS NOT NULL " +
           "AND m.meetingTime >= :currentTime " +
           "ORDER BY m.meetingTime ASC")
    List<Meeting> findUpcomingMeetingsWithLocation(@Param("currentTime") LocalDateTime currentTime);

    // 특정 상태의 약속 수 조회
    long countByStatus(MeetingStatus status);

    // 특정 상태의 약속 목록 조회 (페이지네이션 지원)
    // 이유: 상태별 필터링과 페이지네이션을 동시에 지원하여 대량의 약속 데이터를 효율적으로 조회하기 위해
    Page<Meeting> findByStatusOrderByMeetingTimeAsc(MeetingStatus status, Pageable pageable);

    // 제목으로 약속 검색 (페이지네이션 지원)
    // 이유: 키워드 검색과 페이지네이션을 동시에 지원하여 검색 결과를 효율적으로 관리하기 위해
    Page<Meeting> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);

    // 장소명으로 약속 검색
    // 이유: 위치 기반 검색을 지원하여 사용자가 특정 지역의 약속을 찾을 수 있도록 하기 위해
    List<Meeting> findByLocationNameContainingIgnoreCase(String locationName);
    
    /**
     * 시간 범위로 약속 검색
     * 이유: 특정 기간 내의 약속들을 조회하여 일정 관리를 지원
     */
    List<Meeting> findByMeetingTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
