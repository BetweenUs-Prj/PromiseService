package com.promiseservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 약속 생성 요청 DTO
 * 이유: 사용자가 새로운 약속을 생성할 때 필요한 정보를 전달받기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCreateRequest {

    /**
     * 약속 제목
     * 이유: 약속을 식별하고 구분하기 위한 제목
     */
    @NotBlank(message = "약속 제목은 필수입니다")
    @Size(max = 255, message = "약속 제목은 255자를 초과할 수 없습니다")
    private String title;

    /**
     * 장소 ID (외부 장소 서비스 연동용)
     * 이유: 장소 기반 약속 생성 시 외부 장소 서비스와 연동하여 정확한 장소 정보를 가져오기 위해
     */
    @Min(value = 1, message = "장소 ID는 1 이상이어야 합니다")
    private Long placeId;

    /**
     * 장소명
     * 이유: 사용자에게 표시할 장소명을 직접 제공하기 위해
     */
    @Size(max = 255, message = "장소명은 255자를 초과할 수 없습니다")
    private String placeName;

    /**
     * 장소 주소
     * 이유: 사용자에게 표시할 장소 주소를 직접 제공하기 위해
     */
    @Size(max = 500, message = "장소 주소는 500자를 초과할 수 없습니다")
    private String placeAddress;

    /**
     * 외부 장소 소스 (카카오맵, 네이버맵 등)
     * 이유: 외부 API에서 가져온 장소인지 구분하기 위해
     */
    @Size(max = 20, message = "외부 소스는 20자를 초과할 수 없습니다")
    private String externalPlaceSource;

    /**
     * 외부 장소 ID
     * 이유: 외부 API의 장소 ID를 저장하여 중복 생성을 방지하기 위해
     */
    @Size(max = 100, message = "외부 ID는 100자를 초과할 수 없습니다")
    private String externalPlaceId;

    /**
     * 약속 예정 시간
     * 이유: 약속이 언제 진행될지 명시하기 위해
     */
    @NotNull(message = "약속 시간은 필수입니다")
    @Future(message = "약속 시간은 미래 시간이어야 합니다")
    private LocalDateTime scheduledAt;

    /**
     * 최대 참여 인원 수
     * 이유: 장소 수용 인원이나 모임 규모를 제한하여 원활한 진행을 보장하기 위해
     */
    @Min(value = 2, message = "최대 참여 인원은 2명 이상이어야 합니다")
    @Max(value = 100, message = "최대 참여 인원은 100명을 초과할 수 없습니다")
    private Integer maxParticipants;

    /**
     * 약속 메모
     * 이유: 약속의 목적, 준비사항, 주의사항 등 부가 정보를 제공하기 위해
     */
    @Size(max = 1000, message = "약속 메모는 1000자를 초과할 수 없습니다")
    private String memo;

    /**
     * 초대 메시지
     * 이유: 초대받는 사람들에게 보낼 초대 메시지를 제공하기 위해
     */
    @Size(max = 500, message = "초대 메시지는 500자를 초과할 수 없습니다")
    private String inviteMessage;

    /**
     * 초대할 사용자 ID 목록
     * 이유: 어떤 사용자들을 약속에 초대할지 지정하기 위해
     */
    @Size(max = 98, message = "초대할 사용자는 최대 98명까지 가능합니다") // 호스트 포함 최대 100명
    private List<Long> participantUserIds;

    /**
     * 초대할 카카오 ID 목록 (카카오 친구 초대 시 사용)
     * 이유: 카카오 친구 중에서 약속에 초대할 사용자들을 지정하기 위해
     */
    private List<String> kakaoIds;
}
