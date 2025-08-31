package com.promiseservice.enums;

/**
 * 친구 관계 상태를 나타내는 열거형
 * 이유: 친구 관계의 다양한 상태를 타입 안전하게 관리
 */
public enum FriendshipStatus {
    /**
     * 대기 중 상태
     * 이유: 친구 요청이 보내졌지만 아직 응답하지 않은 상태
     */
    PENDING,
    
    /**
     * 수락된 상태
     * 이유: 친구 요청이 수락되어 친구 관계가 성립된 상태
     */
    ACCEPTED,
    
    /**
     * 차단된 상태
     * 이유: 사용자가 특정 사용자를 차단한 상태
     */
    BLOCKED
}


















