package com.promiseservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 약속 생성 응답 DTO
 * 이유: 약속 생성 완료 후 클라이언트에게 생성된 약속의 상세 정보를 반환하여
 * 사용자가 약속 정보를 확인하고 추가 작업을 진행할 수 있도록 지원
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCreateResponse {

    /**
     * 생성된 약속의 고유 식별자
     * 이유: 클라이언트가 생성된 약속을 고유하게 식별하고
     * 이후 약속 수정, 조회, 참여자 관리 등의 작업을 수행할 수 있도록 지원
     */
    private Long meetingId;

    /**
     * 약속의 현재 상태
     * 이유: 생성 직후 약속의 상태를 명확히 표시하여
     * 사용자가 약속의 진행 단계를 파악할 수 있도록 지원
     */
    private String status;

    /**
     * 약속 방장(호스트) 정보
     * 이유: 약속을 생성한 사용자의 정보를 제공하여
     * 방장 권한과 책임을 명확히 하고 참여자들이 연락할 수 있도록 지원
     */
    private HostInfo host;

    /**
     * 약속 장소 정보
     * 이유: 외부 장소 서비스에서 가져온 정확한 장소 정보를 제공하여
     * 참여자들이 약속 장소를 쉽게 찾을 수 있도록 지원
     */
    private PlaceInfo place;

    /**
     * 약속 참여자 목록
     * 이유: 현재 약속에 참여하고 있는 사용자들의 정보를 제공하여
     * 참여자 현황을 실시간으로 파악할 수 있도록 지원
     */
    private List<ParticipantInfo> participants;

    /**
     * 약속 방장(호스트) 정보를 담는 내부 클래스
     * 이유: 방장의 기본 정보를 구조화하여 응답 데이터의 가독성을 향상시키고
     * 향후 방장 관련 추가 정보 확장 시 유연성을 보장하기 위해
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HostInfo {

        /**
         * 방장 사용자 ID
         * 이유: 방장을 고유하게 식별하고 권한 검증에 활용하기 위해
         */
        private Long userId;

        /**
         * 방장 사용자 이름
         * 이유: UI에서 방장을 사용자 친화적으로 표시하기 위해
         */
        private String name;

        /**
         * 방장 사용자 프로필 이미지 URL (선택사항)
         * 이유: UI에서 방장을 시각적으로 식별할 수 있도록 지원
         */
        private String avatarUrl;
    }

    /**
     * 약속 장소 정보를 담는 내부 클래스
     * 이유: 장소 관련 정보를 구조화하여 응답 데이터의 가독성을 향상시키고
     * 지도 서비스 연동 시 필요한 좌표 정보를 체계적으로 제공하기 위해
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceInfo {

        /**
         * 장소 고유 식별자
         * 이유: 외부 장소 서비스와의 연동 및 장소 정보 업데이트 시 활용하기 위해
         */
        private Long placeId;

        /**
         * 장소명
         * 이유: 사용자가 약속 장소를 쉽게 인식할 수 있도록 지원
         */
        private String placeName;

        /**
         * 장소 상세 주소
         * 이유: 참여자들이 약속 장소를 정확히 찾을 수 있도록 지원
         */
        private String address;

        /**
         * 장소 위도 좌표
         * 이유: 지도 서비스 연동 및 위치 기반 기능(거리 계산, 경로 안내 등)을 제공하기 위해
         */
        private Double lat;

        /**
         * 장소 경도 좌표
         * 이유: 지도 서비스 연동 및 위치 기반 기능(거리 계산, 경로 안내 등)을 제공하기 위해
         */
        private Double lng;

        /**
         * 장소 카테고리 (선택사항)
         * 이유: 장소의 성격을 분류하여 UI에서 아이콘 표시나 필터링에 활용하기 위해
         */
        private String category;

        /**
         * 장소 전화번호 (선택사항)
         * 이유: 참여자들이 장소에 대한 추가 정보를 얻을 수 있도록 지원
         */
        private String phoneNumber;
    }

    /**
     * 약속 참여자 정보를 담는 내부 클래스
     * 이유: 참여자의 역할과 상태를 구조화하여 응답 데이터의 가독성을 향상시키고
     * 참여자별 권한과 상태를 명확하게 구분하여 제공하기 위해
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {

        /**
         * 참여자 사용자 ID
         * 이유: 참여자를 고유하게 식별하고 권한 검증에 활용하기 위해
         */
        private Long userId;

        /**
         * 참여자 사용자 이름
         * 이유: UI에서 참여자를 사용자 친화적으로 표시하기 위해
         */
        private String name;

        /**
         * 참여자 역할
         * 이유: 방장(HOST)과 일반 참여자(MEMBER)를 구분하여
         * 각각에 맞는 권한과 UI 표시를 제공하기 위해
         */
        private String role;

        /**
         * 참여자 상태
         * 이유: 참여자의 현재 상태(JOINED, INVITED, DECLINED 등)를 표시하여
         * 참여자 현황을 실시간으로 파악할 수 있도록 지원
         */
        private String status;

        /**
         * 참여자 프로필 이미지 URL (선택사항)
         * 이유: UI에서 참여자를 시각적으로 식별할 수 있도록 지원
         */
        private String avatarUrl;

        /**
         * 참여 응답 시점 (선택사항)
         * 이유: 참여자가 언제 참여 의사를 표명했는지 기록하여
         * 참여 순서나 응답 시간 분석에 활용하기 위해
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime respondedAt;
    }

    /**
     * 약속 생성 응답을 생성하는 정적 팩토리 메서드
     * 이유: Meeting 엔티티로부터 응답 DTO를 생성하는 로직을 캡슐화하여
     * 코드 재사용성을 높이고 일관된 응답 형식을 보장하기 위해
     * 
     * @param meetingId 생성된 약속 ID
     * @param status 약속 상태
     * @param host 방장 정보
     * @param place 장소 정보
     * @param participants 참여자 목록
     * @return MeetingCreateResponse 인스턴스
     */
    public static MeetingCreateResponse of(Long meetingId, String status, HostInfo host, 
                                         PlaceInfo place, List<ParticipantInfo> participants) {
        return MeetingCreateResponse.builder()
                .meetingId(meetingId)
                .status(status)
                .host(host)
                .place(place)
                .participants(participants)
                .build();
    }

    /**
     * 약속 생성 응답의 기본값을 설정하는 메서드
     * 이유: 필수 필드가 누락된 경우 기본값을 설정하여
     * 클라이언트에서 안전하게 데이터를 처리할 수 있도록 지원
     */
    public void setDefaults() {
        if (status == null) {
            status = "OPEN";
        }
        if (participants == null) {
            participants = List.of();
        }
    }
}
