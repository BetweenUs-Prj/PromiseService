package com.promiseservice.enums;

/**
 * 약속 상태를 나타내는 열거형
 * 이유: 약속의 생명주기를 체계적으로 관리하고 각 단계에 맞는 비즈니스 로직을 적용하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum MeetingStatus {

    /** 초대 중: 약속이 생성되었고 참가자들에게 초대가 발송된 상태 */
    INVITING("초대 중"),

    /** 대기 중: 초대가 발송되었지만 아직 모든 참가자의 응답을 기다리는 상태 */
    PENDING("대기 중"),

    /** 확정: 모든 참가자가 응답하여 약속이 확정된 상태 */
    CONFIRMED("확정"),

    /** 진행 중: 약속 시간이 되어 진행 중인 상태 */
    IN_PROGRESS("진행 중"),

    /** 완료: 약속이 성공적으로 완료된 상태 */
    COMPLETED("완료"),

    /** 취소: 약속이 취소된 상태 */
    CANCELLED("취소");

    private final String displayName;

    /**
     * MeetingStatus 생성자
     * 이유: 각 상태별로 사용자에게 표시될 한글 이름을 설정하여
     * UI에서 직관적인 상태 정보를 제공하기 위해
     */
    MeetingStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 상태의 사용자 친화적인 표시명을 반환하는 메서드
     * 이유: 알림 메시지, UI 화면에서 약속 상태를 사용자가 이해하기 쉽게 표시하기 위해
     *
     * @return 상태의 한글 표시명
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * 약속이 초대 단계인지 확인하는 메서드
     * 이유: 초대 단계에서 특별한 처리 로직을 적용하기 위해
     *
     * @return 초대 단계 여부 (INVITING, PENDING인 경우 true)
     */
    public boolean isInvitingPhase() {
        return this == INVITING || this == PENDING;
    }

    /**
     * 약속이 확정된 상태인지 확인하는 메서드
     * 이유: 확정된 약속에 대한 특별한 처리 로직을 적용하기 위해
     *
     * @return 확정 상태 여부 (CONFIRMED, IN_PROGRESS, COMPLETED인 경우 true)
     */
    public boolean isConfirmed() {
        return this == CONFIRMED || this == IN_PROGRESS || this == COMPLETED;
    }

    /**
     * 약속이 활성 상태인지 확인하는 메서드
     * 이유: 활성 상태인 약속에 대한 특별한 처리 로직을 적용하기 위해
     *
     * @return 활성 상태 여부 (INVITING, PENDING, CONFIRMED, IN_PROGRESS인 경우 true)
     */
    public boolean isActive() {
        return this == INVITING || this == PENDING || this == CONFIRMED || this == IN_PROGRESS;
    }

    /**
     * 약속이 완료된 상태인지 확인하는 메서드
     * 이유: 완료된 약속에 대한 특별한 처리 로직을 적용하기 위해
     *
     * @return 완료 상태 여부 (COMPLETED, CANCELLED인 경우 true)
     */
    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
