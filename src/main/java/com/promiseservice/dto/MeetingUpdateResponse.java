package com.promiseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 약속 수정 응답 DTO
 * 이유: 약속 수정 결과를 클라이언트에게 전달하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUpdateResponse {

    /**
     * 수정 성공 여부
     * 이유: 약속 정보가 성공적으로 수정되었는지 여부를 알려주기 위해
     */
    private boolean updated;
}
