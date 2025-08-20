package com.promiseservice.controller;

import com.promiseservice.dto.MeetingStatusUpdateRequest;
import com.promiseservice.dto.MeetingStatusResponse;
import com.promiseservice.dto.StatusHistoryResponse;
import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.service.MeetingStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/meetings/{meetingId}/status")
@RequiredArgsConstructor
public class MeetingStatusController {

    private final MeetingStatusService meetingStatusService;

    /**
     * 약속 상태 변경
     * PUT /api/meetings/{meetingId}/status
     */
    @PutMapping
    public ResponseEntity<MeetingStatusResponse> updateMeetingStatus(
            @PathVariable Long meetingId,
            @Valid @RequestBody MeetingStatusUpdateRequest request,
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("약속 상태 변경 요청 - 약속 ID: {}, 사용자: {}, 새 상태: {}", 
                meetingId, userId, request.getStatus());
        
        try {
            MeetingStatusResponse response = meetingStatusService.updateMeetingStatus(meetingId, request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 상태 변경 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 약속 상태 히스토리 조회
     * GET /api/meetings/{meetingId}/status/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<StatusHistoryResponse>> getStatusHistory(@PathVariable Long meetingId) {
        log.info("약속 상태 히스토리 조회 요청 - 약속 ID: {}", meetingId);
        
        try {
            List<StatusHistoryResponse> history = meetingStatusService.getStatusHistory(meetingId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("약속 상태 히스토리 조회 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }
}

