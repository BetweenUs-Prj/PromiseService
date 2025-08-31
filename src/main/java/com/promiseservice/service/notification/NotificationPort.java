package com.promiseservice.service.notification;

import java.util.List;
import java.util.Map;

/**
 * 알림 전송 포트 인터페이스
 * 이유: 다양한 알림 채널(알림톡, 푸시 등)을 추상화하여 일관된 인터페이스 제공
 */
public interface NotificationPort {

    /**
     * 알림 전송 결과
     */
    class SendResult {
        private final boolean success;
        private final String message;
        private final String errorCode;
        private final List<String> successRecipients;
        private final List<String> failedRecipients;

        public SendResult(boolean success, String message, String errorCode, 
                         List<String> successRecipients, List<String> failedRecipients) {
            this.success = success;
            this.message = message;
            this.errorCode = errorCode;
            this.successRecipients = successRecipients;
            this.failedRecipients = failedRecipients;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
        public List<String> getSuccessRecipients() { return successRecipients; }
        public List<String> getFailedRecipients() { return failedRecipients; }
        
        public boolean isOk() { return success; }
    }

    /**
     * 단순 텍스트 알림 전송
     * 이유: 단순 텍스트 알림을 위한 기본 전송 메서드
     * 
     * @param to 수신자 전화번호
     * @param text 메시지 내용
     * @return 전송 결과
     */
    SendResult send(String to, String text);

    /**
     * 템플릿 기반 알림 전송
     * 이유: 알림톡 등 템플릿 기반 알림을 위한 고급 전송 메서드
     * 
     * @param to 수신자 전화번호
     * @param templateCode 템플릿 코드
     * @param variables 템플릿 변수
     * @return 전송 결과
     */
    SendResult sendTemplate(String to, String templateCode, Map<String, String> variables);

    /**
     * 알림 채널명 반환
     * 이유: 로깅 및 모니터링을 위해 알림 채널을 구분하기 위해
     * 
     * @return 채널명 (예: "ALIMTALK", "PUSH")
     */
    String getChannelName();

    /**
     * 알림 채널 우선순위 반환
     * 이유: 대체발송 시 우선순위가 높은 채널부터 시도하기 위해
     * 
     * @return 우선순위 (낮을수록 높은 우선순위)
     */
    int getPriority();
}




