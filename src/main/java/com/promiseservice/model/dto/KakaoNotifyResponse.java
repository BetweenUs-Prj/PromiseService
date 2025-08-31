package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 카카오톡 알림 전송 응답 DTO
 * 이유: 카카오톡 알림 전송 결과를 클라이언트에게 반환하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class KakaoNotifyResponse {

    private boolean success;                           // 전체 전송 성공 여부
    private int sentCount;                            // 성공적으로 전송된 메시지 수
    private int totalCount;                           // 전체 대상자 수
    private List<KakaoNotifyFailure> failed;         // 실패한 전송 목록
    private String message;                           // 응답 메시지

    /**
     * 성공 응답 생성자
     * 이유: 성공적인 전송 결과를 편리하게 생성하기 위해
     * 
     * @param sentCount 전송 성공 수
     * @param totalCount 전체 대상자 수
     */
    public KakaoNotifyResponse(int sentCount, int totalCount) {
        this.success = sentCount > 0;
        this.sentCount = sentCount;
        this.totalCount = totalCount;
        this.failed = new ArrayList<>();
        this.message = sentCount == totalCount ? "모든 메시지가 성공적으로 전송되었습니다" :
                      sentCount > 0 ? "일부 메시지가 전송되었습니다" : "메시지 전송에 실패했습니다";
    }

    /**
     * 실패 정보를 포함한 생성자
     * 이유: 전송 결과와 실패 정보를 함께 설정하기 위해
     * 
     * @param sentCount 전송 성공 수
     * @param totalCount 전체 대상자 수
     * @param failed 실패 목록
     */
    public KakaoNotifyResponse(int sentCount, int totalCount, List<KakaoNotifyFailure> failed) {
        this.success = sentCount > 0;
        this.sentCount = sentCount;
        this.totalCount = totalCount;
        this.failed = failed != null ? failed : new ArrayList<>();
        this.message = generateMessage();
    }

    /**
     * 실패 항목을 추가하는 메서드
     * 이유: 전송 과정에서 발생한 실패를 점진적으로 추가하기 위해
     * 
     * @param failure 실패 정보
     */
    public void addFailure(KakaoNotifyFailure failure) {
        if (this.failed == null) {
            this.failed = new ArrayList<>();
        }
        this.failed.add(failure);
    }

    /**
     * 실패 항목을 추가하는 편의 메서드
     * 이유: 실패 정보를 간편하게 추가하기 위해
     * 
     * @param userId 실패한 사용자 ID
     * @param reason 실패 이유
     */
    public void addFailure(Long userId, String reason) {
        addFailure(new KakaoNotifyFailure(userId, reason));
    }

    /**
     * 실패 수를 반환하는 메서드
     * 이유: 실패한 전송 수를 빠르게 확인하기 위해
     * 
     * @return 실패 수
     */
    public int getFailedCount() {
        return failed != null ? failed.size() : 0;
    }

    /**
     * 부분 성공 여부를 확인하는 메서드
     * 이루: 일부는 성공하고 일부는 실패한 경우를 확인하기 위해
     * 
     * @return 부분 성공 여부
     */
    public boolean isPartialSuccess() {
        return sentCount > 0 && getFailedCount() > 0;
    }

    /**
     * 전체 실패 여부를 확인하는 메서드
     * 이유: 모든 전송이 실패한 경우를 확인하기 위해
     * 
     * @return 전체 실패 여부
     */
    public boolean isCompleteFailure() {
        return sentCount == 0 && totalCount > 0;
    }

    /**
     * 응답 메시지를 생성하는 메서드
     * 이유: 전송 결과에 따라 적절한 메시지를 자동 생성하기 위해
     * 
     * @return 생성된 메시지
     */
    private String generateMessage() {
        if (sentCount == totalCount) {
            return "모든 메시지가 성공적으로 전송되었습니다";
        } else if (sentCount > 0) {
            return String.format("%d/%d 메시지가 전송되었습니다", sentCount, totalCount);
        } else {
            return "메시지 전송에 실패했습니다";
        }
    }

    /**
     * 카카오 알림 전송 실패 정보 내부 클래스
     * 이유: 실패한 전송에 대한 구체적인 정보를 제공하기 위해
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class KakaoNotifyFailure {
        private Long userId;      // 실패한 사용자 ID
        private String reason;    // 실패 이유

        public KakaoNotifyFailure(Long userId, String reason) {
            this.userId = userId;
            this.reason = reason;
        }
    }
}
