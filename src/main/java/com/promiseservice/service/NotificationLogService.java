package com.promiseservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promiseservice.dto.KakaoSendResult;
import com.promiseservice.entity.NotificationLog;
import com.promiseservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 알림 전송 로그 서비스
 * 이유: 알림 전송 결과를 체계적으로 기록하고 조회하여 운영 및 디버깅 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationLogService {

    private final NotificationLogRepository notificationLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * 카카오톡 전송 결과 로깅
     * 이유: 카카오톡 메시지 전송 결과를 데이터베이스에 기록하여 추후 조회 및 분석 가능
     * 
     * @param meetingId 약속 ID
     * @param userId 사용자 ID
     * @param payloadJson 전송한 메시지 JSON
     * @param result 카카오 API 호출 결과
     * @param traceId 추적 ID
     * @return 저장된 로그 엔티티
     */
    @Transactional
    public NotificationLog logKakaoSend(Long meetingId, Long userId, String payloadJson, 
                                       KakaoSendResult result, String traceId) {
        // 멱등성 체크: 이미 동일한 로그가 있는지 확인
        // 이유: 중복 전송이나 재시도로 인한 중복 로그 방지
        var existing = notificationLogRepository.findByMeetingIdAndUserIdAndChannelAndTraceId(
            meetingId, userId, NotificationLog.NotificationChannel.KAKAO, traceId
        );
        
        if (existing.isPresent()) {
            log.debug("이미 존재하는 알림 로그 - meetingId: {}, userId: {}, traceId: {}", 
                     meetingId, userId, traceId);
            return existing.get();
        }

        // 새로운 로그 생성
        // 이유: 전송 결과를 상세히 기록하여 문제 발생 시 디버깅 정보 제공
        NotificationLog logEntry = NotificationLog.builder()
            .meetingId(meetingId)
            .userId(userId)
            .channel(NotificationLog.NotificationChannel.KAKAO)
            .payloadJson(payloadJson)
            .httpStatus(result.httpStatus())
            .resultCode(result.resultCode())
            .errorJson(result.errorMessage())
            .traceId(traceId)
            .build();

        NotificationLog saved = notificationLogRepository.save(logEntry);
        
        log.info("카카오톡 전송 로그 저장 완료 - meetingId: {}, userId: {}, status: {}, traceId: {}", 
                meetingId, userId, result.httpStatus(), traceId);
        
        return saved;
    }

    /**
     * 실패한 전송에 대한 로그 기록
     * 이유: 토큰 없음, 네트워크 오류 등으로 API 호출 자체가 불가능한 경우도 기록
     * 
     * @param meetingId 약속 ID
     * @param userId 사용자 ID
     * @param traceId 추적 ID
     * @param errorMessage 실패 사유
     * @return 저장된 로그 엔티티
     */
    @Transactional
    public NotificationLog logFailedSend(Long meetingId, Long userId, String traceId, String errorMessage) {
        NotificationLog logEntry = NotificationLog.builder()
            .meetingId(meetingId)
            .userId(userId)
            .channel(NotificationLog.NotificationChannel.KAKAO)
            .payloadJson("{\"error\":\"" + errorMessage + "\"}")
            .httpStatus(0) // API 호출 자체 실패
            .resultCode(-1)
            .errorJson(errorMessage)
            .traceId(traceId)
            .build();

        NotificationLog saved = notificationLogRepository.save(logEntry);
        
        log.warn("전송 실패 로그 저장 - meetingId: {}, userId: {}, error: {}, traceId: {}", 
                meetingId, userId, errorMessage, traceId);
        
        return saved;
    }

    /**
     * 약속별 알림 전송 현황 조회
     * 이유: 특정 약속에 대한 모든 참여자의 알림 전송 상태를 한눈에 파악
     * 
     * @param meetingId 약속 ID
     * @return 해당 약속의 모든 알림 로그 (최신순)
     */
    public List<NotificationLog> getNotificationsByMeeting(Long meetingId) {
        return notificationLogRepository.findByMeetingIdOrderByCreatedAtDesc(meetingId);
    }

    /**
     * 사용자별 알림 수신 이력 조회
     * 이유: 특정 사용자의 알림 수신 패턴 분석 및 문제 해결
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 모든 알림 로그 (최신순)
     */
    public List<NotificationLog> getNotificationsByUser(Long userId) {
        return notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 추적 ID로 관련 알림 그룹 조회
     * 이유: 동일한 이벤트로 발송된 모든 알림을 그룹핑하여 전체 현황 파악
     * 
     * @param traceId 추적 ID
     * @return 해당 추적 ID의 모든 알림 로그
     */
    public List<NotificationLog> getNotificationsByTrace(String traceId) {
        return notificationLogRepository.findByTraceIdOrderByCreatedAtDesc(traceId);
    }

    /**
     * 추적 ID 생성
     * 이유: 동일한 약속 이벤트로 발송되는 여러 알림을 그룹핑하기 위한 고유 ID 생성
     * 
     * @param meetingId 약속 ID
     * @return 생성된 추적 ID (예: "mtg-123-ab12cd34")
     */
    public String generateTraceId(Long meetingId) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("mtg-%d-%s", meetingId, uuid);
    }

    /**
     * 약속별 전송 성공률 계산
     * 이유: 약속별 알림 전송 품질을 수치로 파악하여 서비스 개선점 도출
     * 
     * @param meetingId 약속 ID
     * @return 성공률 (0.0 ~ 1.0)
     */
    public double calculateSuccessRate(Long meetingId) {
        List<NotificationLog> logs = getNotificationsByMeeting(meetingId);
        
        if (logs.isEmpty()) {
            return 0.0;
        }
        
        long successCount = logs.stream()
            .mapToLong(log -> log.isSuccess() ? 1 : 0)
            .sum();
            
        return (double) successCount / logs.size();
    }

    /**
     * 최근 실패한 알림 조회 (디버깅용)
     * 이유: 최근 발생한 전송 실패를 빠르게 파악하고 대응하기 위해
     * 
     * @param limit 조회할 개수
     * @return 최근 실패한 알림 로그 목록
     */
    public List<NotificationLog> getRecentFailures(int limit) {
        return notificationLogRepository.findRecentFailures(
            NotificationLog.NotificationChannel.KAKAO, limit
        );
    }
}
