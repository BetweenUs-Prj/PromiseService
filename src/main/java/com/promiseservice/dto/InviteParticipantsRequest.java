package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InviteParticipantsRequest {

    @NotNull(message = "초대할 사용자 ID 목록은 필수입니다")
    @NotEmpty(message = "초대할 사용자 ID 목록은 비어있을 수 없습니다")
    private List<Long> participantUserIds;
}

