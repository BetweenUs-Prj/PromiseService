package com.promiseservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 약속 정보를 저장하는 엔티티
 * 이유: 사용자가 생성한 약속(모임) 정보를 저장하고 관리하기 위해
 */
@Entity
@Table(name = "meeting", indexes = {
    @Index(name = "idx_meeting_time", columnList = "meeting_time"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

    @Column(name = "max_participants")
    private Integer maxParticipants = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MeetingStatus status = MeetingStatus.WAITING;

    @Column(name = "location_name", length = 500)
    private String locationName;

    @Column(name = "location_address", length = 500)
    private String locationAddress;

    @Column(name = "location_coordinates", columnDefinition = "TEXT")
    private String locationCoordinates; // JSON 형태로 위도,경도

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingHistory> history = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MeetingStatus {
        WAITING, CONFIRMED, COMPLETED, CANCELLED;

        /**
         * 상태의 사용자 친화적인 표시명을 반환하는 메서드
         * 이유: 알림이나 UI에서 약속 상태를 사용자가 이해하기 쉽게 표시하기 위해
         * 
         * @return 상태의 표시명
         */
        public String getDisplayName() {
            switch (this) {
                case WAITING:
                    return "대기 중";
                case CONFIRMED:
                    return "확정";
                case COMPLETED:
                    return "완료";
                case CANCELLED:
                    return "취소";
                default:
                    return this.name();
            }
        }
    }

    /**
     * 약속의 방장을 찾는 메서드
     * 이유: 가장 먼저 초대된 참여자를 방장으로 판단하기 위해
     * 
     * @return 방장의 userId, 방장이 없으면 null
     */
    public Long getHostId() {
        return participants.stream()
                .min((p1, p2) -> p1.getInvitedAt().compareTo(p2.getInvitedAt()))
                .map(MeetingParticipant::getUserId)
                .orElse(null);
    }

    /**
     * 특정 사용자가 방장인지 확인하는 메서드
     * 이유: 권한 검증에 사용하기 위해
     * 
     * @param userId 확인할 사용자 ID
     * @return 방장 여부
     */
    public boolean isHost(Long userId) {
        Long hostId = getHostId();
        return hostId != null && hostId.equals(userId);
    }

    /**
     * 현재 참여자 수를 반환하는 메서드
     * 이유: 최대 참여자 수 제한 검증 및 UI 표시를 위해
     * 
     * @return 현재 참여자 수
     */
    public int getCurrentParticipantCount() {
        return (int) participants.stream()
                .filter(participant -> participant.getResponse() == MeetingParticipant.ResponseStatus.ACCEPTED)
                .count();
    }

    /**
     * 최대 참여자 수 도달 여부를 확인하는 메서드
     * 이유: 새로운 참여자 추가 시 제한 확인을 위해
     * 
     * @return 최대 참여자 수 도달 여부
     */
    public boolean isMaxParticipantsReached() {
        return getCurrentParticipantCount() >= maxParticipants;
    }
}