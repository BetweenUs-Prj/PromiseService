package com.promiseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 알림 전송 결과 조회 컨트롤러 (최소 구현)
 * 이유: 관리자와 개발자가 알림 전송 현황을 실시간으로 모니터링하고 디버깅할 수 있도록 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationLogController {

    /**
     * 약속별 알림 전송 통계 조회 (최소 구현)
     * 이유: "약속 잡혔는데 카톡 갔나요?" 질문에 즉시 답변할 수 있도록 약속별 전송 상태 제공
     * 
     * @param meetingId 약속 ID
     * @return 성공률 및 통계 정보
     */
    @GetMapping(value = "/meeting/{meetingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> statsByMeeting(@PathVariable long meetingId) {
        
        log.info("약속별 알림 통계 조회 요청 - meetingId: {}", meetingId);
        
        // TODO: 실제 NotificationLogService 구현 후 교체
        // 이유: 현재는 더미 데이터로 API 매핑 확인, 추후 실제 서비스 로직 연결
        Map<String, Object> stats = Map.of(
            "meetingId", meetingId,
            "totalCount", 5,
            "successCount", 4,
            "failureCount", 1,
            "successRate", 80.0,
            "lastUpdated", LocalDateTime.now(),
            "status", "mock_data"
        );
        
        log.info("약속별 알림 통계 조회 완료 - meetingId: {}, 성공률: 80%", meetingId);
        
        return stats;
    }
}
