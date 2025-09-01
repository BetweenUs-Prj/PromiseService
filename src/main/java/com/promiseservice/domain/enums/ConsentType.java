package com.promiseservice.domain.enums;

/**
 * 동의 유형 enum
 * 이유: 사용자가 동의할 수 있는 다양한 동의 유형을 정의하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum ConsentType {
    /**
     * 마케팅
     */
    MARKETING,
    
    /**
     * 푸시 알림
     */
    PUSH_NOTIFICATION,
    
    /**
     * 위치 공유
     */
    LOCATION_SHARING,
    
    /**
     * 친구 추천
     */
    FRIEND_RECOMMENDATION,
    
    /**
     * 데이터 분석
     */
    DATA_ANALYTICS
}
