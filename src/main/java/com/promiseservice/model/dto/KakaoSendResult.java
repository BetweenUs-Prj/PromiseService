package com.promiseservice.dto;

/**
 * 카카오톡 메시지 전송 결과
 * 이유: 카카오 API 호출 결과를 표준화된 형태로 반환하여 로깅 및 후속 처리에 활용
 * 
 * @param httpStatus HTTP 상태 코드 (200, 401, 403 등)
 * @param resultCode 카카오 서비스 결과 코드 (성공=0, 실패=음수)
 * @param rawResponse 원본 응답 문자열 (성공/실패 모두 포함)
 * @param errorMessage 에러 메시지 (실패 시에만)
 */
public record KakaoSendResult(
    int httpStatus,
    Integer resultCode,
    String rawResponse,
    String errorMessage
) {
    
    /**
     * 성공 결과 생성 팩토리 메서드
     * 이유: 성공 케이스를 명확하게 표현하고 코드 가독성 향상
     * 
     * @param rawResponse 카카오 API 성공 응답
     * @return 성공 결과 객체
     */
    public static KakaoSendResult success(String rawResponse) {
        return new KakaoSendResult(200, 0, rawResponse, null);
    }
    
    /**
     * 실패 결과 생성 팩토리 메서드
     * 이유: 실패 케이스를 명확하게 표현하고 에러 정보 포함
     * 
     * @param httpStatus HTTP 상태 코드
     * @param rawResponse 카카오 API 에러 응답
     * @param errorMessage 에러 메시지
     * @return 실패 결과 객체
     */
    public static KakaoSendResult failure(int httpStatus, String rawResponse, String errorMessage) {
        return new KakaoSendResult(httpStatus, null, rawResponse, errorMessage);
    }
    
    /**
     * 성공 여부 판단
     * 이유: 결과 객체에서 성공/실패를 쉽게 판단할 수 있도록 편의 메서드 제공
     * 
     * @return HTTP 200대이고 result_code가 0이면 성공
     */
    public boolean isSuccess() {
        return httpStatus >= 200 && httpStatus < 300 && (resultCode == null || resultCode == 0);
    }
    
    /**
     * 토큰 관련 에러인지 판단
     * 이유: 401 에러 시 토큰 갱신 로직을 트리거하기 위해
     * 
     * @return 401 Unauthorized 또는 토큰 관련 에러면 true
     */
    public boolean isTokenError() {
        return httpStatus == 401 || 
               (errorMessage != null && errorMessage.contains("token"));
    }
    
    /**
     * 권한 부족 에러인지 판단
     * 이유: 403 에러 시 사용자 동의 또는 팀원 등록 안내를 위해
     * 
     * @return 403 Forbidden 또는 권한 관련 에러면 true
     */
    public boolean isPermissionError() {
        return httpStatus == 403 || 
               (errorMessage != null && (errorMessage.contains("scope") || errorMessage.contains("permission")));
    }
}
