package com.promiseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 약속 확정 요청 DTO
 * 이유: 방장이 약속을 확정할 때 필요한 정보를 전달받기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingConfirmRequest {

    /**
     * 약속 ID
     * 이유: 어떤 약속을 확정할지 식별하기 위해
     */
    @NotNull(message = "약속 ID는 필수입니다")
    private Long meetingId;

    /**
     * 확정 메시지 (선택사항)
     * 이유: 약속 확정 시 참가자들에게 보낼 메시지를 제공하기 위해
     */
    private String confirmMessage;
}
