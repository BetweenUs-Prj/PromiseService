package com.promiseservice.controller;

import com.promiseservice.dto.*;
import com.promiseservice.service.MeetingService;
import com.promiseservice.validator.MeetingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 약속 관련 API 컨트롤러
 * 이유: 약속 생성, 수정, 초대 등 약속 관리의 핵심 기능을 제공하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingValidator meetingValidator;

    /**
     * 약속 생성 API
     * 이유: 사용자가 새로운 약속을 생성하고 참가자를 초대할 수 있도록 하기 위해
     *
     * @param request 약속 생성 요청 데이터
     * @return 생성된 약속 정보
     */
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(@Valid @RequestBody MeetingCreateRequest request) {
        log.info("약속 생성 요청 - 제목: {}, 장소: {}, 시간: {}",
                request.getTitle(), request.getPlaceName(), request.getScheduledAt());

        // 유효성 검증
        if (!meetingValidator.isValidCreateRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MeetingResponse response = meetingService.createMeeting(request);
            log.info("약속 생성 완료 - ID: {}, 제목: {}", response.getMeetingId(), response.getTitle());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 생성 실패 - 에러: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 약속 상세 조회 API
     * 이유: 생성된 약속의 상세 정보와 참가자 목록을 조회할 수 있도록 하기 위해
     *
     * @param meetingId 약속 ID
     * @return 약속 상세 정보
     */
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

    /**
     * 약속 확정 API
     * 이유: 모든 참가자가 응답한 후 방장이 약속을 확정할 수 있도록 하기 위해
     *
     * @param request 약속 확정 요청 데이터
     * @return 확정된 약속 정보
     */
    @PostMapping("/confirm")
    public ResponseEntity<MeetingResponse> confirmMeeting(@Valid @RequestBody MeetingConfirmRequest request) {
        log.info("약속 확정 요청 - ID: {}", request.getMeetingId());

        try {
            MeetingResponse response = meetingService.confirmMeeting(request);
            log.info("약속 확정 완료 - ID: {}, 제목: {}", response.getMeetingId(), response.getTitle());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 확정 실패 - ID: {}, 에러: {}", request.getMeetingId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 약속 초대 응답 API (수락/거부)
     * 이유: 초대받은 사용자가 약속 초대에 응답할 수 있도록 하기 위해
     *
     * @param meetingId 약속 ID
     * @param userId 사용자 ID
     * @param accept 수락 여부
     * @return 응답 결과
     */
    @PostMapping("/{meetingId}/respond")
    public ResponseEntity<Object> respondToInvite(@PathVariable Long meetingId,
                                                 @RequestParam Long userId,
                                                 @RequestParam boolean accept) {
        log.info("약속 초대 응답 요청 - 약속: {}, 사용자: {}, 수락: {}", meetingId, userId, accept);

        try {
            String result = meetingService.respondToInvite(meetingId, userId, accept);
            log.info("약속 초대 응답 완료 - 약속: {}, 사용자: {}, 결과: {}", meetingId, userId, result);
            return ResponseEntity.ok().body(new Object() {
                public final String responseStatus = result;
                public final String message = "초대 응답이 완료되었습니다";
            });
        } catch (Exception e) {
            log.error("약속 초대 응답 실패 - 약속: {}, 사용자: {}, 에러: {}", meetingId, userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 확정된 약속 목록 조회 API
     * 이유: 확정된 약속들을 목록으로 조회할 수 있도록 하기 위해
     *
     * @param userId 사용자 ID
     * @return 확정된 약속 목록
     */
    @GetMapping("/confirmed")
    public ResponseEntity<Object> getConfirmedMeetings(@RequestParam Long userId) {
        log.info("확정된 약속 목록 조회 요청 - 사용자: {}", userId);

        try {
            var meetings = meetingService.getConfirmedMeetings(userId);
            return ResponseEntity.ok().body(new Object() {
                public final List<MeetingResponse> meetingList = meetings;
                public final int totalCount = meetings.size();
            });
        } catch (Exception e) {
            log.error("확정된 약속 목록 조회 실패 - 사용자: {}, 에러: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 약속 정보 수정 API
     * 이유: 약속의 제목, 시간, 장소 등 정보를 수정할 수 있도록 하기 위해
     *
     * @param meetingId 약속 ID
     * @param request 수정할 정보
     * @return 수정 결과
     */
    @PatchMapping("/{meetingId}")
    public ResponseEntity<Object> updateMeeting(@PathVariable Long meetingId,
                                              @Valid @RequestBody MeetingUpdateRequest request) {
        log.info("약속 수정 요청 - ID: {}, 수정 필드: {}", meetingId, request);

        // 유효성 검증
        if (!meetingValidator.isValidUpdateRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            boolean updated = meetingService.updateMeeting(meetingId, request);
            if (updated) {
                log.info("약속 수정 완료 - ID: {}", meetingId);
                return ResponseEntity.ok().body(new Object() {
                    public final boolean updated = true;
                });
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("약속 수정 실패 - ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 약속 취소 API
     * 이유: 약속이 더 이상 필요하지 않을 때 취소할 수 있도록 하기 위해
     *
     * @param meetingId 약속 ID
     * @return 취소 결과
     */
    @PostMapping("/{meetingId}/cancel")
    public ResponseEntity<Object> cancelMeeting(@PathVariable Long meetingId) {
        log.info("약속 취소 요청 - ID: {}", meetingId);

        try {
            String result = meetingService.cancelMeeting(meetingId);
            log.info("약속 취소 완료 - ID: {}, 상태: {}", meetingId, result);
            return ResponseEntity.ok().body(new Object() {
                public final String cancelStatus = result;
            });
        } catch (Exception e) {
            log.error("약속 취소 실패 - ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 약속 초대 API
     * 이유: 기존 약속에 새로운 참가자를 초대할 수 있도록 하기 위해
     *
     * @param meetingId 약속 ID
     * @param request 초대 요청 데이터
     * @return 초대 결과
     */
    @PostMapping("/{meetingId}/invites")
    public ResponseEntity<Object> inviteParticipants(@PathVariable Long meetingId,
                                                   @Valid @RequestBody MeetingInviteRequest request) {
        log.info("약속 초대 요청 - ID: {}, 초대 대상: {}", meetingId, request);

        // 유효성 검증
        if (!meetingValidator.isValidInviteRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Object response = meetingService.inviteParticipants(meetingId, request);
            log.info("약속 초대 완료 - ID: {}", meetingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 초대 실패 - ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 약속 참가 API
     * 이유: 초대받은 사용자가 약속에 참가할 수 있도록 하기 위해
     *
     * @param meetingId 약속 ID
     * @return 참가 결과
     */
    @PostMapping("/{meetingId}/join")
    public ResponseEntity<Object> joinMeeting(@PathVariable Long meetingId) {
        log.info("약속 참가 요청 - ID: {}", meetingId);

        try {
            String result = meetingService.joinMeeting(meetingId);
            log.info("약속 참가 완료 - ID: {}, 상태: {}", meetingId, result);
            return ResponseEntity.ok().body(new Object() {
                public final String joinStatus = result;
            });
        } catch (Exception e) {
            log.error("약속 참가 실패 - ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 약속 나가기 API
     * 이유: 참가자가 약속에서 나갈 수 있도록 하기 위해
     *
     * @param meetingId 약속 ID
     * @return 나가기 결과
     */
    @PostMapping("/{meetingId}/leave")
    public ResponseEntity<Object> leaveMeeting(@PathVariable Long meetingId) {
        log.info("약속 나가기 요청 - ID: {}", meetingId);

        try {
            String result = meetingService.leaveMeeting(meetingId);
            log.info("약속 나가기 완료 - ID: {}, 상태: {}", meetingId, result);
            return ResponseEntity.ok().body(new Object() {
                public final String leaveStatus = result;
            });
        } catch (Exception e) {
            log.error("약속 나가기 실패 - ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 약속 참가자 목록 조회 API
     * 이유: 약속에 참가한 사용자들의 목록을 조회할 수 있도록 하기 위해
     *
     * @param meetingId 약속 ID
     * @return 참가자 목록
     */
    @GetMapping("/{meetingId}/participants")
    public ResponseEntity<Object> getParticipants(@PathVariable Long meetingId) {
        log.info("참가자 목록 조회 요청 - ID: {}", meetingId);

        try {
            Object response = meetingService.getParticipants(meetingId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("참가자 목록 조회 실패 - ID: {}, 에러: {}", meetingId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}
