package com.promiseservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 약속 참여자의 초대 및 응답 상태를 관리하는 JPA 엔티티
 * 이유: 각 약속에 초대된 사용자들의 참여 의사, 응답 시점, 실제 참여 여부를 체계적으로 추적하여
 * 방장이 참여자 현황을 실시간으로 파악하고 알림 발송 대상을 정확히 선정할 수 있도록 지원
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "meeting_participant", 
       indexes = {
           // 이유: 특정 약속의 참여자 목록 조회 성능 최적화를 위해
           @Index(name = "idx_participant_meeting_id", columnList = "meeting_id"),
           // 이유: 특정 사용자의 참여 약속 목록 조회 성능 최적화를 위해  
           @Index(name = "idx_participant_user_id", columnList = "user_id"),
           // 이유: 응답 상태별 참여자 필터링 성능 향상을 위해
           @Index(name = "idx_participant_response", columnList = "response")
       },
       uniqueConstraints = {
           // 이유: 동일한 사용자가 같은 약속에 중복 초대되는 것을 방지하기 위해
           @UniqueConstraint(name = "unique_meeting_user", columnNames = {"meeting_id", "user_id"})
       })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MeetingParticipant {

    /**
     * 참여자 고유 식별자
     * 이유: 시스템 내에서 각 참여자 레코드를 유일하게 구분하기 위한 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약속 ID (외래키)
     * 이유: 참여자가 어떤 약속에 속하는지 식별하기 위한 필수 참조 정보
     * Meeting 엔티티와의 관계를 표현하는 데이터베이스 연결고리
     */
    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    /**
     * 참여자 사용자 ID
     * 이유: 외부 UserService의 사용자와 연결하기 위한 식별자
     * 실제 사용자 정보는 외부 서비스에서 관리되므로 ID만 저장
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 약속 엔티티와의 다대일 관계
     * 이유: JPA 객체 관계 매핑을 통해 편리한 약속 정보 접근을 지원하되
     * 지연 로딩으로 성능을 최적화하고 순환 참조를 방지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false, insertable = false, updatable = false)
    private Meeting meeting;

    /**
     * 참여자의 초대 응답 상태
     * 이유: 초대, 수락, 거부 등의 참여 의사를 명확히 구분하여
     * 각 상태에 맞는 비즈니스 로직과 알림을 적용하기 위해
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "response", nullable = false)
    private ResponseStatus response = ResponseStatus.INVITED;

    /**
     * 실제 참여 확정 시점
     * 이유: 참여 의사 표명 시점과 실제 참여 시점을 구분하여
     * 정확한 참여 통계 및 리마인더 발송 여부를 판단하기 위해
     */
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    /**
     * 초대 발송 시점
     * 이유: 언제 초대가 발송되었는지 기록하여 초대 순서 파악,
     * 응답 대기 시간 계산, 감사 로그 등에 활용하기 위해 (자동 설정)
     */
    @CreatedDate
    @Column(name = "invited_at", nullable = false, updatable = false)
    private LocalDateTime invitedAt;

    /**
     * 참여자의 초대 응답 상태를 나타내는 열거형
     * 이유: 참여자가 초대에 대해 가질 수 있는 모든 응답 상태를 체계적으로 정의하여
     * 각 상태별로 적절한 비즈니스 로직과 UI 표시를 제공하기 위해
     */
    public enum ResponseStatus {
        /** 초대됨: 초대장이 발송되었으나 아직 응답하지 않은 상태 */
        INVITED("초대됨"),
        /** 참여: 초대를 수락하여 약속 참여를 확정한 상태 */
        ACCEPTED("참여"),
        /** 거부: 초대를 거절하여 약속에 참여하지 않는 상태 */
        REJECTED("거부");

        private final String displayName;

        /**
         * ResponseStatus 생성자
         * 이유: 각 상태별로 사용자에게 표시될 한글 이름을 설정하여
         * UI에서 직관적인 상태 정보를 제공하기 위해
         */
        ResponseStatus(String displayName) {
            this.displayName = displayName;
        }

        /**
         * 응답 상태의 사용자 친화적인 표시명을 반환하는 메서드
         * 이유: UI 화면, 알림 메시지에서 참여자 상태를 사용자가 이해하기 쉽게 표시하기 위해
         * 시스템 내부 코드값 대신 한글 표시명을 제공하여 사용자 경험 향상
         * 
         * @return 상태의 한글 표시명
         */
        public String getDisplayName() {
            return this.displayName;
        }

        /**
         * 응답 상태가 최종 결정된 상태인지 확인하는 메서드
         * 이유: 리마인더 발송, 통계 계산 등에서 아직 결정이 필요한 참여자를 구분하기 위해
         * 
         * @return 최종 결정 상태 여부 (ACCEPTED, REJECTED인 경우 true)
         */
        public boolean isFinal() {
            return this == ACCEPTED || this == REJECTED;
        }

        /**
         * 긍정적인 응답인지 확인하는 메서드
         * 이유: 실제 참여 인원 계산 및 알림 발송 대상 선정에 활용하기 위해
         * 
         * @return 긍정적 응답 여부 (ACCEPTED인 경우에만 true)
         */
        public boolean isPositive() {
            return this == ACCEPTED;
        }
    }

    /**
     * 참여자가 약속에 실제 참여했는지 여부를 확인하는 메서드
     * 이유: 참여 의사 표명(ACCEPTED)과 실제 참여 확정을 구분하여
     * 정확한 참여 통계 산출 및 노쇼(no-show) 관리에 활용하기 위해
     * 
     * @return 실제 참여 여부 (joinedAt이 설정되어 있으면 true)
     */
    public boolean hasActuallyJoined() {
        return joinedAt != null;
    }

    /**
     * 참여자의 초대 응답 상태를 안전하게 변경하는 메서드
     * 이유: 단순 상태 변경뿐만 아니라 상태별 부가 작업(참여시간 설정/초기화)을
     * 원자적으로 처리하여 데이터 일관성을 보장하기 위해
     * 
     * @param newResponse 변경할 새로운 응답 상태 (null 체크 포함)
     * @throws IllegalArgumentException 잘못된 상태값인 경우
     */
    public void updateResponse(ResponseStatus newResponse) {
        // 입력값 유효성 검증
        // 이유: null 상태로의 변경을 방지하여 데이터 무결성을 보장하기 위해
        if (newResponse == null) {
            throw new IllegalArgumentException("응답 상태는 null일 수 없습니다");
        }

        // 이전 상태 저장 (로깅 및 히스토리 추적 용도)
        // 이유: 상태 변경 과정을 추적하고 문제 발생 시 디버깅에 활용하기 위해
        ResponseStatus previousStatus = this.response;
        this.response = newResponse;
        
        // 상태별 부가 작업 처리
        handleResponseStatusChange(newResponse, previousStatus);
    }

    /**
     * 초대 후 경과 시간을 계산하는 메서드
     * 이유: 응답 대기 시간을 추적하여 리마인더 발송 시점 결정 및
     * 장기간 미응답 참여자에 대한 후속 조치를 위해
     * 
     * @return 초대 후 경과 시간 (분 단위)
     */
    public long getMinutesSinceInvited() {
        if (invitedAt == null) {
            return 0;
        }
        return java.time.Duration.between(invitedAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * 응답 상태 변경에 따른 부가 작업을 처리하는 private 메서드
     * 이유: 상태별 처리 로직을 분리하여 메인 메서드의 복잡성을 줄이고
     * 각 상태 전환에 대한 비즈니스 규칙을 명확히 관리하기 위해
     * 
     * @param newStatus 새로운 상태
     * @param previousStatus 이전 상태
     */
    private void handleResponseStatusChange(ResponseStatus newStatus, ResponseStatus previousStatus) {
        switch (newStatus) {
            case ACCEPTED:
                // 수락한 경우 참여 시간 설정 (최초 수락 시에만)
                // 이유: 실제 참여 시점을 정확히 기록하여 통계 및 알림에 활용
                if (this.joinedAt == null) {
                    this.joinedAt = LocalDateTime.now();
                }
                break;
                
            case REJECTED:
            case INVITED:
                // 거부하거나 초대 상태로 되돌아간 경우 참여 시간 초기화
                // 이유: 상태 변경 시 데이터 일관성을 유지하고 잘못된 참여 통계를 방지
                this.joinedAt = null;
                break;
        }
    }

    /**
     * 정적 팩토리 메서드 - 새로운 참여자 생성
     * 이유: 복잡한 연관관계와 기본값을 가진 객체를 안전하게 생성하고
     * 필수 필드 누락을 방지하며 코드 가독성을 향상시키기 위해
     * 
     * @param meetingId 약속 ID (필수)
     * @param userId 참여자 사용자 ID (필수)
     * @param meeting 약속 엔티티 (선택, 지연 로딩 시 null 가능)
     * @param response 초기 응답 상태 (선택, 기본값 INVITED)
     */
    public static MeetingParticipant create(Long meetingId, Long userId, Meeting meeting, ResponseStatus response) {
        // 필수 필드 유효성 검증
        // 이유: 생성 시점에 필수 데이터의 누락을 방지하기 위해
        if (meetingId == null) {
            throw new IllegalArgumentException("약속 ID는 필수입니다");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }

        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeetingId(meetingId);
        participant.setUserId(userId);
        participant.setMeeting(meeting);
        // 기본값 설정 - response가 null인 경우 INVITED로 설정
        participant.setResponse(response != null ? response : ResponseStatus.INVITED);
        
        return participant;
    }

    /**
     * 정적 팩토리 메서드 - 기본 참여자 생성 (INVITED 상태)
     * 이유: 간단한 참여자 생성을 위한 편의 메서드
     */
    public static MeetingParticipant createInvited(Long meetingId, Long userId) {
        return create(meetingId, userId, null, ResponseStatus.INVITED);
    }
}