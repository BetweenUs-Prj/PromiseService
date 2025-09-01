package com.promiseservice.enums;

/**
 * 약속 참가자 상태를 나타내는 열거형
 * 이유: 참가자의 약속 참여 생명주기를 체계적으로 관리하고 각 단계에 맞는 비즈니스 로직을 적용하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum ParticipantStatus {

    /** 초대됨: 약속에 초대되었지만 아직 응답하지 않은 상태 */
    INVITED("초대됨"),

    /** 수락: 약속 초대를 수락한 상태 */
    ACCEPTED("수락"),

    /** 거부: 약속 초대를 거부한 상태 */
    DECLINED("거부"),

    /** 참가: 약속에 실제로 참가한 상태 */
    JOINED("참가"),

    /** 나감: 약속에서 나간 상태 */
    LEFT("나감");

    private final String displayName;

    /**
     * ParticipantStatus 생성자
     * 이유: 각 상태별로 사용자에게 표시될 한글 이름을 설정하여
     * UI에서 직관적인 상태 정보를 제공하기 위해
     */
    ParticipantStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 상태의 사용자 친화적인 표시명을 반환하는 메서드
     * 이유: 알림 메시지, UI 화면에서 참가자 상태를 사용자가 이해하기 쉽게 표시하기 위해
     *
     * @return 상태의 한글 표시명
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * 초대 상태인지 확인하는 메서드
     * 이유: 초대 상태에서 특별한 처리 로직을 적용하기 위해
     *
     * @return 초대 상태 여부 (INVITED인 경우 true)
     */
    public boolean isInvited() {
        return this == INVITED;
    }

    /**
     * 응답 완료 상태인지 확인하는 메서드
     * 이유: 응답 완료 상태에서 특별한 처리 로직을 적용하기 위해
     *
     * @return 응답 완료 상태 여부 (ACCEPTED, DECLINED인 경우 true)
     */
    public boolean hasResponded() {
        return this == ACCEPTED || this == DECLINED;
    }

    /**
     * 참가 가능한 상태인지 확인하는 메서드
     * 이유: 참가 가능한 상태에서 특별한 처리 로직을 적용하기 위해
     *
     * @return 참가 가능 상태 여부 (ACCEPTED, JOINED인 경우 true)
     */
    public boolean canParticipate() {
        return this == ACCEPTED || this == JOINED;
    }

    /**
     * 최종 상태인지 확인하는 메서드
     * 이유: 최종 상태에서 특별한 처리 로직을 적용하기 위해
     *
     * @return 최종 상태 여부 (JOINED, LEFT, DECLINED인 경우 true)
     */
    public boolean isFinal() {
        return this == JOINED || this == LEFT || this == DECLINED;
    }
}
