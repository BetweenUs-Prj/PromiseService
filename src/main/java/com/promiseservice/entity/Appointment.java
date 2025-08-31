package com.promiseservice.entity;

import com.promiseservice.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * 약속 정보를 저장하는 엔티티
 * 이유: 사용자가 생성한 약속의 상세 정보와 알림 설정을 저장하여
 * 지정된 시간에 카카오톡 알림을 자동으로 발송할 수 있도록 지원하기 위해.
 * 약속 시간, 알림 시간, 발송 상태 등을 관리
 */
@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    /**
     * 약속 고유 ID (Primary Key)
     * 이유: 각 약속을 고유하게 식별하고 관리하기 위한 자동 생성되는 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 최대 참여 인원
     * 이유: 약속에 참여할 수 있는 최대 인원을 제한하여
     * 장소 수용 인원이나 모임 규모를 관리하기 위해
     */
    @Column(name = "max_participants")
    private Integer maxParticipants;

    /**
     * 추천 장소 정보
     * 이유: 사용자가 선택한 추천 장소의 정보를 저장하여
     * 약속 참여자들이 장소를 쉽게 찾을 수 있도록 지원하기 위해
     */
    @Column(name = "recommended_place", length = 500)
    private String recommendedPlace;

    /**
     * 약속 제목
     * 이유: 사용자가 쉽게 약속을 식별할 수 있도록 하고,
     * 카카오톡 알림 메시지에 표시할 약속의 주요 내용을 저장하기 위해
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 약속 장소
     * 이유: 약속 위치 정보를 저장하여 알림 메시지에 포함시키고,
     * 사용자가 약속 장소를 미리 확인할 수 있도록 지원하기 위해
     */
    @Column(name = "place", length = 300)
    private String place;

    /**
     * 장소 위도
     * 이유: 정확한 위치 정보 제공과 지도 서비스 연동을 위해
     * 약속 장소의 위도 좌표를 저장
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * 장소 경도
     * 이유: 정확한 위치 정보 제공과 지도 서비스 연동을 위해
     * 약속 장소의 경도 좌표를 저장
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * 약속 시작 시간
     * 이유: 실제 약속이 시작되는 시점을 저장하여
     * 알림 메시지에 표시하고 약속 일정 관리에 활용하기 위해
     */
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    /**
     * 알림 발송 시간 (선택적)
     * 이유: 카카오톡 알림을 언제 발송할지 정확한 시점을 저장하여
     * 필요한 경우 자동 알림을 전송할 수 있도록 하기 위해
     */
    @Column(name = "remind_at")
    private Instant remindAt;

    /**
     * 약속 상세 보기 링크 URL
     * 이유: 약속에 대한 추가 정보나 상세 페이지 링크를 저장하여
     * 카카오톡 알림 메시지에서 클릭 시 해당 페이지로 이동할 수 있도록 지원
     */
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    /**
     * 알림 발송 완료 여부
     * 이유: 중복 발송을 방지하고 발송 상태를 추적하기 위해
     * 알림이 성공적으로 전송된 약속을 구분하여 관리
     */
    @Column(name = "sent", nullable = false)
    private boolean sent = false;

    /**
     * 약속 상태
     * 이유: 약속의 진행 단계를 관리하여 각 상태별로 적절한 처리 로직을 적용하고,
     * 사용자에게 현재 약속 상태를 명확히 전달하기 위해
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status = AppointmentStatus.DRAFT;

    /**
     * 약속 호스트 사용자 ID
     * 이유: 약속을 생성한 호스트를 식별하여 약속 관리 권한을 제어하고,
     * 호스트만 약속을 확정하거나 취소할 수 있도록 권한 관리하기 위해
     */
    @Column(name = "host_user_id")
    private Long hostUserId;

    /**
     * 약속 상세 정보 URL
     * 이유: 약속에 대한 추가 정보나 외부 링크를 저장하여
     * 참여자들이 더 자세한 정보를 확인할 수 있도록 지원하기 위해
     */
    @Column(name = "detail_url", length = 500)
    private String detailUrl;

    /**
     * 약속 생성 시점
     * 이유: 약속이 언제 생성되었는지 기록하여 디버깅 및 통계 분석에 활용하고,
     * 오래된 약속 데이터를 정리하는 기준으로 사용하기 위해
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 약속 최종 수정 시점
     * 이유: 약속 정보 변경 시점을 추적하여 데이터 변경 이력을 관리하고,
     * 최근 수정된 약속을 식별하기 위해
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 엔티티 생성 전 자동 실행 메서드
     * 이유: 새로운 약속 생성 시 생성 시점과 수정 시점을 자동으로 설정하여
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
     * 이유: 약속 정보 업데이트 시 수정 시점을 자동으로 갱신하여
     * 최신 변경 사항을 정확히 추적하기 위해
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * 알림 발송 시간이 현재 시간을 지났는지 확인하는 메서드
     * 이유: 스케줄러에서 발송해야 할 알림을 효율적으로 식별하기 위해
     * 현재 시간과 알림 시간을 비교하는 편의 메서드 제공
     * 
     * @return 알림 발송 시간 도래 여부 (true: 발송 시간 지남, false: 아직 시간 안 됨)
     */
    public boolean isRemindTimeDue() {
        return this.remindAt != null && this.remindAt.isBefore(Instant.now());
    }

    /**
     * 알림 발송 완료 처리 메서드
     * 이유: 알림 발송 후 상태를 일관되게 업데이트하고
     * 중복 발송을 방지하기 위해 sent 플래그를 안전하게 설정
     */
    public void markAsSent() {
        this.sent = true;
    }

    /**
     * 약속이 과거 약속인지 확인하는 메서드
     * 이유: 이미 지난 약속에 대한 알림 발송을 방지하고,
     * 데이터 정리 시 과거 약속을 식별하기 위해
     * 
     * @return 과거 약속 여부 (true: 지난 약속, false: 미래 약속)
     */
    public boolean isPastAppointment() {
        return this.startAt != null && this.startAt.isBefore(Instant.now());
    }

    /**
     * 약속까지 남은 시간(분)을 계산하는 메서드
     * 이유: 약속까지 얼마나 남았는지 계산하여 알림 메시지에 포함시키거나
     * 적절한 알림 시점을 결정하는데 활용하기 위해
     * 
     * @return 약속까지 남은 시간(분), 음수이면 이미 지난 약속
     */
    public long getMinutesUntilAppointment() {
        if (this.startAt == null) {
            return 0;
        }
        return (this.startAt.toEpochMilli() - Instant.now().toEpochMilli()) / (1000 * 60);
    }
}
