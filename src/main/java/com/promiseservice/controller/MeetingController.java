package com.promiseservice.controller;

import com.promiseservice.repository.UserIdentityRepository;
import com.promiseservice.dto.MeetingCreateRequest;
import com.promiseservice.dto.MeetingResponse;
import com.promiseservice.service.MeetingService;
import com.promiseservice.model.entity.Meeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final   UserIdentityRepository userIdentityRepository;

    /**
     * 약속방 생성
     * POST /api/meetings
     * 이유: 새로운 약속을 생성하고 초대된 사용자들에게 자동으로 카카오톡 알림을 전송하기 위해
     * 
     * 인증 방식:
     * 1. X-User-Id: 직접 사용자 ID 전달 (테스트용)
     * 2. X-Kakao-Id: 카카오 사용자 ID로 사용자 조회 (운영용)
     */
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody MeetingCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Kakao-Id", required = false) String kakaoId) {
        
        log.info("=== 🔍 약속 생성 요청 진단 시작 ===");
        log.info("제목: {}", request.getTitle());
        log.info("X-User-Id: {}", userId);
        log.info("X-Kakao-Id: {}", kakaoId);
        log.info("participants: {}", request.getParticipants());
        log.info("participantUserIds: {}", request.getParticipantUserIds());
        log.info("getParticipantUserIds(): {}", request.getParticipantUserIds());
        log.info("=== 🔍 약속 생성 요청 진단 완료 ===");
        
        try {
            // 사용자 ID 해결 (X-User-Id 우선, 없으면 X-Kakao-Id로 조회)
            Long currentUserId = resolveUserId(userId, kakaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "X-User-Id 또는 X-Kakao-Id 중 하나는 필수입니다."));
            
            log.info("약속방 생성 시작 - 방장: {}, 제목: {}", currentUserId, request.getTitle());
            
            MeetingResponse response = meetingService.createMeeting(request, currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("약속방 생성 실패 - 에러: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "약속 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 ID를 해결하는 메서드
     * 이유: X-User-Id 또는 X-Kakao-Id를 통해 실제 사용자 ID를 찾아내기 위해
     * 
     * @param userId 직접 전달된 사용자 ID
     * @param kakaoId 카카오 사용자 ID
     * @return 해결된 사용자 ID
     */
    private Optional<Long> resolveUserId(Long userId, String kakaoId) {
        // 1. X-User-Id가 직접 전달된 경우 (테스트용)
        if (userId != null) {
            log.debug("X-User-Id로 사용자 인증: {}", userId);
            return Optional.of(userId);
        }
        
        // 2. X-Kakao-Id가 전달된 경우 (운영용)
        if (kakaoId != null && !kakaoId.isBlank()) {
            log.debug("X-Kakao-Id로 사용자 조회: {}", kakaoId);
            return findUserIdByKakaoId(kakaoId);
        }
        
        // 3. 둘 다 없는 경우
        log.warn("사용자 인증 정보가 없습니다 - X-User-Id: {}, X-Kakao-Id: {}", userId, kakaoId);
        return Optional.empty();
    }

    /**
     * 카카오 ID로 사용자 ID를 찾는 메서드
     * 이유: 카카오 로그인 사용자의 실제 사용자 ID를 조회하기 위해
     * 
     * @param kakaoId 카카오 사용자 ID
     * @return 사용자 ID (Optional)
     */
    private Optional<Long> findUserIdByKakaoId(String kakaoId) {
        if (kakaoId == null || kakaoId.isBlank()) {
            log.warn("Kakao ID가 null이거나 빈 문자열입니다");
            return Optional.empty();
        }

        // 공백 제거 및 정규화
        String cleanKakaoId = kakaoId.trim();
        log.debug("Kakao ID 정규화: '{}' → '{}'", kakaoId, cleanKakaoId);

        try {
            // UserIdentityRepository로 조회
            var userIdentity = userIdentityRepository.findByProviderAndProviderUserId(
                com.promiseservice.enums.Provider.KAKAO, cleanKakaoId);

            if (userIdentity.isPresent()) {
                Long userId = userIdentity.get().getUserId();
                log.info("사용자 ID 조회 성공 - kakaoId: {} → userId: {}", cleanKakaoId, userId);
                return Optional.of(userId);
            } else {
                log.warn("Kakao ID 매핑 실패. provider=KAKAO, kakaoId='{}'", cleanKakaoId);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("Kakao ID 조회 중 오류 발생 - kakaoId: {}, error: {}", kakaoId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 여러 값 중에서 첫 번째 non-blank 값을 반환하는 헬퍼 메서드
     * 이유: 다양한 헤더 이름과 레벨에서 Provider ID를 찾기 위해
     * 
     * @param vals 검사할 문자열 배열
     * @return 첫 번째 non-blank 값 또는 null
     */
    private static String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (var v : vals) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
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
