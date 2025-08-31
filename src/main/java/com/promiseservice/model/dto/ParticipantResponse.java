package com.promiseservice.dto;

import com.promiseservice.model.entity.MeetingParticipant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * MeetingParticipant 엔티티에 대한 응답 DTO
 * 이유: 클라이언트에게 참여자 정보를 전달할 때 필요한 데이터만 노출하고 복합키 구조를 숨기기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class ParticipantResponse {

    private Long meetingId; // 약속 ID
    private Long userId; // 사용자 ID
    private String response; // 응답 상태
    private String responseDisplayName; // 응답 상태 표시명
    private LocalDateTime joinedAt; // 실제 참여 시간
    private LocalDateTime invitedAt; // 초대된 시간
    private Boolean hasActuallyJoined; // 실제 참여 여부


    /**
     * MeetingParticipant 엔티티로부터 ParticipantResponse 생성
     * 이유: 엔티티를 DTO로 변환하여 API 응답에 적합한 형태로 가공
     * 
     * @param participant MeetingParticipant 엔티티
     * @return ParticipantResponse 객체
     */
    public static ParticipantResponse from(MeetingParticipant participant) {
        ParticipantResponse response = new ParticipantResponse();
        response.setMeetingId(participant.getMeetingId());
        response.setUserId(participant.getUserId());
        response.setResponse(participant.getResponse().name());
        response.setResponseDisplayName(participant.getResponse().getDisplayName());
        response.setJoinedAt(participant.getJoinedAt());
        response.setInvitedAt(participant.getInvitedAt());
        response.setHasActuallyJoined(participant.hasActuallyJoined());
        

        
        return response;
    }


}

