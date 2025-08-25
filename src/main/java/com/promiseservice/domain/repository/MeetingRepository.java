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

/**
 * Meeting 엔티티를 위한 JPA Repository
 * 이유: 약속 정보에 대한 데이터베이스 접근 계층을 제공하여 비즈니스 로직과 데이터 접근을 분리하기 위해
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /**
     * 특정 사용자가 방장인 약속 목록 조회
     * 이유: 가장 먼저 초대된 참여자가 방장이므로 해당 조건으로 조회
     */
    @Query("SELECT DISTINCT m FROM Meeting m " +
           "JOIN m.participants p " +
           "WHERE p.userId = :hostId " +
           "AND p.invitedAt = (SELECT MIN(p2.invitedAt) FROM MeetingParticipant p2 WHERE p2.meetingId = m.id) " +
           "ORDER BY m.createdAt DESC")
    List<Meeting> findMeetingsByHostId(@Param("hostId") Long hostId);

    /**
     * 사용자가 참여한 약속 목록 조회
     * 이유: 사용자가 참여한 모든 약속을 조회하여 개인 일정 관리를 지원
     */
    @Query("SELECT DISTINCT m FROM Meeting m " +
           "JOIN m.participants p " +
           "WHERE p.userId = :userId " +
           "ORDER BY m.meetingTime DESC")
    List<Meeting> findMeetingsByParticipantUserId(@Param("userId") Long userId);

    /**
     * 특정 상태의 약속 목록 조회
     * 이유: 약속 상태별로 필터링하여 관리 효율성 향상
     */
    List<Meeting> findByStatusOrderByMeetingTimeAsc(MeetingStatus status);

    /**
     * 특정 기간의 약속 목록 조회
     * 이유: 날짜 범위로 약속을 조회하여 일정 관리 지원
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.meetingTime BETWEEN :startTime AND :endTime " +
           "ORDER BY m.meetingTime ASC")
    List<Meeting> findMeetingsByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 특정 사용자가 방장인 약속 목록 조회 (페이지네이션 지원)
     * 이유: 대량의 데이터를 효율적으로 조회하기 위해
     */
    @Query("SELECT DISTINCT m FROM Meeting m " +
           "JOIN m.participants p " +
           "WHERE p.userId = :hostId " +
           "AND p.invitedAt = (SELECT MIN(p2.invitedAt) FROM MeetingParticipant p2 WHERE p2.meetingId = m.id)")
    Page<Meeting> findMeetingsByHostId(@Param("hostId") Long hostId, Pageable pageable);

    /**
     * 제목으로 약속 검색
     * 이유: 키워드 기반 검색을 지원하여 사용자 편의성 향상
     */
    List<Meeting> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    /**
     * 현재 진행 중인 약속 목록 조회 (위치 정보가 있는 약속)
     * 이유: 현재 진행 중이고 위치 정보가 있는 약속을 조회하여 위치 기반 서비스 지원
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.locationName IS NOT NULL " +
           "AND m.meetingTime >= :currentTime " +
           "ORDER BY m.meetingTime ASC")
    List<Meeting> findUpcomingMeetingsWithLocation(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 특정 상태의 약속 수 조회
     * 이유: 상태별 통계 정보 제공을 위해
     */
    long countByStatus(MeetingStatus status);

    /**
     * 특정 상태의 약속 목록 조회 (페이지네이션 지원)
     * 이유: 상태별 필터링과 페이지네이션을 동시에 지원하여 대량의 약속 데이터를 효율적으로 조회하기 위해
     */
    Page<Meeting> findByStatusOrderByMeetingTimeAsc(MeetingStatus status, Pageable pageable);

    /**
     * 제목으로 약속 검색 (페이지네이션 지원)
     * 이유: 키워드 검색과 페이지네이션을 동시에 지원하여 검색 결과를 효율적으로 관리하기 위해
     */
    Page<Meeting> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);

    /**
     * 장소명으로 약속 검색
     * 이유: 위치 기반 검색을 지원하여 사용자가 특정 지역의 약속을 찾을 수 있도록 하기 위해
     */
    List<Meeting> findByLocationNameContainingIgnoreCase(String locationName);
    
    /**
     * 시간 범위로 약속 검색
     * 이유: 특정 기간 내의 약속들을 조회하여 일정 관리를 지원
     */
    List<Meeting> findByMeetingTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 오늘 날짜의 약속 목록 조회
     * 이유: 일일 일정 관리를 위한 편의 메서드 제공
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE CAST(m.meetingTime AS DATE) = CAST(CURRENT_TIMESTAMP AS DATE) " +
           "ORDER BY m.meetingTime ASC")
    List<Meeting> findTodayMeetings();

    /**
     * 특정 사용자가 참여 가능한 약속 목록 조회 (최대 인원 미달)
     * 이유: 아직 참여 가능한 약속을 찾아 추천하기 위해
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.status = 'WAITING' " +
           "AND (SELECT COUNT(p) FROM MeetingParticipant p WHERE p.meetingId = m.id AND p.response = 'ACCEPTED') < m.maxParticipants " +
           "ORDER BY m.meetingTime ASC")
    List<Meeting> findAvailableMeetings();
}