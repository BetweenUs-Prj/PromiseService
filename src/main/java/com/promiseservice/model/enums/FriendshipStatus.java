package com.promiseservice.model.enums;

/**
 * 친구 관계 상태 열거형
 * 이유: 친구 관계의 상태를 명확하게 정의하고 타입 안전성을 보장하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum FriendshipStatus {
    /**
     * 대기 중
     * 이유: 친구 관계가 아직 확정되지 않은 상태를 나타내기 위해
     */
    PENDING,

    /**
     * 수락됨
     * 이유: 친구 관계가 성립된 상태를 나타내기 위해
     */
    ACCEPTED,

    /**
     * 차단됨
     * 이유: 사용자가 특정 사용자를 차단한 상태를 나타내기 위해
     */
    BLOCKED
}