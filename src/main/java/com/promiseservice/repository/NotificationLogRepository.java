package com.promiseservice.repository;

import com.promiseservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 알림 전송 로그 리포지토리
 * 이유: 알림 전송 기록의 저장, 조회, 통계 기능을 제공하여 운영 및 디버깅 지원
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * 특정 약속의 모든 알림 로그 조회
     * 이유: 약속별 알림 전송 현황을 한눈에 파악하기 위해
     * 
     * @param meetingId 약속 ID
     * @return 해당 약속의 모든 알림 로그 (최신순)
     */
    List<NotificationLog> findByMeetingIdOrderByCreatedAtDesc(Long meetingId);

    /**
     * 특정 사용자의 알림 로그 조회
     * 이유: 사용자별 알림 수신 이력을 확인하기 위해
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 모든 알림 로그 (최신순)
     */
    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 추적 ID로 관련된 모든 알림 로그 조회
     * 이유: 동일한 이벤트로 발송된 모든 알림을 그룹핑하여 조회하기 위해
     * 
     * @param traceId 추적 ID
     * @return 해당 추적 ID의 모든 알림 로그
     */
    List<NotificationLog> findByTraceIdOrderByCreatedAtDesc(String traceId);

    /**
     * 멱등성 체크: 동일한 조건의 로그가 이미 존재하는지 확인
     * 이유: 중복 전송을 방지하기 위해 동일한 약속/사용자/채널/추적ID 조합 확인
     * 
     * @param meetingId 약속 ID
     * @param userId 사용자 ID
     * @param channel 알림 채널
     * @param traceId 추적 ID
     * @return 기존 로그가 있으면 반환, 없으면 empty
     */
    Optional<NotificationLog> findByMeetingIdAndUserIdAndChannelAndTraceId(
            Long meetingId, 
            Long userId, 
            NotificationLog.NotificationChannel channel, 
            String traceId
    );

    /**
     * 특정 기간 내 전송 성공률 통계
     * 이유: 알림 서비스의 성능을 모니터링하고 개선점을 파악하기 위해
     * 
     * @param channel 알림 채널
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 성공한 알림 개수
     */
    @Query("SELECT COUNT(n) FROM NotificationLog n WHERE n.channel = :channel " +
           "AND n.createdAt BETWEEN :startDate AND :endDate " +
           "AND n.httpStatus BETWEEN 200 AND 299 AND (n.resultCode IS NULL OR n.resultCode = 0)")
    long countSuccessfulNotifications(@Param("channel") NotificationLog.NotificationChannel channel,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 기간 내 전체 전송 시도 개수
     * 이유: 전체 전송량 대비 성공률을 계산하기 위해
     * 
     * @param channel 알림 채널
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 전체 알림 시도 개수
     */
    @Query("SELECT COUNT(n) FROM NotificationLog n WHERE n.channel = :channel " +
           "AND n.createdAt BETWEEN :startDate AND :endDate")
    long countTotalNotifications(@Param("channel") NotificationLog.NotificationChannel channel,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    /**
     * 최근 실패한 알림 조회 (디버깅용)
     * 이유: 최근 발생한 전송 실패를 빠르게 파악하고 대응하기 위해
     * 
     * @param channel 알림 채널
     * @param limit 조회할 개수
     * @return 최근 실패한 알림 로그 목록
     */
    @Query("SELECT n FROM NotificationLog n WHERE n.channel = :channel " +
           "AND (n.httpStatus < 200 OR n.httpStatus >= 300 OR n.resultCode != 0) " +
           "ORDER BY n.createdAt DESC")
    List<NotificationLog> findRecentFailures(@Param("channel") NotificationLog.NotificationChannel channel,
                                           @Param("limit") int limit);

    /**
     * 특정 약속의 채널별 전송 현황 요약
     * 이유: 약속별로 각 채널의 전송 성공/실패 현황을 한눈에 파악하기 위해
     * 
     * @param meetingId 약속 ID
     * @return 채널별 전송 현황 (성공/실패 개수 포함)
     */
    @Query("SELECT n.channel, " +
           "SUM(CASE WHEN n.httpStatus BETWEEN 200 AND 299 AND (n.resultCode IS NULL OR n.resultCode = 0) THEN 1 ELSE 0 END) as successCount, " +
           "SUM(CASE WHEN n.httpStatus < 200 OR n.httpStatus >= 300 OR n.resultCode != 0 THEN 1 ELSE 0 END) as failureCount " +
           "FROM NotificationLog n WHERE n.meetingId = :meetingId GROUP BY n.channel")
    List<Object[]> getNotificationSummaryByMeeting(@Param("meetingId") Long meetingId);
}
