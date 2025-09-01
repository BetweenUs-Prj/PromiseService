package com.promiseservice.domain.enums;

/**
 * 선호하는 교통수단 enum
 * 이유: 사용자가 선호하는 교통수단을 정의하여 약속 장소 추천이나 경로 안내에 활용하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum PreferredTransport {
    /**
     * 도보
     */
    WALKING,
    
    /**
     * 자전거
     */
    BICYCLE,
    
    /**
     * 자동차
     */
    CAR,
    
    /**
     * 대중교통
     */
    PUBLIC_TRANSPORT,
    
    /**
     * 혼합
     */
    MIXED
}
