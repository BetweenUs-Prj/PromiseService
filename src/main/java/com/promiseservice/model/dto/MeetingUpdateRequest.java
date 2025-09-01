package com.promiseservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 약속 수정 요청 DTO
 * 이유: 사용자가 기존 약속의 정보를 수정할 때 필요한 정보를 전달받기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUpdateRequest {

    /**
     * 약속 제목
     * 이유: 약속을 식별하고 구분하기 위한 제목
     */
    @Size(max = 255, message = "약속 제목은 255자를 초과할 수 없습니다")
    private String title;

    /**
     * 약속 예정 시간
     * 이유: 약속이 언제 진행될지 명시하기 위해
     */
    @Future(message = "약속 시간은 미래 시간이어야 합니다")
    private LocalDateTime scheduledAt;

    /**
     * 약속 장소명
     * 이유: 약속이 진행될 장소의 이름을 제공하기 위해
     */
    @Size(max = 500, message = "장소명은 500자를 초과할 수 없습니다")
    private String placeName;

    /**
     * 약속 장소 상세 주소
     * 이유: 약속이 진행될 장소의 정확한 위치를 제공하기 위해
     */
    @Size(max = 500, message = "장소 주소는 500자를 초과할 수 없습니다")
    private String placeAddress;

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
}
