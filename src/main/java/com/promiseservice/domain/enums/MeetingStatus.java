package com.promiseservice.domain.enums;

/**
 * 약속 진행 상태 enum
 * 이유: 약속의 현재 상태를 정의하여 약속 진행 과정을 관리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum MeetingStatus {
    /**
     * 대기 중
     */
    WAITING,
    
    /**
     * 확정됨
     */
    CONFIRMED,
    
    /**
     * 완료됨
     */
    COMPLETED,
    
    /**
     * 취소됨
     */
    CANCELLED
}
