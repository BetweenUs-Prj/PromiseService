package com.promiseservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 약속 초대 요청 DTO
 * 이유: 클라이언트로부터 약속에 새로운 참여자를 초대하는데 필요한 정보를 받아서 서버에서 처리하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingInviteRequest {

    /**
     * 초대할 사용자 ID 목록
     * 이유: 시스템에 등록된 사용자들을 약속에 초대하기 위해
     */
    @NotEmpty(message = "초대할 사용자 ID 목록은 필수입니다")
    @Size(max = 50, message = "한 번에 초대할 수 있는 사용자는 50명을 초과할 수 없습니다")
    private List<Long> userIds;

    /**
     * 초대할 카카오 ID 목록 (선택사항)
     * 이유: 카카오 친구 중에서 약속에 초대할 사용자들을 지정하기 위해
     */
    @Size(max = 50, message = "한 번에 초대할 수 있는 카카오 사용자는 50명을 초과할 수 없습니다")
    private List<String> kakaoIds;


    /**
     * 초대 메시지 (선택사항)
     * 이유: 초대 시 전송할 개인화된 메시지를 제공하기 위해
     */
    @Size(max = 500, message = "초대 메시지는 500자를 초과할 수 없습니다")
    private String message;
}
