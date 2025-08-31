package com.promiseservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 카카오톡 알림 전송 요청 DTO
 * 이유: 클라이언트로부터 카카오톡 알림 전송 요청 정보를 받기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class NotifyKakaoRequest {

    @NotNull(message = "약속 ID는 필수입니다")
    private Long meetingId;

    @Size(min = 1, max = 20, message = "수신자는 1명 이상 20명 이하여야 합니다")
    private List<Long> receiverIds;

    /**
     * 생성자
     * 이유: 테스트나 특정 상황에서 객체를 편리하게 생성하기 위해
     * 
     * @param meetingId 약속 ID
     * @param receiverIds 수신자 ID 목록
     */
    public NotifyKakaoRequest(Long meetingId, List<Long> receiverIds) {
        this.meetingId = meetingId;
        this.receiverIds = receiverIds;
    }

    /**
     * 수신자가 있는지 확인하는 메서드
     * 이유: 수신자 목록이 유효한지 빠르게 확인하기 위해
     * 
     * @return 수신자 존재 여부
     */
    public boolean hasReceivers() {
        return receiverIds != null && !receiverIds.isEmpty();
    }

    /**
     * 수신자 수를 반환하는 메서드
     * 이유: 로깅이나 배치 처리 시 수신자 수를 확인하기 위해
     * 
     * @return 수신자 수
     */
    public int getReceiverCount() {
        return receiverIds != null ? receiverIds.size() : 0;
    }
}
