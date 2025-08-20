package com.promiseservice.dto;

import com.promiseservice.domain.entity.MeetingParticipant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ParticipantResponse {

    private Long id;
    private Long userId;
    private String response;
    private LocalDateTime joinedAt;
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;

    public static ParticipantResponse from(MeetingParticipant participant) {
        ParticipantResponse response = new ParticipantResponse();
        response.setId(participant.getId());
        response.setUserId(participant.getUserId());
        response.setResponse(participant.getResponse().name());
        response.setJoinedAt(participant.getJoinedAt());
        response.setInvitedAt(participant.getInvitedAt());
        response.setRespondedAt(participant.getRespondedAt());
        return response;
    }
}

