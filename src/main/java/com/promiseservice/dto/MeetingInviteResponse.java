package com.promiseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 약속 초대 응답 DTO
 * 이유: 약속 초대 결과를 클라이언트에게 제공하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingInviteResponse {

    /**
     * 초대 성공한 사용자 목록
     * 이유: 초대가 성공적으로 발송된 사용자들의 정보를 제공하기 위해
     */
    private List<InvitedParticipant> invited;

    /**
     * 카카오 알림 발송 여부
     * 이유: 카카오톡 알림이 발송되었는지 여부를 표시하기 위해
     */
    private boolean kakaoSent;

    /**
     * 초대 실패한 사용자 목록
     * 이유: 초대 발송에 실패한 사용자들과 실패 이유를 제공하기 위해
     */
    private List<String> errors;

    /**
     * 초대된 참가자 정보 내부 클래스
     * 이유: 초대가 성공한 참가자의 정보를 구조화하여 제공하기 위해
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvitedParticipant {
        private Long userId;
        private String status;
    }
}
