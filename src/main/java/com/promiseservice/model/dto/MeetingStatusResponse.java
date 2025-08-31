package com.promiseservice.dto;

import com.promiseservice.model.entity.Meeting;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MeetingStatusResponse {

    private Long meetingId;
    private String currentStatus;
    private String previousStatus;
    private String reason;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private List<StatusHistoryResponse> statusHistory;

    public static MeetingStatusResponse from(Meeting meeting, String previousStatus, String reason, Long updatedBy) {
        MeetingStatusResponse response = new MeetingStatusResponse();
        response.setMeetingId(meeting.getId());
        response.setCurrentStatus(meeting.getStatus().name());
        response.setPreviousStatus(previousStatus);
        response.setReason(reason);
        response.setUpdatedBy(updatedBy);
        response.setUpdatedAt(meeting.getUpdatedAt());
        return response;
    }
}

























