package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InviteResponse {

    private Long meetingId;
    private List<Long> successfullyInvited;
    private List<Long> alreadyInvited;
    private List<Long> failedToInvite;
    private String message;

    public InviteResponse(Long meetingId, List<Long> successfullyInvited, 
                         List<Long> alreadyInvited, List<Long> failedToInvite) {
        this.meetingId = meetingId;
        this.successfullyInvited = successfullyInvited;
        this.alreadyInvited = alreadyInvited;
        this.failedToInvite = failedToInvite;
        
        // 메시지 생성
        StringBuilder sb = new StringBuilder();
        if (!successfullyInvited.isEmpty()) {
            sb.append("성공적으로 초대된 사용자: ").append(successfullyInvited.size()).append("명");
        }
        if (!alreadyInvited.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("이미 초대된 사용자: ").append(alreadyInvited.size()).append("명");
        }
        if (!failedToInvite.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("초대 실패한 사용자: ").append(failedToInvite.size()).append("명");
        }
        this.message = sb.toString();
    }
}

