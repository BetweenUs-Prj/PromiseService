package com.promiseservice.model.enums;

/**
 * 참가자 상태 enum
 * 이유: 약속 참가자의 현재 상태를 정의하여 참가 과정을 관리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum ParticipantStatus {
    /**
     * 초대됨
     */
    INVITED,
    
    /**
     * 확정됨
     */
    CONFIRMED,
    
    /**
     * 거절됨
     */
    DECLINED,
    
    /**
     * 아마도
     */
    MAYBE,
    
    /**
     * 취소됨
     */
    CANCELLED;

    /**
     * 응답했는지 여부
     * 이유: 참가자가 초대에 응답했는지 확인하기 위해
     */
    public boolean hasResponded() {
        return this == CONFIRMED || this == DECLINED || this == MAYBE;
    }
}
