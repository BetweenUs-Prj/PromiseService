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
    public ResponseEntity<MeetingResponse> createMeeting(@Valid @RequestBody MeetingCreateRequest request,
                                                        @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 생성 요청 - 사용자: {}, 제목: {}, 장소ID: {}, 시간: {}",
                userId, request.getTitle(), request.getPlaceId(), request.getScheduledAt());

        // 유효성 검증
        if (!meetingValidator.isValidCreateRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MeetingResponse response = meetingService.createMeeting(request);
            log.info("약속 생성 완료 - ID: {}, 상태: {}", response.getMeetingId(), response.getStatus());
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
     * 약속 정보 수정 API
     * 이유: 약속의 제목, 시간, 장소 등 정보를 수정할 수 있도록 하기 위해
     *
     * @param meetingId 약속 ID
     * @param request 수정할 정보
     * @return 수정 결과
     */
    @PatchMapping("/{meetingId}")
    public ResponseEntity<Object> updateMeeting(@PathVariable Long meetingId,
                                              @Valid @RequestBody MeetingUpdateRequest request,
                                              @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 수정 요청 - 약속ID: {}, 호스트ID: {}, 수정필드: 제목={}, 시간={}", 
                meetingId, userId, request.getTitle(), request.getScheduledAt());

        // 유효성 검증
        if (!meetingValidator.isValidUpdateRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MeetingUpdateResponse updateResponse = meetingService.updateMeeting(meetingId, request);
            if (updateResponse.isUpdated()) {
                log.info("약속 수정 완료 - 약속ID: {}", meetingId);
                return ResponseEntity.ok().body(new Object() {
                    public final boolean updated = true;
                });
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("약속 수정 실패 - 약속ID: {}, 에러: {}", meetingId, e.getMessage(), e);
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
    public ResponseEntity<Object> cancelMeeting(@PathVariable Long meetingId,
                                               @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 취소 요청 - 약속ID: {}, 호스트ID: {}", meetingId, userId);

        try {
            String result = meetingService.cancelMeeting(meetingId);
            log.info("약속 취소 완료 - 약속ID: {}, 상태: {}", meetingId, result);
            return ResponseEntity.ok().body(new Object() {
                public final String status = result;
            });
        } catch (Exception e) {
            log.error("약속 취소 실패 - 약속ID: {}, 에러: {}", meetingId, e.getMessage(), e);
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
    public ResponseEntity<MeetingInviteResponse> inviteParticipants(@PathVariable Long meetingId,
                                                                  @Valid @RequestBody MeetingInviteRequest request,
                                                                  @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 초대 요청 - 약속ID: {}, 호스트ID: {}, 초대대상: {}명", meetingId, userId, 
                request.getUserIds() != null ? request.getUserIds().size() : 0);

        // 유효성 검증
        if (!meetingValidator.isValidInviteRequest(request)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MeetingInviteResponse response = meetingService.inviteParticipants(meetingId, request, userId);
            log.info("약속 초대 완료 - 약속ID: {}, 초대성공: {}명", meetingId, response.getInvited().size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 초대 실패 - 약속ID: {}, 에러: {}", meetingId, e.getMessage(), e);
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
    public ResponseEntity<Object> joinMeeting(@PathVariable Long meetingId,
                                             @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 참가 요청 - 약속ID: {}, 사용자ID: {}", meetingId, userId);

        try {
            String result = meetingService.joinMeeting(meetingId, userId);
            log.info("약속 참가 완료 - 약속ID: {}, 사용자ID: {}, 상태: {}", meetingId, userId, result);
            return ResponseEntity.ok().body(new Object() {
                public final String status = result;
            });
        } catch (Exception e) {
            log.error("약속 참가 실패 - 약속ID: {}, 사용자ID: {}, 에러: {}", meetingId, userId, e.getMessage(), e);
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
    public ResponseEntity<Object> leaveMeeting(@PathVariable Long meetingId,
                                              @RequestHeader("X-User-ID") Long userId) {
        log.info("약속 나가기 요청 - 약속ID: {}, 사용자ID: {}", meetingId, userId);

        try {
            String result = meetingService.leaveMeeting(meetingId, userId);
            log.info("약속 나가기 완료 - 약속ID: {}, 사용자ID: {}, 상태: {}", meetingId, userId, result);
            return ResponseEntity.ok().body(new Object() {
                public final String status = result;
            });
        } catch (Exception e) {
            log.error("약속 나가기 실패 - 약속ID: {}, 사용자ID: {}, 에러: {}", meetingId, userId, e.getMessage(), e);
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
