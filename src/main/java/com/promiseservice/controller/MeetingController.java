package com.promiseservice.controller;

import com.promiseservice.dto.MeetingCreateRequest;
import com.promiseservice.dto.MeetingResponse;
import com.promiseservice.service.MeetingService;
import com.promiseservice.domain.entity.Meeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    /**
     * 약속방 생성
     * POST /api/meetings
     */
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody MeetingCreateRequest request,
            @RequestHeader("X-User-ID") Long hostId) {
        
        log.info("약속방 생성 요청 - 방장: {}, 제목: {}", hostId, request.getTitle());
        
        try {
            MeetingResponse response = meetingService.createMeeting(request, hostId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("약속방 생성 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 약속방 조회
     * GET /api/meetings/{meetingId}
     */
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable Long meetingId) {
        log.info("약속방 조회 요청 - ID: {}", meetingId);
        
        try {
            MeetingResponse response = meetingService.getMeeting(meetingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속방 조회 실패 - ID: {}, 에러: {}", meetingId, e.getMessage());
            throw e;
        }
    }

    /**
     * 방장이 생성한 약속 목록 조회
     * GET /api/meetings/host/{hostId}
     * 이유: 사용자가 자신이 만든 약속들을 관리할 수 있도록 하기 위해
     */
    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<MeetingResponse>> getMeetingsByHost(@PathVariable Long hostId) {
        log.info("방장의 약속 목록 조회 요청 - 방장 ID: {}", hostId);
        
        try {
            List<MeetingResponse> response = meetingService.getMeetingsByHost(hostId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("방장의 약속 목록 조회 실패 - 방장 ID: {}, 에러: {}", hostId, e.getMessage());
            throw e;
        }
    }

    /**
     * 사용자가 참여한 약속 목록 조회
     * GET /api/meetings/participant/{userId}
     */
    @GetMapping("/participant/{userId}")
    public ResponseEntity<List<MeetingResponse>> getMeetingsByParticipant(@PathVariable Long userId) {
        log.info("참여자의 약속 목록 조회 요청 - 사용자 ID: {}", userId);
        
        try {
            List<MeetingResponse> response = meetingService.getMeetingsByParticipant(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("참여자의 약속 목록 조회 실패 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
            throw e;
        }
    }



    /**
     * 약속 삭제
     * DELETE /api/meetings/{meetingId}
     * 이유: 방장이 더 이상 필요하지 않은 약속을 삭제할 수 있도록 하기 위해
     */
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable Long meetingId,
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("약속 삭제 요청 - 약속 ID: {}, 사용자 ID: {}", meetingId, userId);
        
        try {
            meetingService.deleteMeeting(meetingId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("약속 삭제 실패 - 약속 ID: {}, 에러: {}", meetingId, e.getMessage());
            throw e;
        }
    }

    /**
     * 약속 정보 수정
     * PUT /api/meetings/{meetingId}
     * 이유: 방장이 약속의 세부 정보를 변경할 수 있도록 하기 위해
     */
    @PutMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> updateMeeting(
            @PathVariable Long meetingId,
            @Valid @RequestBody MeetingCreateRequest request,
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("약속 정보 수정 요청 - 약속 ID: {}, 사용자 ID: {}", meetingId, userId);
        
        try {
            MeetingResponse response = meetingService.updateMeeting(meetingId, request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 정보 수정 실패 - 약속 ID: {}, 에러: {}", meetingId, e.getMessage());
            throw e;
        }
    }

    /**
     * 약속 완료 처리
     * POST /api/meetings/{meetingId}/complete
     * 이유: 약속이 끝난 후 완료 상태로 변경하기 위해
     */
    @PostMapping("/{meetingId}/complete")
    public ResponseEntity<MeetingResponse> completeMeeting(
            @PathVariable Long meetingId,
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("약속 완료 처리 요청 - 약속 ID: {}, 사용자 ID: {}", meetingId, userId);
        
        try {
            MeetingResponse response = meetingService.completeMeeting(meetingId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 완료 처리 실패 - 약속 ID: {}, 에러: {}", meetingId, e.getMessage());
            throw e;
        }
    }
}
