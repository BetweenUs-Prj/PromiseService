package com.promiseservice.model.enums;

/**
 * 친구 요청 상태 enum
 * 이유: 친구 요청의 현재 상태를 정의하여 요청 처리 과정을 관리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum FriendRequestStatus {
    /**
     * 대기 중
     */
    PENDING,
    
    /**
     * 수락됨
     */
    ACCEPTED,
    
    /**
     * 거절됨
     */
    REJECTED,
    
    /**
     * 취소됨
     */
    CANCELLED
}
