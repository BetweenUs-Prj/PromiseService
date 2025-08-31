package com.promiseservice.dto;

import com.promiseservice.model.entity.Meeting;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 약속 요약 정보를 위한 응답 DTO
 * 이유: 검색 결과에서 약속의 핵심 정보만 간결하게 제공하여 목록 조회 시 성능 최적화 및 사용자 경험 향상
 */
@Getter
@Setter
@NoArgsConstructor
public class MeetingSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime meetingTime;
    private Integer maxParticipants;
    private String status;
    private String locationName;
    private String locationAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int currentParticipantCount;
    private boolean isHost;

    /**
     * Meeting 엔티티로부터 MeetingSummaryResponse 객체 생성
     * 이유: 엔티티 데이터를 DTO로 변환하여 API 응답에 적합한 형태로 가공
     * 
     * @param meeting 약속 엔티티
     * @param currentParticipantCount 현재 참여자 수
     * @param isHost 현재 사용자가 방장인지 여부
     * @return MeetingSummaryResponse 객체
     */
    public static MeetingSummaryResponse from(Meeting meeting, int currentParticipantCount, boolean isHost) {
        MeetingSummaryResponse response = new MeetingSummaryResponse();
        response.setId(meeting.getId());
        response.setTitle(meeting.getTitle());
        response.setDescription(meeting.getDescription());
        response.setMeetingTime(meeting.getMeetingTime());
        response.setMaxParticipants(meeting.getMaxParticipants());
        response.setStatus(meeting.getStatus().name());
        response.setLocationName(meeting.getLocationName());
        response.setLocationAddress(meeting.getLocationAddress());
        response.setCreatedAt(meeting.getCreatedAt());
        response.setUpdatedAt(meeting.getUpdatedAt());
        response.setCurrentParticipantCount(currentParticipantCount);
        response.setHost(isHost);
        return response;
    }
}
