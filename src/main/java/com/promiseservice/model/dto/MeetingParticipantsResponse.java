package com.promiseservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 약속 참가자 목록 응답 DTO
 * 이유: 약속에 참가하는 사용자들의 목록을 클라이언트에게 전달하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingParticipantsResponse {

    /**
     * 참가자 정보 목록
     * 이유: 참가자들의 상세 정보를 제공하기 위해
     */
    private List<ParticipantInfo> items;

    /**
     * 참가자 정보 DTO
     * 이유: 개별 참가자의 정보를 구조화하여 제공하기 위해
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {

        /**
         * 사용자 ID
         * 이유: 참가자를 고유하게 식별하기 위해
         */
        private Long userId;

        /**
         * 사용자 이름
         * 이유: 참가자의 이름을 표시하기 위해
         */
        private String name;

        /**
         * 참가자 역할
         * 이유: 참가자가 약속에서 어떤 역할을 하는지 구분하기 위해
         */
        private String role;

        /**
         * 참가 상태
         * 이유: 참가자의 현재 상태를 표시하기 위해
         */
        private String status;
    }
}
