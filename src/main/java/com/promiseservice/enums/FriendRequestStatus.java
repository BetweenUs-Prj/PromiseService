package com.promiseservice.enums;

/**
 * 친구 요청 상태를 나타내는 열거형
 * 이유: 친구 요청의 생명주기를 체계적으로 관리하고 각 단계에 맞는 비즈니스 로직을 적용하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum FriendRequestStatus {
    
    /** 대기 중: 친구 요청이 발송되었지만 아직 응답하지 않은 상태 */
    PENDING("대기 중"),
    
    /** 수락: 친구 요청이 수락되어 친구 관계가 성립된 상태 */
    ACCEPTED("수락"),
    
    /** 거부: 친구 요청이 거부된 상태 */
    REJECTED("거부"),
    
    /** 취소: 친구 요청이 발송자에 의해 취소된 상태 */
    CANCELLED("취소");

    private final String displayName;

    /**
     * FriendRequestStatus 생성자
     * 이유: 각 상태별로 사용자에게 표시될 한글 이름을 설정하여
     * UI에서 직관적인 상태 정보를 제공하기 위해
     */
    FriendRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 상태의 사용자 친화적인 표시명을 반환하는 메서드
     * 이유: 알림 메시지, UI 화면에서 친구 요청 상태를 사용자가 이해하기 쉽게 표시하기 위해
     * 
     * @return 상태의 한글 표시명
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * 친구 요청 상태가 대기 중인지 확인하는 메서드
     * 이유: 대기 중인 요청에 대한 특별한 처리 로직을 적용하기 위해
     * 
     * @return 대기 중 상태 여부 (PENDING인 경우 true)
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 친구 요청 상태가 완료된 상태인지 확인하는 메서드
     * 이유: 완료된 요청에 대한 특별한 처리 로직을 적용하기 위해
     * 
     * @return 완료 상태 여부 (ACCEPTED, REJECTED, CANCELLED인 경우 true)
     */
    public boolean isFinal() {
        return this == ACCEPTED || this == REJECTED || this == CANCELLED;
    }
}
