package com.promiseservice.model.enums;

/**
 * 친구 관계 상태 enum
 * 이유: 친구 관계의 현재 상태를 정의하여 관계 관리를 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum FriendshipStatus {
    /**
     * 대기 중
     */
    PENDING,
    
    /**
     * 수락됨
     */
    ACCEPTED,
    
    /**
     * 차단됨
     */
    BLOCKED
}
