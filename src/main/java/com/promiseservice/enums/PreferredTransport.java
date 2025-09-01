package com.promiseservice.enums;

/**
 * 선호하는 교통수단을 나타내는 열거형
 * 이유: 사용자의 이동 선호도를 파악하여 약속 장소 추천이나 경로 안내에 활용하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
public enum PreferredTransport {
    
    /** 대중교통 (지하철, 버스) */
    PUBLIC_TRANSPORT("대중교통"),
    
    /** 자동차 */
    CAR("자동차"),
    
    /** 자전거 */
    BICYCLE("자전거"),
    
    /** 도보 */
    WALKING("도보"),
    
    /** 오토바이 */
    MOTORCYCLE("오토바이"),
    
    /** 택시 */
    TAXI("택시"),
    
    /** 기타 */
    OTHER("기타");

    private final String displayName;

    /**
     * PreferredTransport 생성자
     * 이유: 각 교통수단별로 사용자에게 표시될 한글 이름을 설정하여
     * UI에서 직관적인 교통수단 정보를 제공하기 위해
     */
    PreferredTransport(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 교통수단의 사용자 친화적인 표시명을 반환하는 메서드
     * 이유: 사용자 프로필, 설정 화면 등에서 선호 교통수단을 사용자가 이해하기 쉽게 표시하기 위해
     * 
     * @return 교통수단의 한글 표시명
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * 대중교통인지 확인하는 메서드
     * 이유: 대중교통 관련 특별한 처리 로직을 적용하기 위해
     * 
     * @return 대중교통 여부 (PUBLIC_TRANSPORT인 경우 true)
     */
    public boolean isPublicTransport() {
        return this == PUBLIC_TRANSPORT;
    }

    /**
     * 개인 교통수단인지 확인하는 메서드
     * 이유: 개인 교통수단 관련 특별한 처리 로직을 적용하기 위해
     * 
     * @return 개인 교통수단 여부 (CAR, BICYCLE, MOTORCYCLE인 경우 true)
     */
    public boolean isPersonalTransport() {
        return this == CAR || this == BICYCLE || this == MOTORCYCLE;
    }

    /**
     * 친환경 교통수단인지 확인하는 메서드
     * 이유: 친환경 교통수단 사용자에 대한 특별한 혜택이나 추천을 제공하기 위해
     * 
     * @return 친환경 교통수단 여부 (PUBLIC_TRANSPORT, BICYCLE, WALKING인 경우 true)
     */
    public boolean isEcoFriendly() {
        return this == PUBLIC_TRANSPORT || this == BICYCLE || this == WALKING;
    }
}
