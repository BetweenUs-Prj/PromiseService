package com.promiseservice.controller;

import com.promiseservice.model.dto.*;
import com.promiseservice.service.MeetingService;
import com.promiseservice.validator.MeetingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5174")
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingValidator meetingValidator;

    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(@Valid @RequestBody MeetingCreateRequest request,
                                                         @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 생성 요청 - 사용자: {}, 제목: {}, 장소ID: {}, 시간: {}",
                userId, request.getTitle(), request.getPlaceId(), request.getScheduledAt());

        if (!meetingValidator.isValidCreateRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MeetingResponse response = meetingService.createMeeting(request, userId);
            log.info("약속 생성 완료 - ID: {}, 상태: {}", response.getMeetingId(), response.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 생성 실패 - 에러: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable Long meetingId) {
        log.info("약속 상세 조회 요청 - ID: {}", meetingId);
        try {
            MeetingResponse response = meetingService.getMeeting(meetingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 조회 실패 - ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<MeetingResponse>> getUserMeetings(@RequestHeader("X-User-ID") Long userId) {
        log.info("사용자 약속 목록 조회 요청 - 사용자ID: {}", userId);
        try {
            List<MeetingResponse> meetings = meetingService.getUserMeetings(userId);
            log.info("사용자 약속 목록 조회 완료 - 사용자ID: {}, 약속수: {}개", userId, meetings.size());
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            log.error("사용자 약속 목록 조회 실패 - 사용자ID: {}, 에러: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{meetingId}")
    public ResponseEntity<Object> updateMeeting(@PathVariable Long meetingId,
                                                @Valid @RequestBody MeetingUpdateRequest request,
                                                @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 수정 요청 - 약속ID: {}, 호스트ID: {}, 수정필드: 제목={}, 시간={}",
                meetingId, userId, request.getTitle(), request.getScheduledAt());

        if (!meetingValidator.isValidUpdateRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MeetingUpdateResponse updateResponse = meetingService.updateMeeting(meetingId, request);
            if (updateResponse.isUpdated()) {
                log.info("약속 수정 완료 - 약속ID: {}", meetingId);
                return ResponseEntity.ok().body(new Object() { public final boolean updated = true; });
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("약속 수정 실패 - 약속ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{meetingId}/cancel")
    public ResponseEntity<Object> cancelMeeting(@PathVariable Long meetingId,
                                                @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 취소 요청 - 약속ID: {}, 호스트ID: {}", meetingId, userId);
        try {
            String result = meetingService.cancelMeeting(meetingId);
            log.info("약속 취소 완료 - 약속ID: {}, 상태: {}", meetingId, result);
            return ResponseEntity.ok().body(new Object() { public final String status = result; });
        } catch (Exception e) {
            log.error("약속 취소 실패 - 약속ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{meetingId}/join")
    public ResponseEntity<Object> joinMeeting(@PathVariable Long meetingId,
                                              @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 참가 요청 - 약속ID: {}, 사용자ID: {}", meetingId, userId);
        try {
            String result = meetingService.joinMeeting(meetingId, userId);
            log.info("약속 참가 완료 - 약속ID: {}, 사용자ID: {}, 상태: {}", meetingId, userId, result);
            return ResponseEntity.ok().body(new Object() { public final String status = result; });
        } catch (Exception e) {
            log.error("약속 참가 실패 - 약속ID: {}, 사용자ID: {}, 에러: {}", meetingId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{meetingId}/leave")
    public ResponseEntity<Object> leaveMeeting(@PathVariable Long meetingId,
                                               @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 나가기 요청 - 약속ID: {}, 사용자ID: {}", meetingId, userId);
        try {
            String result = meetingService.leaveMeeting(meetingId, userId);
            log.info("약속 나가기 완료 - 약속ID: {}, 사용자ID: {}, 상태: {}", meetingId, userId, result);
            return ResponseEntity.ok().body(new Object() { public final String status = result; });
        } catch (Exception e) {
            log.error("약속 나가기 실패 - 약속ID: {}, 사용자ID: {}, 에러: {}", meetingId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{meetingId}/participants")
    public ResponseEntity<MeetingParticipantsResponse> getParticipants(@PathVariable Long meetingId) {
        log.info("참가자 목록 조회 요청 - 약속ID: {}", meetingId);
        try {
            MeetingParticipantsResponse response = meetingService.getParticipants(meetingId);
            log.info("참가자 목록 조회 완료 - 약속ID: {}, 참가자수: {}명", meetingId, response.getItems().size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("참가자 목록 조회 실패 - 약속ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}
