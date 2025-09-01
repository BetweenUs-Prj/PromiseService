package com.promiseservice.domain.enums;

/**
 * 참가자 역할 enum
 * 이유: 약속 참가자의 역할을 정의하여 권한과 책임을 구분하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum ParticipantRole {
    /**
     * 주최자
     */
    ORGANIZER,
    
    /**
     * 참가자
     */
    PARTICIPANT,
    
    /**
     * 게스트
     */
    GUEST
}
