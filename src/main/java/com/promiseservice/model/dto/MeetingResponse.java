package com.promiseservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 약속 응답 DTO
 * 이유: 클라이언트에게 약속의 상세 정보를 제공하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {

    /**
     * 약속 고유 식별자
     * 이유: 약속을 고유하게 구분하기 위한 기본 키
     */
    private Long meetingId;

    /**
     * 약속 제목
     * 이유: 약속의 제목을 표시하기 위해
     */
    private String title;

    /**
     * 약속 상태
     * 이유: 약속의 현재 상태를 표시하기 위해
     */
    private String status;

    /**
     * 호스트 정보
     * 이유: 약속을 만든 사람의 정보를 제공하기 위해
     */
    private HostInfo host;

    /**
     * 장소 정보
     * 이유: 약속이 진행될 장소의 정보를 제공하기 위해
     */
    private PlaceInfo place;

    /**
     * 참가자 목록
     * 이유: 약속에 참가하는 사용자들의 정보를 제공하기 위해
     */
    private List<ParticipantInfo> participants;

    /**
     * 호스트 정보 내부 클래스
     * 이유: 호스트의 상세 정보를 구조화하여 제공하기 위해
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HostInfo {
        private Long userId;
        private String name;
    }

    /**
     * 장소 정보 내부 클래스
     * 이유: 장소의 상세 정보를 구조화하여 제공하기 위해
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceInfo {
        private Long placeId;
        private String placeName;
        private String address;
    }

    /**
     * 참가자 정보 내부 클래스
     * 이유: 참가자의 상세 정보를 구조화하여 제공하기 위해
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private Long userId;
        private String role;
        private String status;
    }
}
