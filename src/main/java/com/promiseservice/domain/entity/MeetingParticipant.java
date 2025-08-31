package com.promiseservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 약속 참여자 정보를 저장하는 엔티티
 * 이유: 약속에 참여하는 사용자들의 응답 상태와 참여 정보를 관리하기 위해
 */
@Entity
@Table(name = "meeting_participant", 
       indexes = {
           @Index(name = "idx_participant_meeting_id", columnList = "meeting_id"),
           @Index(name = "idx_participant_user_id", columnList = "user_id"),
           @Index(name = "idx_participant_response", columnList = "response")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_meeting_user", columnNames = {"meeting_id", "user_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false, insertable = false, updatable = false)
    private Meeting meeting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseStatus response = ResponseStatus.INVITED;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @CreatedDate
    @Column(name = "invited_at", nullable = false, updatable = false)
    private LocalDateTime invitedAt;

    public enum ResponseStatus {
        INVITED, ACCEPTED, REJECTED;

        /**
         * 응답 상태의 사용자 친화적인 표시명을 반환하는 메서드
         * 이유: UI에서 참여자 상태를 사용자가 이해하기 쉽게 표시하기 위해
         * 
         * @return 상태의 표시명
         */
        public String getDisplayName() {
            switch (this) {
                case INVITED:
                    return "초대됨";
                case ACCEPTED:
                    return "참여";
                case REJECTED:
                    return "거부";
                default:
                    return this.name();
            }
        }
    }

    /**
     * 참여자가 약속에 실제 참여했는지 여부를 확인하는 메서드
     * 이유: 참여 의사를 표명했지만 실제로는 참여하지 않은 경우를 구분하기 위해
     * 
     * @return 실제 참여 여부
     */
    public boolean hasActuallyJoined() {
        return joinedAt != null;
    }

    /**
     * 참여자의 응답 상태를 변경하는 메서드
     * 이유: 상태 변경 시 필요한 부가 작업을 한 번에 처리하기 위해
     * 
     * @param newResponse 새로운 응답 상태
     */
    public void updateResponse(ResponseStatus newResponse) {
        this.response = newResponse;
        
        // 수락한 경우 참여 시간 설정
        // 이유: 실제 참여 시점을 기록하여 통계 및 알림에 활용
        if (newResponse == ResponseStatus.ACCEPTED && this.joinedAt == null) {
            this.joinedAt = LocalDateTime.now();
        }
        
        // 거부하거나 초대 상태로 돌아간 경우 참여 시간 초기화
        // 이유: 상태 변경 시 일관성을 유지하기 위해
        if (newResponse == ResponseStatus.REJECTED || newResponse == ResponseStatus.INVITED) {
            this.joinedAt = null;
        }
    }
}