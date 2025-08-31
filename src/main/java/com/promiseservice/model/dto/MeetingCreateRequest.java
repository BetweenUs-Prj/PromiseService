package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 약속 생성 요청을 위한 DTO
 * 이유: 클라이언트로부터 약속 생성에 필요한 정보를 받고 유효성 검증을 수행하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class MeetingCreateRequest {

    @NotBlank(message = "약속 제목은 필수입니다")
    @Size(max = 255, message = "약속 제목은 255자를 초과할 수 없습니다")
    private String title;

    @Size(max = 2000, message = "약속 설명은 2000자를 초과할 수 없습니다")
    private String description;

    @NotNull(message = "약속 시간은 필수입니다")
    private LocalDateTime meetingTime;

    @Min(value = 1, message = "최대 참여자 수는 최소 1명 이상이어야 합니다")
    @Max(value = 10, message = "최대 참여자 수는 10명을 초과할 수 없습니다")
    private Integer maxParticipants = 10;

    // 장소 정보 (LocationService에서 전달받은 정보)
    @NotBlank(message = "장소명은 필수입니다")
    @Size(max = 500, message = "장소명은 500자를 초과할 수 없습니다")
    private String locationName;

    @Size(max = 500, message = "주소는 500자를 초과할 수 없습니다")
    private String locationAddress;

    private String locationCoordinates; // JSON 형태로 위도,경도

    // 초대할 친구 ID 목록 (방장은 자동으로 추가되므로 여기에 포함하지 않음)
    // 이유: 요청을 보내는 사용자가 자동으로 방장이 되고, 추가로 초대할 사용자들만 명시
    private List<Long> participantUserIds;

    // 클라이언트 요청과의 호환성을 위한 participants 필드 (participantUserIds와 동일)
    private List<Long> participants;

    // 알림 발송 여부
    // 이유: 약속 생성 시 카카오톡 알림 발송을 선택적으로 할 수 있도록 하기 위해
    private Boolean sendNotification = true;

    /**
     * 참가자 ID 목록을 가져오는 메서드
     * 이유: participants 또는 participantUserIds 중 어느 것이든 사용할 수 있도록 하기 위해
     * 
     * @return 참가자 ID 목록
     */
    public List<Long> getParticipantUserIds() {
        if (participantUserIds != null && !participantUserIds.isEmpty()) {
            return participantUserIds;
        }
        return participants != null ? participants : new ArrayList<>();
    }

    /**
     * 최대 참여자 수 검증
     * 이유: 초대할 사용자 수가 최대 참여자 수를 초과하지 않도록 검증
     * 
     * @param hostIncluded 방장을 포함할지 여부
     * @return 유효성 검증 결과
     */
    public boolean isValidParticipantCount(boolean hostIncluded) {
        if (participantUserIds == null) {
            return hostIncluded ? maxParticipants >= 1 : maxParticipants >= 0;
        }
        
        int totalParticipants = participantUserIds.size() + (hostIncluded ? 1 : 0);
        return totalParticipants <= maxParticipants;
    }

    /**
     * 총 초대 인원 수 계산
     * 이유: 방장 포함 총 인원을 계산하여 제한 검증에 활용
     * 
     * @return 총 초대 인원 수 (방장 포함)
     */
    public int getTotalInvitedCount() {
        return (participantUserIds != null ? participantUserIds.size() : 0) + 1; // +1은 방장
    }
}

