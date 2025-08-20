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

@Getter
@Setter
@NoArgsConstructor
public class MeetingCreateRequest {

    @NotBlank(message = "약속 제목은 필수입니다")
    @Size(max = 255, message = "약속 제목은 255자를 초과할 수 없습니다")
    private String title;

    @Size(max = 1000, message = "약속 설명은 1000자를 초과할 수 없습니다")
    private String description;

    @NotNull(message = "약속 시간은 필수입니다")
    private LocalDateTime meetingTime;

    @Min(value = 1, message = "최대 참여자 수는 최소 1명 이상이어야 합니다")
    @Max(value = 10, message = "최대 참여자 수는 10명을 초과할 수 없습니다")
    private Integer maxParticipants = 10;

    // 장소 정보 (다른 서비스에서 전달받은 정보)
    @NotBlank(message = "장소명은 필수입니다")
    @Size(max = 500, message = "장소명은 500자를 초과할 수 없습니다")
    private String locationName;

    @Size(max = 500, message = "주소는 500자를 초과할 수 없습니다")
    private String locationAddress;

    private String locationCoordinates; // JSON 형태로 위도,경도

    // 초대할 친구 ID 목록
    @NotNull(message = "초대할 친구 목록은 필수입니다")
    private List<Long> participantUserIds;
}

