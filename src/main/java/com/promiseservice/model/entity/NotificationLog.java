package com.promiseservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 알림 로그 엔티티
 * 이유: 발송된 알림의 이력과 결과를 추적하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "notification_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationLog {

    /**
     * PK
     * 이유: 알림 로그를 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약속 ID
     * 이유: 어떤 약속과 관련된 알림인지 식별하기 위해
     */
    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    /**
     * 알림 사용자 ID
     * 이유: 어떤 사용자에게 알림을 보냈는지 식별하기 위해
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 알림 채널
     * 이유: 어떤 채널을 통해 알림을 보냈는지 구분하기 위해
     */
    @Column(name = "channel", nullable = false, length = 32)
    private String channel;

    /**
     * 전송 페이로드
     * 이유: 알림과 함께 전송된 데이터를 저장하기 위해
     */
    @Column(name = "payload_json", columnDefinition = "TEXT", nullable = false)
    private String payloadJson;

    /**
     * HTTP 상태
     * 이유: 알림 전송 시 HTTP 응답 상태를 기록하기 위해
     */
    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    /**
     * 서비스 결과 코드
     * 이유: 알림 서비스에서 반환한 결과 코드를 저장하기 위해
     */
    @Column(name = "result_code")
    private Integer resultCode;

    /**
     * 에러 JSON
     * 이유: 알림 전송 실패 시 에러 정보를 저장하기 위해
     */
    @Column(name = "error_json", columnDefinition = "TEXT")
    private String errorJson;

    /**
     * 추적 ID
     * 이유: 알림 전송 과정을 추적하기 위한 고유 식별자
     */
    @Column(name = "trace_id", nullable = false, length = 64)
    private String traceId;

    /**
     * 생성 시각
     * 이유: 언제 해당 알림이 생성되었는지 기록하기 위해
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 약속과의 관계
     * 이유: 약속 정보를 조회하기 위해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", insertable = false, updatable = false)
    private Meeting meeting;

    /**
     * 사용자와의 관계
     * 이유: 사용자 정보를 조회하기 위해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserProfile user;
}
