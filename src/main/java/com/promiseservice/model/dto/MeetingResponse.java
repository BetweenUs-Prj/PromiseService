package com.promiseservice.dto;

import com.promiseservice.model.entity.Meeting;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Meeting 엔티티에 대한 응답 DTO
 * 이유: 클라이언트에게 약속 정보를 전달할 때 필요한 데이터만 노출하고 민감한 정보를 보호하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class MeetingResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime meetingTime;
    private Integer maxParticipants;
    private String status;
    private String locationName;
    private String locationAddress;
    private String locationCoordinates;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 참여자 정보
    private List<ParticipantResponse> participants;
    
    // 추가 정보
    private Integer currentParticipantCount; // 현재 참여자 수
    private Boolean isMaxParticipantsReached; // 최대 인원 도달 여부

    /**
     * Meeting 엔티티로부터 MeetingResponse 생성
     * 이유: 엔티티를 DTO로 변환하여 API 응답에 적합한 형태로 가공
     * 
     * @param meeting Meeting 엔티티
     * @return MeetingResponse 객체
     */
    public static MeetingResponse from(Meeting meeting) {
        MeetingResponse response = new MeetingResponse();
        response.setId(meeting.getId());
        response.setTitle(meeting.getTitle());
        response.setDescription(meeting.getDescription());
        response.setMeetingTime(meeting.getMeetingTime());
        response.setMaxParticipants(meeting.getMaxParticipants());
        response.setStatus(meeting.getStatus().name());
        response.setLocationName(meeting.getLocationName());
        response.setLocationAddress(meeting.getLocationAddress());
        response.setLocationCoordinates(meeting.getLocationCoordinates());
        response.setCreatedAt(meeting.getCreatedAt());
        response.setUpdatedAt(meeting.getUpdatedAt());
        
        // 참가자 정보 변환 (Meeting 엔티티의 participants 관계 사용)
        if (meeting.getParticipants() != null && !meeting.getParticipants().isEmpty()) {
            response.setParticipants(
                meeting.getParticipants().stream()
                    .map(ParticipantResponse::from)
                    .collect(Collectors.toList())
            );
            response.setCurrentParticipantCount(meeting.getParticipants().size());
        } else {
            response.setParticipants(List.of());
            response.setCurrentParticipantCount(0);
        }
        
        // 최대 인원 도달 여부 설정
        response.setIsMaxParticipantsReached(
            response.getCurrentParticipantCount() >= meeting.getMaxParticipants()
        );
        
        return response;
    }
}

