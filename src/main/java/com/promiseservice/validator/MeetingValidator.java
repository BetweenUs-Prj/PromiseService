package com.promiseservice.validator;

import com.promiseservice.model.dto.MeetingCreateRequest;
import com.promiseservice.model.dto.MeetingUpdateRequest;
import com.promiseservice.model.dto.MeetingInviteRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 약속 관련 DTO 유효성 검증기
 * 이유: 약속 생성, 수정, 초대 요청의 복잡한 비즈니스 규칙 검증을 담당하여
 * DTO의 책임을 분리하고 재사용성을 높이기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Component
public class MeetingValidator {

    /**
     * 약속 생성 요청 유효성 검증
     * 이유: 약속 생성 시 필요한 모든 필드가 올바르게 입력되었는지 확인하기 위해
     *
     * @param request 약속 생성 요청
     * @return 유효성 검증 결과
     */
    public boolean isValidCreateRequest(MeetingCreateRequest request) {
        if (request == null) return false;

        // 기본 필수 필드 검증
        if (!isValidBasicFields(request)) return false;

        // 참여자 수 제한 검증
        return isValidParticipantCount(request);
    }

    /**
     * 약속 수정 요청 유효성 검증
     * 이유: 약속 수정 시 최소한 하나의 필드는 수정되어야 하고, 각 필드가 유효한지 확인하기 위해
     *
     * @param request 약속 수정 요청
     * @return 유효성 검증 결과
     */
    public boolean isValidUpdateRequest(MeetingUpdateRequest request) {
        if (request == null) return false;

        // 최소한 하나의 필드는 수정되어야 함
        if (!hasUpdateFields(request)) return false;

        // 개별 필드 유효성 검증
        return isValidIndividualFields(request);
    }

    /**
     * 약속 초대 요청 유효성 검증
     * 이유: 약속 초대 시 최소한 하나의 초대 방식은 제공되어야 하고, 메시지 길이가 적절한지 확인하기 위해
     *
     * @param request 약속 초대 요청
     * @return 유효성 검증 결과
     */
    public boolean isValidInviteRequest(MeetingInviteRequest request) {
        if (request == null) return false;

        // 최소한 하나의 초대 방식은 제공되어야 함
        if (!hasInviteMethod(request)) return false;

        // 초대 메시지 길이 검증
        return isValidMessageLength(request);
    }

    /**
     * 기본 필수 필드 검증
     * 이유: 약속 생성에 필요한 기본 필드들이 올바르게 입력되었는지 확인하기 위해
     */
    private boolean isValidBasicFields(MeetingCreateRequest request) {
        return request.getTitle() != null && !request.getTitle().trim().isEmpty() &&
               request.getPlaceId() != null && request.getPlaceId() > 0 &&
               request.getPlaceName() != null && !request.getPlaceName().trim().isEmpty() &&
               request.getPlaceAddress() != null && !request.getPlaceAddress().trim().isEmpty() &&
               request.getScheduledAt() != null && request.getScheduledAt().isAfter(LocalDateTime.now()) &&
               request.getMaxParticipants() != null && request.getMaxParticipants() >= 2 &&
               request.getParticipantUserIds() != null && !request.getParticipantUserIds().isEmpty();
    }

    /**
     * 참여자 수 제한 검증
     * 이유: 초대할 참여자 수가 최대 참여자 수를 초과하지 않는지 확인하기 위해
     */
    private boolean isValidParticipantCount(MeetingCreateRequest request) {
        int totalInvitees = request.getParticipantUserIds().size();
        if (request.getKakaoIds() != null) {
            totalInvitees += request.getKakaoIds().size();
        }

        return totalInvitees <= request.getMaxParticipants();
    }

    /**
     * 수정 필드 존재 여부 확인
     * 이유: 최소한 하나의 필드는 수정되어야 약속 수정이 의미가 있기 때문에
     */
    private boolean hasUpdateFields(MeetingUpdateRequest request) {
        return request.getTitle() != null || request.getScheduledAt() != null ||
               request.getMemo() != null || request.getMaxParticipants() != null ||
               request.getPlaceId() != null || request.getStatus() != null;
    }

    /**
     * 개별 필드 유효성 검증
     * 이유: 수정하려는 각 필드가 올바른 값을 가지고 있는지 확인하기 위해
     */
    private boolean isValidIndividualFields(MeetingUpdateRequest request) {
        // 제목이 제공된 경우 빈 문자열이 아닌지 검증
        if (request.getTitle() != null && request.getTitle().trim().isEmpty()) {
            return false;
        }

        // 시간이 제공된 경우 과거 시간이 아닌지 검증
        if (request.getScheduledAt() != null && request.getScheduledAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 최대 참여자 수가 제공된 경우 유효한 범위인지 검증
        if (request.getMaxParticipants() != null &&
            (request.getMaxParticipants() < 2 || request.getMaxParticipants() > 100)) {
            return false;
        }

        // 장소 ID가 제공된 경우 유효한 값인지 검증
        if (request.getPlaceId() != null && request.getPlaceId() <= 0) {
            return false;
        }

        return true;
    }

    /**
     * 초대 방식 존재 여부 확인
     * 이유: 최소한 하나의 초대 방식은 제공되어야 초대가 의미가 있기 때문에
     */
    private boolean hasInviteMethod(MeetingInviteRequest request) {
        return (request.getUserIds() != null && !request.getUserIds().isEmpty()) ||
               (request.getKakaoIds() != null && !request.getKakaoIds().isEmpty());
    }

    /**
     * 메시지 길이 유효성 검증
     * 이유: 초대 메시지가 너무 길지 않도록 하여 사용자 경험을 향상시키기 위해
     */
    private boolean isValidMessageLength(MeetingInviteRequest request) {
        return request.getMessage() == null || request.getMessage().length() <= 500;
    }
}
