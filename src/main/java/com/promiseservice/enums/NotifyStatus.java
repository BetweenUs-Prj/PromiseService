package com.promiseservice.enums;

/**
 * 알림 전송 상태를 나타내는 열거형
 * 이유: 카카오톡 알림 전송 과정에서 발생할 수 있는 다양한 상태를 체계적으로 관리하여
 * 전송 실패 원인을 분석하고 적절한 후속 조치를 취할 수 있도록 지원하기 위해.
 * 각 상태별로 명확한 의미를 부여하여 시스템 운영과 디버깅을 효율화
 */
public enum NotifyStatus {

    /**
     * 알림 전송 대기 상태
     * 이유: 아직 전송되지 않은 알림을 표시하여 스케줄러나 수동 전송 대상으로 식별하기 위해
     */
    PENDING("대기"),

    /**
     * 알림 전송 성공 상태  
     * 이유: 카카오톡 알림이 성공적으로 전송된 상태를 표시하여 중복 전송을 방지하기 위해
     */
    SENT("전송완료"),

    /**
     * 사용자 동의 필요 상태
     * 이유: 카카오톡 메시지 전송 권한에 대한 사용자 동의가 필요한 경우를 구분하여
     * 재동의 안내나 권한 요청 프로세스를 실행하기 위해
     */
    NEEDS_CONSENT("동의필요"),

    /**
     * 토큰 만료 상태
     * 이유: 액세스 토큰이 만료되어 전송에 실패한 경우를 구분하여
     * 토큰 갱신 프로세스를 자동으로 실행하기 위해
     */
    TOKEN_EXPIRED("토큰만료"),

    /**
     * 전송 실패 상태
     * 이유: 네트워크 오류, API 오류 등으로 인한 전송 실패를 표시하여
     * 재시도 대상으로 식별하고 문제 원인을 분석하기 위해
     */
    FAILED("전송실패");

    private final String description;

    /**
     * NotifyStatus 생성자
     * 이유: 각 상태에 대한 한국어 설명을 함께 저장하여
     * UI 표시나 로그 메시지에서 사용자가 이해하기 쉬운 형태로 제공하기 위해
     * 
     * @param description 상태에 대한 한국어 설명
     */
    NotifyStatus(String description) {
        this.description = description;
    }

    /**
     * 상태 설명을 반환하는 메서드
     * 이유: 열거형 값의 한국어 설명을 조회하여 사용자 인터페이스나 로그에서 활용하기 위해
     * 
     * @return 상태에 대한 한국어 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 재시도 가능한 상태인지 확인하는 메서드
     * 이유: 전송 실패 시 자동 재시도 대상 여부를 판단하여
     * 불필요한 재시도를 방지하고 효율적인 알림 시스템 운영을 지원하기 위해
     * 
     * @return 재시도 가능 여부 (true: 재시도 가능, false: 재시도 불가)
     */
    public boolean isRetryable() {
        return this == PENDING || this == TOKEN_EXPIRED || this == FAILED;
    }

    /**
     * 성공 상태인지 확인하는 메서드
     * 이유: 알림 전송이 성공적으로 완료된 상태인지 빠르게 확인하여
     * 통계 계산이나 상태 체크에서 활용하기 위해
     * 
     * @return 성공 상태 여부 (true: 전송 성공, false: 미완료 또는 실패)
     */
    public boolean isSuccessful() {
        return this == SENT;
    }

    /**
     * 사용자 액션이 필요한 상태인지 확인하는 메서드
     * 이유: 시스템에서 자동으로 해결할 수 없어 사용자의 직접적인 액션이 필요한 상태를 구분하여
     * 적절한 안내 메시지나 UI를 제공하기 위해
     * 
     * @return 사용자 액션 필요 여부 (true: 사용자 액션 필요, false: 시스템 처리 가능)
     */
    public boolean requiresUserAction() {
        return this == NEEDS_CONSENT;
    }
}







