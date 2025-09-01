package com.promiseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

/**
 * 카카오 알림 전송 요청 DTO
 * 이유: 클라이언트로부터 카카오톡을 통한 알림 전송에 필요한 정보를 받아서 서버에서 처리하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoNotificationRequest {

    /**
     * 알림 템플릿 유형
     * 이유: 미리 정의된 알림 템플릿을 사용하여 일관된 디자인과 메시지 형식을 제공하기 위해
     */
    @NotBlank(message = "알림 템플릿은 필수입니다")
    @Pattern(regexp = "^(MEETING_INVITE|MEETING_UPDATE|MEETING_CANCEL|MEETING_REMINDER|GENERAL)$", 
             message = "유효하지 않은 알림 템플릿입니다")
    private String template;

    /**
     * 알림을 받을 사용자 ID 목록
     * 이유: 시스템에 등록된 사용자들에게 알림을 전송하기 위해
     */
    @NotEmpty(message = "알림을 받을 사용자 ID 목록은 필수입니다")
    @Size(max = 100, message = "한 번에 알림을 보낼 수 있는 사용자는 100명을 초과할 수 없습니다")
    private List<Long> toUserIds;

    /**
     * 알림 페이로드 (템플릿별 데이터)
     * 이유: 템플릿에 맞는 개인화된 데이터를 제공하기 위해
     */
    @NotNull(message = "알림 페이로드는 필수입니다")
    private Map<String, Object> payload;

    /**
     * 약속 ID (선택사항)
     * 이유: 약속 관련 알림인 경우 해당 약속을 식별하기 위해
     */
    @Min(value = 1, message = "약속 ID는 1 이상이어야 합니다")
    private Long meetingId;

    /**
     * 알림 우선순위 (선택사항)
     * 이유: 알림의 중요도에 따라 발송 우선순위를 조정하기 위해
     */
    @Pattern(regexp = "^(LOW|NORMAL|HIGH|URGENT)$", 
             message = "유효하지 않은 알림 우선순위입니다")
    @Builder.Default
    private String priority = "NORMAL";

    /**
     * 알림 발송 지연 시간 (초 단위, 선택사항)
     * 이유: 특정 시점에 알림을 발송하고 싶은 경우 예약 발송 기능을 지원하기 위해
     */
    @Min(value = 0, message = "알림 발송 지연 시간은 0초 이상이어야 합니다")
    @Max(value = 86400, message = "알림 발송 지연 시간은 24시간(86400초)을 초과할 수 없습니다")
    private Integer delaySeconds;
}
