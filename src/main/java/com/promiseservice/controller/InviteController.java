package com.promiseservice.controller;

import com.promiseservice.model.dto.InviteCreateRequest;
import com.promiseservice.model.dto.MeetingInviteRequest;
import com.promiseservice.model.dto.MeetingInviteResponse;
import com.promiseservice.service.MeetingService;
import com.promiseservice.validator.MeetingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final MeetingService meetingService;
    private final MeetingValidator meetingValidator;

    @PostMapping
    public ResponseEntity<MeetingInviteResponse> createInvite(@Valid @RequestBody InviteCreateRequest request,
                                                              @RequestHeader("X-User-ID") Long userId) {
        log.info("초대 생성 요청 - 약속ID: {}, 보낸이: {}, 대상수: {}",
                request.getMeetingId(), userId,
                request.getInviteeIds() != null ? request.getInviteeIds().size() : 0);

        // 기존 Validator/Service가 MeetingInviteRequest(userIds/message)를 기대한다면 어댑트
        MeetingInviteRequest legacy = new MeetingInviteRequest();
        // MeetingInviteRequest의 리스트 필드명이 userIds 라면:
        legacy.setUserIds(request.getInviteeIds());
        // 만약 필드명이 inviteeIds 라면 위 줄 대신 legacy.setInviteeIds(...);
        legacy.setMessage(request.getMessage());

        if (!meetingValidator.isValidInviteRequest(legacy)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MeetingInviteResponse resp =
                    meetingService.inviteParticipants(request.getMeetingId(), legacy, userId);
            log.info("초대 생성 완료 - 약속ID: {}, 성공 {}명", request.getMeetingId(), resp.getInvited().size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("초대 생성 실패 - 약속ID: {}, 에러: {}", request.getMeetingId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
