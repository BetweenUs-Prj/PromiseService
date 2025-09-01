package com.promiseservice.model.enums;

/**
 * 알림 유형 enum
 * 이유: 시스템에서 발송할 수 있는 다양한 알림 유형을 정의하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum NotificationType {
    /**
     * 친구 요청
     */
    FRIEND_REQUEST,
    
    /**
     * 약속 초대
     */
    MEETING_INVITE,
    
    /**
     * 약속 업데이트
     */
    MEETING_UPDATE,
    
    /**
     * 약속 리마인더
     */
    MEETING_REMINDER,
    
    /**
     * 시스템 알림
     */
    SYSTEM
}
