package com.promiseservice.enums;

/**
 * OAuth 제공자 열거형
 * 이유: 카카오, 구글 등 다양한 OAuth 제공자를 표준화된 방식으로 관리하기 위해
 * DB에 저장될 때는 문자열 값이 사용되므로 정확한 값으로 정의
 */
public enum Provider {
    
    KAKAO("KAKAO"),      // 카카오 OAuth
    GOOGLE("GOOGLE"),    // 구글 OAuth
    NAVER("NAVER"),      // 네이버 OAuth
    APPLE("APPLE");      // 애플 OAuth
    
    private final String value;
    
    Provider(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
