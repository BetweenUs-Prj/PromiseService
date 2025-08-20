package com.promiseservice.dto;

import com.promiseservice.domain.entity.Meeting;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class MeetingResponse {

    private Long id;
    private Long hostId;
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

    public static MeetingResponse from(Meeting meeting) {
        MeetingResponse response = new MeetingResponse();
        response.setId(meeting.getId());
        response.setHostId(meeting.getHostId());
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
        
        // 참여자 정보 변환
        if (meeting.getParticipants() != null) {
            response.setParticipants(
                meeting.getParticipants().stream()
                    .map(ParticipantResponse::from)
                    .collect(Collectors.toList())
            );
        }
        
        return response;
    }
}

