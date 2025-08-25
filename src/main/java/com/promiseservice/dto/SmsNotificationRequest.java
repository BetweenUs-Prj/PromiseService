package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * SMS 알림 요청을 위한 DTO
 * 이유: 사용자에게 약속 관련 SMS 알림을 전송하기 위한 SMS 정보를 체계적으로 관리하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class SmsNotificationRequest {

    // SMS를 받을 사용자 ID 목록
    // 이유: 특정 사용자들에게만 SMS를 전송하여 개인화된 SMS 알림 서비스 제공
    @NotNull(message = "SMS를 받을 사용자 ID 목록은 필수입니다")
    private List<Long> recipientUserIds;

    // SMS를 받을 전화번호 목록 (사용자 ID 대신 직접 전화번호 지정 시 사용)
    // 이유: 사용자 ID를 모르는 경우나 외부 사용자에게 SMS를 보낼 때 사용
    private List<String> phoneNumbers;

    // SMS 메시지 내용
    // 이유: SMS의 핵심 내용을 전달하여 사용자가 필요한 정보를 파악할 수 있도록 함
    @NotBlank(message = "SMS 메시지 내용은 필수입니다")
    private String message;

    // SMS 타입 (URGENT, NORMAL, REMINDER)
    // 이유: SMS의 종류를 구분하여 전송 우선순위와 처리 방식을 결정하기 위해
    @NotBlank(message = "SMS 타입은 필수입니다")
    private String smsType;

    // 관련 약속 ID
    // 이유: SMS와 관련된 약속을 연결하여 추적 및 관리를 용이하게 하기 위해
    private Long meetingId;

    // 발신자 이름 (선택사항)
    // 이유: SMS 발신자를 명확히 하여 수신자가 메시지의 출처를 쉽게 파악할 수 있도록 함
    private String senderName = "PromiseService";

    // 예약 전송 시간 (선택사항)
    // 이유: 특정 시간에 SMS를 전송하여 적절한 타이밍에 알림을 제공하기 위해
    private String scheduledTime;

    // 템플릿 ID (선택사항)
    // 이유: 미리 정의된 SMS 템플릿을 사용하여 일관된 메시지 형식 제공
    private String templateId;

    // 템플릿 변수 (JSON 형태)
    // 이유: SMS 템플릿에 동적 데이터를 삽입하여 개인화된 메시지 생성
    private String templateVariables;

    /**
     * 전화번호 형식 검증
     * 이유: 올바른 전화번호 형식인지 확인하여 SMS 전송 실패를 방지하기 위해
     * 
     * @param phoneNumber 검증할 전화번호
     * @return 유효성 검증 결과
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        // 한국 전화번호 형식 검증 (010-XXXX-XXXX 또는 01XXXXXXXXX)
        String pattern = "^(010|011|016|017|018|019)-?\\d{3,4}-?\\d{4}$";
        return phoneNumber != null && phoneNumber.matches(pattern);
    }

    /**
     * SMS 메시지 길이 검증
     * 이유: SMS 메시지 길이 제한을 확인하여 메시지 분할 또는 축약 필요성을 판단하기 위해
     * 
     * @return 메시지 길이가 적절한지 여부
     */
    public boolean isMessageLengthValid() {
        // SMS는 일반적으로 90자(한글 기준) 또는 160자(영문 기준) 제한
        return message != null && message.length() <= 90;
    }

    /**
     * 긴급 SMS 여부 확인
     * 이유: 긴급 SMS의 경우 우선 처리하여 중요한 알림이 지연되지 않도록 하기 위해
     * 
     * @return 긴급 SMS 여부
     */
    public boolean isUrgent() {
        return "URGENT".equals(smsType);
    }
}

