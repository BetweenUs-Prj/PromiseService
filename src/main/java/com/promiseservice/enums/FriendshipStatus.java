package com.promiseservice.enums;

/**
 * 친구 관계 상태를 나타내는 열거형
 * 이유: 친구 관계의 생명주기를 체계적으로 관리하고 각 단계에 맞는 비즈니스 로직을 적용하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum FriendshipStatus {
    
    /** 대기 중: 친구 요청이 발송되었지만 아직 응답하지 않은 상태 */
    PENDING("대기 중"),
    
    /** 수락: 친구 요청이 수락되어 친구 관계가 성립된 상태 */
    ACCEPTED("수락"),
    
    /** 활성: 정상적인 친구 관계 상태 */
    ACTIVE("활성"),
    
    /** 차단: 한쪽 사용자가 상대방을 차단한 상태 */
    BLOCKED("차단"),
    
    /** 삭제: 친구 관계가 삭제된 상태 */
    DELETED("삭제"),
    
    /** 일시정지: 친구 관계가 일시적으로 정지된 상태 */
    SUSPENDED("일시정지");

    private final String displayName;

    /**
     * FriendshipStatus 생성자
     * 이유: 각 상태별로 사용자에게 표시될 한글 이름을 설정하여
     * UI에서 직관적인 상태 정보를 제공하기 위해
     */
    FriendshipStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 상태의 사용자 친화적인 표시명을 반환하는 메서드
     * 이유: 알림 메시지, UI 화면에서 친구 관계 상태를 사용자가 이해하기 쉽게 표시하기 위해
     * 
     * @return 상태의 한글 표시명
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * 친구 관계가 활성 상태인지 확인하는 메서드
     * 이유: 활성 상태인 친구 관계에 대한 특별한 처리 로직을 적용하기 위해
     * 
     * @return 활성 상태 여부 (ACTIVE인 경우 true)
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 친구 관계가 차단된 상태인지 확인하는 메서드
     * 이유: 차단된 친구 관계에 대한 특별한 처리 로직을 적용하기 위해
     * 
     * @return 차단 상태 여부 (BLOCKED인 경우 true)
     */
    public boolean isBlocked() {
        return this == BLOCKED;
    }

    /**
     * 친구 관계가 정상적으로 유지 가능한 상태인지 확인하는 메서드
     * 이유: 정상적인 친구 관계 기능을 제공할 수 있는 상태인지 확인하기 위해
     * 
     * @return 정상 상태 여부 (ACTIVE인 경우 true)
     */
    public boolean isNormal() {
        return this == ACTIVE;
    }
}
