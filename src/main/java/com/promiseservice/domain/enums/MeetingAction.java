package com.promiseservice.domain.enums;

/**
 * 약속 액션 enum
 * 이유: 약속에서 수행될 수 있는 액션들을 정의하여 히스토리 추적을 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum MeetingAction {
    /**
     * 생성됨
     */
    CREATED,
    
    /**
     * 수정됨
     */
    UPDATED,
    
    /**
     * 취소됨
     */
    CANCELLED,
    
    /**
     * 완료됨
     */
    COMPLETED,
    
    /**
     * 참가자 추가됨
     */
    PARTICIPANT_ADDED,
    
    /**
     * 참가자 제거됨
     */
    PARTICIPANT_REMOVED
}
