package com.promiseservice.controller;

import com.promiseservice.dto.InviteParticipantsRequest;
import com.promiseservice.dto.InviteResponse;
import com.promiseservice.dto.ParticipantResponse;
import com.promiseservice.domain.entity.MeetingParticipant;
import com.promiseservice.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/meetings/{meetingId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    /**
     * 추가 참여자 초대
     * POST /api/meetings/{meetingId}/participants/invite
     */
    @PostMapping("/invite")
    public ResponseEntity<InviteResponse> inviteParticipants(
            @PathVariable Long meetingId,
            @Valid @RequestBody InviteParticipantsRequest request,
            @RequestHeader("X-User-ID") Long hostId) {
        
        log.info("참여자 초대 요청 - 약속 ID: {}, 방장: {}, 초대할 사용자: {}", 
                meetingId, hostId, request.getParticipantUserIds());
        
        try {
            InviteResponse response = participantService.inviteParticipants(meetingId, request, hostId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("참여자 초대 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 초대 응답 (수락/거절)
     * PUT /api/meetings/{meetingId}/participants/respond
     */
    @PutMapping("/respond")
    public ResponseEntity<Void> respondToInvite(
            @PathVariable Long meetingId,
            @RequestParam MeetingParticipant.ResponseStatus response,
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("초대 응답 요청 - 약속 ID: {}, 사용자: {}, 응답: {}", meetingId, userId, response);
        
        try {
            participantService.respondToInvite(meetingId, userId, response);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("초대 응답 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 참여자 제거 (방장만 가능)
     * DELETE /api/meetings/{meetingId}/participants/{participantUserId}
     */
    @DeleteMapping("/{participantUserId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Long meetingId,
            @PathVariable Long participantUserId,
            @RequestHeader("X-User-ID") Long hostId) {
        
        log.info("참여자 제거 요청 - 약속 ID: {}, 제거할 사용자: {}, 방장: {}", 
                meetingId, participantUserId, hostId);
        
        try {
            participantService.removeParticipant(meetingId, participantUserId, hostId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("참여자 제거 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 참여자 목록 조회
     * GET /api/meetings/{meetingId}/participants
     */
    @GetMapping
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable Long meetingId) {
        log.info("참여자 목록 조회 요청 - 약속 ID: {}", meetingId);
        
        try {
            List<MeetingParticipant> participants = participantService.getParticipants(meetingId);
            List<ParticipantResponse> response = participants.stream()
                .map(ParticipantResponse::from)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("참여자 목록 조회 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 응답 상태의 참여자 목록 조회
     * GET /api/meetings/{meetingId}/participants/status/{response}
     */
    @GetMapping("/status/{response}")
    public ResponseEntity<List<ParticipantResponse>> getParticipantsByResponse(
            @PathVariable Long meetingId,
            @PathVariable MeetingParticipant.ResponseStatus response) {
        
        log.info("응답 상태별 참여자 목록 조회 요청 - 약속 ID: {}, 응답 상태: {}", meetingId, response);
        
        try {
            List<MeetingParticipant> participants = participantService.getParticipantsByResponse(meetingId, response);
            List<ParticipantResponse> responseList = participants.stream()
                .map(ParticipantResponse::from)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            log.error("응답 상태별 참여자 목록 조회 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 참여자 통계 정보 조회
     * GET /api/meetings/{meetingId}/participants/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getParticipantStats(@PathVariable Long meetingId) {
        log.info("참여자 통계 조회 요청 - 약속 ID: {}", meetingId);
        
        try {
            long totalCount = participantService.getParticipantCount(meetingId);
            long acceptedCount = participantService.getAcceptedParticipantCount(meetingId);
            long invitedCount = totalCount - acceptedCount;
            
            return ResponseEntity.ok(new Object() {
                public final long totalParticipants = totalCount;
                public final long acceptedParticipants = acceptedCount;
                public final long invitedParticipants = invitedCount;
            });
        } catch (Exception e) {
            log.error("참여자 통계 조회 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }
}

