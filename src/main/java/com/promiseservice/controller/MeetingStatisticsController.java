package com.promiseservice.controller;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.service.MeetingStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/meetings/statistics")
@RequiredArgsConstructor
public class MeetingStatisticsController {

    private final MeetingStatusService meetingStatusService;

    /**
     * 약속 상태별 통계 조회
     * GET /api/meetings/statistics/status
     */
    @GetMapping("/status")
    public ResponseEntity<Object> getStatusStatistics() {
        log.info("약속 상태별 통계 조회 요청");
        
        try {
            Object statistics = meetingStatusService.getStatusStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("약속 상태별 통계 조회 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 상태의 약속 목록 조회
     * GET /api/meetings/statistics/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Meeting>> getMeetingsByStatus(@PathVariable String status) {
        log.info("특정 상태의 약속 목록 조회 요청 - 상태: {}", status);
        
        try {
            Meeting.MeetingStatus meetingStatus = Meeting.MeetingStatus.valueOf(status.toUpperCase());
            List<Meeting> meetings = meetingStatusService.getMeetingsByStatus(meetingStatus);
            return ResponseEntity.ok(meetings);
        } catch (IllegalArgumentException e) {
            log.error("유효하지 않은 약속 상태: {}", status);
            throw new RuntimeException("유효하지 않은 약속 상태입니다: " + status);
        } catch (Exception e) {
            log.error("특정 상태의 약속 목록 조회 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }
}

