package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class MeetingStatusUpdateRequest {

    @NotNull(message = "약속 상태는 필수입니다")
    private String status;

    @Size(max = 500, message = "상태 변경 사유는 500자를 초과할 수 없습니다")
    private String reason;
}

