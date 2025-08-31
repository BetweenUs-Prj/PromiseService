package com.promiseservice.enums;

/**
 * 선호하는 교통수단을 나타내는 열거형
 * 이유: 사용자의 이동 패턴 정보를 타입 안전하게 관리
 */
public enum PreferredTransport {
    /**
     * 도보
     * 이유: 짧은 거리 이동 시 선호
     */
    WALK,
    
    /**
     * 자전거
     * 이유: 친환경적이고 건강한 이동 수단
     */
    BICYCLE,
    
    /**
     * 대중교통
     * 이유: 대중교통 이용 선호 사용자
     */
    PUBLIC_TRANSPORT,
    
    /**
     * 자동차
     * 이유: 편리하고 빠른 이동 선호 사용자
     */
    CAR,
    
    /**
     * 오토바이
     * 이유: 빠르고 기동성 높은 이동 수단 선호 사용자
     */
    MOTORCYCLE
}



