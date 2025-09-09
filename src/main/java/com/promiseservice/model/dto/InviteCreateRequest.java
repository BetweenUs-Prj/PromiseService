package com.promiseservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** 초대 생성 요청 Body: POST /api/invites */
@Getter @Setter
public class InviteCreateRequest {

    @NotNull
    private Long meetingId;

    @Size(max = 200)
    private String message;

    @NotNull
    @Size(min = 1)
    private List<Long> inviteeIds;
}
