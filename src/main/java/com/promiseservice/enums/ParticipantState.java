package com.promiseservice.enums;

/**
 * 약속 참여자 상태를 나타내는 열거형
 * 이유: 약속 참여자의 응답 상태를 체계적으로 관리하여 호스트가 참여 현황을 파악하고,
 * 참여자별로 적절한 알림이나 처리를 제공할 수 있도록 지원하기 위해.
 * 초대부터 수락, 거절까지의 참여자 응답 과정을 상태로 구분하여 관리
 */
public enum ParticipantState {

    /**
     * 초대됨 상태
     * 이유: 약속에 초대되었지만 아직 응답하지 않은 상태를 표시하여
     * 아직 응답 대기 중인 참여자를 식별하고 리마인더 알림 대상으로 관리하기 위해
     */
    INVITED("초대됨"),

    /**
     * 수락 상태
     * 이유: 약속 참여를 수락한 상태를 표시하여 확정 알림 발송 대상으로 식별하고,
     * 최종 참여 인원 계산에 포함시키기 위해
     */
    ACCEPTED("수락"),

    /**
     * 거절 상태
     * 이유: 약속 참여를 거절한 상태를 표시하여 알림 발송 대상에서 제외하고,
     * 호스트에게 참여 불가 현황을 알려주기 위해
     */
    DECLINED("거절");

    private final String description;

    /**
     * ParticipantState 생성자
     * 이유: 각 상태에 대한 한국어 설명을 함께 저장하여
     * UI 표시나 상태 확인 메시지에서 사용자가 이해하기 쉬운 형태로 제공하기 위해
     * 
     * @param description 상태에 대한 한국어 설명
     */
    ParticipantState(String description) {
        this.description = description;
    }

    /**
     * 상태 설명을 반환하는 메서드
     * 이유: 열거형 값의 한국어 설명을 조회하여 사용자 인터페이스에서 활용하기 위해
     * 
     * @return 상태에 대한 한국어 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 응답 완료 상태인지 확인하는 메서드
     * 이유: 참여자가 초대에 대한 응답을 완료했는지 확인하여
     * 아직 응답하지 않은 참여자에 대한 리마인더 발송 여부를 결정하기 위해
     * 
     * @return 응답 완료 여부 (true: 응답 완료, false: 응답 대기)
     */
    public boolean hasResponded() {
        return this == ACCEPTED || this == DECLINED;
    }

    /**
     * 참여 확정 상태인지 확인하는 메서드
     * 이유: 실제로 약속에 참여할 예정인 참여자인지 확인하여
     * 확정 알림 발송 대상과 최종 참여 인원 계산에 활용하기 위해
     * 
     * @return 참여 확정 여부 (true: 참여 예정, false: 미정 또는 불참)
     */
    public boolean isParticipating() {
        return this == ACCEPTED;
    }

    /**
     * 알림 발송 대상인지 확인하는 메서드
     * 이유: 약속 관련 알림을 발송해야 할 참여자인지 확인하여
     * 불필요한 알림 발송을 방지하고 적절한 대상에게만 알림을 전송하기 위해
     * 
     * @return 알림 발송 대상 여부 (true: 알림 발송 대상, false: 알림 발송 제외)
     */
    public boolean shouldReceiveNotifications() {
        return this == ACCEPTED;
    }

    /**
     * 리마인더 알림 대상인지 확인하는 메서드
     * 이유: 아직 응답하지 않은 참여자에게 리마인더 알림을 발송해야 하는지 확인하여
     * 참여자 응답률을 높이고 약속 진행을 원활하게 하기 위해
     * 
     * @return 리마인더 대상 여부 (true: 리마인더 필요, false: 리마인더 불필요)
     */
    public boolean needsReminder() {
        return this == INVITED;
    }

    /**
     * 상태 변경이 가능한지 확인하는 메서드
     * 이유: 현재 상태에서 다른 상태로 변경이 가능한지 확인하여
     * 불가능한 상태 변경을 방지하고 일관성 있는 상태 관리를 하기 위해
     * 
     * @param newState 변경하려는 새로운 상태
     * @return 상태 변경 가능 여부 (true: 변경 가능, false: 변경 불가)
     */
    public boolean canChangeTo(ParticipantState newState) {
        // 이미 응답한 경우 재변경 허용 (마음이 바뀔 수 있음)
        // 이유: 사용자가 초기 응답 후 마음을 바꿀 수 있는 상황을 고려하여 유연성 제공
        if (this == INVITED) {
            return newState == ACCEPTED || newState == DECLINED;
        }
        
        if (this == ACCEPTED || this == DECLINED) {
            return newState == ACCEPTED || newState == DECLINED;
        }
        
        return false;
    }
}







