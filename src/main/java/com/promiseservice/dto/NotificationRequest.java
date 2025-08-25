package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 알림 요청을 위한 DTO
 * 이유: 사용자에게 약속 관련 알림을 전송하기 위한 알림 정보를 체계적으로 관리하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class NotificationRequest {

    // 알림을 받을 사용자 ID 목록
    // 이유: 특정 사용자들에게만 알림을 전송하여 개인화된 알림 서비스 제공
    @NotNull(message = "알림을 받을 사용자 ID 목록은 필수입니다")
    private List<Long> recipientUserIds;

    // 알림 제목
    // 이유: 사용자가 알림의 핵심 내용을 빠르게 파악할 수 있도록 간결한 제목 제공
    @NotBlank(message = "알림 제목은 필수입니다")
    private String title;

    // 알림 내용
    // 이유: 알림의 상세 정보를 제공하여 사용자가 필요한 정보를 완전히 이해할 수 있도록 함
    @NotBlank(message = "알림 내용은 필수입니다")
    private String content;

    // 알림 타입
    // 이유: 알림의 종류를 구분하여 사용자가 알림을 분류하고 관리할 수 있도록 함
    @NotBlank(message = "알림 타입은 필수입니다")
    private String type;

    // 관련 약속 ID
    // 이유: 알림과 관련된 약속을 연결하여 사용자가 해당 약속으로 바로 이동할 수 있도록 함
    private Long meetingId;

    // 알림 우선순위 (HIGH, MEDIUM, LOW)
    // 이유: 중요한 알림을 우선적으로 표시하여 사용자가 놓치지 않도록 함
    private String priority = "MEDIUM";

    // 추가 데이터 (JSON 형태)
    // 이유: 알림에 필요한 추가 정보를 유연하게 포함할 수 있도록 확장성 제공
    private String extraData;
}






