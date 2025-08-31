package com.promiseservice.enums;

/**
 * 약속 상태를 나타내는 열거형
 * 이유: 약속의 생명주기를 체계적으로 관리하여 각 단계별로 적절한 처리 로직을 적용하고,
 * 사용자에게 현재 약속 상태를 명확히 전달하기 위해.
 * 약속 생성부터 확정, 취소까지의 전체 과정을 상태로 구분하여 관리
 */
public enum AppointmentStatus {

    /**
     * 초안 상태
     * 이유: 약속이 생성되었지만 아직 참여자들에게 공개되지 않은 상태를 표시하여
     * 호스트가 약속 세부사항을 완성하기 전까지 임시 저장 상태임을 나타내기 위해
     */
    DRAFT("초안"),

    /**
     * 대기 상태
     * 이유: 참여자들에게 초대가 발송되었지만 아직 모든 응답이 완료되지 않은 상태를 표시하여
     * 참여자들의 응답을 기다리는 중임을 나타내기 위해
     */
    PENDING("대기중"),

    /**
     * 확정 상태
     * 이유: 약속이 최종 확정되어 모든 참여자에게 확정 알림이 전송된 상태를 표시하여
     * 더 이상 변경이 어려운 확정된 약속임을 나타내기 위해
     */
    CONFIRMED("확정"),

    /**
     * 취소 상태
     * 이유: 약속이 취소되어 더 이상 진행되지 않는 상태를 표시하여
     * 관련된 알림이나 처리를 중단하고 참여자들에게 취소 안내를 하기 위해
     */
    CANCELED("취소");

    private final String description;

    /**
     * AppointmentStatus 생성자
     * 이유: 각 상태에 대한 한국어 설명을 함께 저장하여
     * UI 표시나 사용자 안내 메시지에서 이해하기 쉬운 형태로 제공하기 위해
     * 
     * @param description 상태에 대한 한국어 설명
     */
    AppointmentStatus(String description) {
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
     * 수정 가능한 상태인지 확인하는 메서드
     * 이유: 약속 정보를 수정할 수 있는 상태인지 판단하여
     * 불필요한 수정 시도를 방지하고 적절한 권한 제어를 수행하기 위해
     * 
     * @return 수정 가능 여부 (true: 수정 가능, false: 수정 불가)
     */
    public boolean isEditable() {
        return this == DRAFT || this == PENDING;
    }

    /**
     * 참여자 초대가 가능한 상태인지 확인하는 메서드
     * 이유: 새로운 참여자를 초대할 수 있는 상태인지 판단하여
     * 적절한 타이밍에만 초대 기능을 활성화하기 위해
     * 
     * @return 초대 가능 여부 (true: 초대 가능, false: 초대 불가)
     */
    public boolean canInviteParticipants() {
        return this == DRAFT || this == PENDING;
    }

    /**
     * 확정 알림을 발송할 수 있는 상태인지 확인하는 메서드
     * 이유: 확정 알림 발송이 적절한 상태인지 확인하여
     * 잘못된 타이밍의 알림 발송을 방지하기 위해
     * 
     * @return 확정 알림 발송 가능 여부 (true: 발송 가능, false: 발송 불가)
     */
    public boolean canSendConfirmationNotification() {
        return this == CONFIRMED;
    }

    /**
     * 취소 알림을 발송할 수 있는 상태인지 확인하는 메서드
     * 이유: 취소 알림 발송이 필요한 상태인지 확인하여
     * 적절한 취소 안내를 참여자들에게 제공하기 위해
     * 
     * @return 취소 알림 발송 가능 여부 (true: 발송 가능, false: 발송 불가)
     */
    public boolean canSendCancellationNotification() {
        return this == CANCELED;
    }

    /**
     * 활성 상태인지 확인하는 메서드
     * 이유: 약속이 현재 진행 중인 활성 상태인지 확인하여
     * 통계 계산이나 목록 표시에서 활용하기 위해
     * 
     * @return 활성 상태 여부 (true: 활성 약속, false: 비활성 약속)
     */
    public boolean isActive() {
        return this != CANCELED;
    }

    /**
     * 완료된 상태인지 확인하는 메서드
     * 이유: 약속이 완료되어 더 이상 변경이나 관리가 필요하지 않은 상태인지 확인하여
     * 아카이빙이나 정리 작업의 대상으로 식별하기 위해
     * 
     * @return 완료 상태 여부 (true: 완료된 약속, false: 진행 중인 약속)
     */
    public boolean isCompleted() {
        return this == CONFIRMED || this == CANCELED;
    }
}







