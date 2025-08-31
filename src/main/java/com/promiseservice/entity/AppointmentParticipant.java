package com.promiseservice.entity;

import com.promiseservice.enums.NotifyStatus;
import com.promiseservice.enums.ParticipantState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 약속 참여자 정보를 저장하는 엔티티
 * 이유: 각 약속에 참여하는 사용자들의 정보와 참여 상태, 알림 발송 현황을 관리하여
 * 호스트가 참여자 현황을 파악하고 적절한 알림을 발송할 수 있도록 지원하기 위해.
 * 참여자별 개별 관리를 통해 정확한 약속 진행과 알림 전송 가능
 */
@Entity
@Table(name = "appointment_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentParticipant {

    /**
     * 참여자 고유 ID (Primary Key)
     * 이유: 각 참여자 레코드를 고유하게 식별하고 관리하기 위한 자동 생성되는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 약속 ID (Foreign Key)
     * 이유: 어느 약속에 대한 참여자인지 식별하기 위해 약속 엔티티와의 연관관계 설정.
     * 하나의 약속에 여러 참여자가 있을 수 있는 1:N 관계
     */
    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    /**
     * 내부 사용자 ID (선택사항)
     * 이유: 시스템 내부 사용자 관리 테이블과 연동이 필요한 경우 사용.
     * 내부 사용자가 아닌 외부 초대자의 경우 null 가능
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 카카오 사용자 ID
     * 이유: 카카오톡 알림을 발송하기 위해 필수적으로 필요한 카카오 사용자 식별자.
     * 카카오 OAuth 인증을 통해 얻은 사용자 고유 ID 저장
     */
    @Column(name = "kakao_id", nullable = false)
    private Long kakaoId;

    /**
     * 참여자 응답 상태
     * 이유: 초대에 대한 참여자의 응답 상태를 추적하여 호스트가 참여 현황을 파악하고,
     * 알림 발송 대상을 적절히 필터링할 수 있도록 지원하기 위해
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private ParticipantState state = ParticipantState.INVITED;

    /**
     * 알림 발송 상태
     * 이유: 카카오톡 알림 발송 결과를 추적하여 전송 실패 시 재시도하거나,
     * 전송 성공 시 중복 발송을 방지하기 위해
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notify_status", nullable = false)
    private NotifyStatus notifyStatus = NotifyStatus.PENDING;

    /**
     * 알림 발송 시점
     * 이유: 언제 알림이 발송되었는지 기록하여 발송 이력을 추적하고,
     * 알림 간격 제어나 통계 분석에 활용하기 위해
     */
    @Column(name = "notified_at")
    private Instant notifiedAt;

    /**
     * 알림 발송 오류 메시지
     * 이유: 알림 발송 실패 시 구체적인 오류 원인을 저장하여
     * 문제 해결과 재시도 전략 수립에 활용하기 위해
     */
    @Column(name = "notify_error", length = 512)
    private String notifyError;

    /**
     * 참여자 정보 생성 시점
     * 이유: 참여자가 언제 약속에 초대되었는지 기록하여 이력 관리 및 통계 분석에 활용
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 참여자 정보 최종 수정 시점
     * 이유: 참여자 상태나 알림 정보가 언제 마지막으로 업데이트되었는지 추적하기 위해
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 엔티티 생성 전 자동 실행 메서드
     * 이유: 새로운 참여자 정보 저장 시 생성 시점과 수정 시점을 자동으로 설정하여
     * 개발자가 수동으로 시점을 관리하는 실수를 방지하기 위해
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 엔티티 수정 전 자동 실행 메서드
     * 이유: 참여자 정보 업데이트 시 수정 시점을 자동으로 갱신하여
     * 최신 변경 사항을 정확히 추적하기 위해
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * 참여자 상태를 안전하게 변경하는 메서드
     * 이유: 상태 변경 시 유효성을 검증하고 관련 정보를 일관되게 업데이트하여
     * 데이터 무결성을 보장하기 위해
     * 
     * @param newState 변경할 새로운 상태
     * @throws IllegalStateException 잘못된 상태 변경 시도 시
     */
    public void changeState(ParticipantState newState) {
        if (!this.state.canChangeTo(newState)) {
            throw new IllegalStateException(
                String.format("상태 변경 불가: %s → %s", this.state.getDescription(), newState.getDescription())
            );
        }
        this.state = newState;
    }

    /**
     * 알림 발송 성공 처리 메서드
     * 이유: 알림 발송 성공 시 관련 정보를 일관되게 업데이트하여
     * 중복 발송 방지와 정확한 발송 이력 관리를 위해
     */
    public void markNotificationSent() {
        this.notifyStatus = NotifyStatus.SENT;
        this.notifiedAt = Instant.now();
        this.notifyError = null; // 성공 시 이전 오류 정보 제거
    }

    /**
     * 알림 발송 실패 처리 메서드
     * 이유: 알림 발송 실패 시 상태와 오류 정보를 체계적으로 저장하여
     * 재시도 로직과 문제 해결에 활용하기 위해
     * 
     * @param status 실패 상태 (TOKEN_EXPIRED, NEEDS_CONSENT, FAILED 등)
     * @param errorMessage 오류 메시지
     */
    public void markNotificationFailed(NotifyStatus status, String errorMessage) {
        if (status == NotifyStatus.SENT || status == NotifyStatus.PENDING) {
            throw new IllegalArgumentException("실패 상태가 아닙니다: " + status);
        }
        
        this.notifyStatus = status;
        this.notifyError = errorMessage != null && errorMessage.length() > 500 
            ? errorMessage.substring(0, 500) + "..." 
            : errorMessage;
    }

    /**
     * 알림 재시도 가능 여부 확인 메서드
     * 이유: 현재 알림 상태에서 재시도가 가능한지 판단하여
     * 효율적인 재시도 로직 구현과 불필요한 재시도 방지를 위해
     * 
     * @return 재시도 가능 여부
     */
    public boolean canRetryNotification() {
        return this.notifyStatus.isRetryable() && this.state.shouldReceiveNotifications();
    }

    /**
     * 참여자가 알림을 받을 대상인지 확인하는 메서드
     * 이유: 현재 참여자 상태에서 알림 발송이 적절한지 확인하여
     * 적절한 대상에게만 알림을 발송하기 위해
     * 
     * @return 알림 발송 대상 여부
     */
    public boolean shouldReceiveNotifications() {
        return this.state.shouldReceiveNotifications();
    }

    /**
     * 참여자 정보의 유효성을 검증하는 메서드
     * 이유: 필수 정보가 모두 입력되었는지 확인하여 데이터 무결성을 보장하기 위해
     * 
     * @throws IllegalStateException 필수 정보 누락 시
     */
    public void validateRequiredFields() {
        if (this.appointmentId == null) {
            throw new IllegalStateException("약속 ID는 필수입니다");
        }
        if (this.kakaoId == null) {
            throw new IllegalStateException("카카오 사용자 ID는 필수입니다");
        }
        if (this.state == null) {
            throw new IllegalStateException("참여자 상태는 필수입니다");
        }
        if (this.notifyStatus == null) {
            throw new IllegalStateException("알림 상태는 필수입니다");
        }
    }
}







