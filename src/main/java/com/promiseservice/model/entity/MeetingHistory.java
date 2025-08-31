package com.promiseservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 약속 히스토리 정보를 저장하는 엔티티
 * 이유: 약속에서 발생하는 모든 활동을 기록하여 추적 가능성을 제공하고 사용자에게 알림 및 히스토리를 제공하기 위해
 */
@Entity
@Table(name = "meeting_history",
       indexes = {
           @Index(name = "idx_history_meeting_id", columnList = "meeting_id"),
           @Index(name = "idx_history_user_id", columnList = "user_id"),
           @Index(name = "idx_history_timestamp", columnList = "timestamp")
       })
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MeetingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "user_id", nullable = false)
    private Long userId; // UserService의 User.id 참조

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public enum ActionType {
        CREATED, JOINED, DECLINED, COMPLETED, CANCELLED, UPDATED;

        /**
         * 액션 타입의 사용자 친화적인 표시명을 반환하는 메서드
         * 이유: UI에서 히스토리를 사용자가 이해하기 쉽게 표시하기 위해
         * 
         * @return 액션의 표시명
         */
        public String getDisplayName() {
            switch (this) {
                case CREATED:
                    return "약속 생성";
                case JOINED:
                    return "참여 확정";
                case DECLINED:
                    return "참여 거부";
                case COMPLETED:
                    return "약속 완료";
                case CANCELLED:
                    return "약속 취소";
                case UPDATED:
                    return "약속 수정";
                default:
                    return this.name();
            }
        }
    }

    /**
     * 정적 팩토리 메서드 - 약속 생성 히스토리
     * 이유: 각 액션 타입별로 히스토리 객체를 쉽게 생성할 수 있도록 편의성 제공
     * 
     * @param meeting 약속 엔티티
     * @param userId 행동 주체 사용자 ID
     * @return MeetingHistory 객체
     */
    public static MeetingHistory createHistory(Meeting meeting, Long userId) {
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setUserId(userId);
        history.setAction(ActionType.CREATED);
        return history;
    }

    /**
     * 정적 팩토리 메서드 - 참여 확정 히스토리
     * 이유: 각 액션 타입별로 히스토리 객체를 쉽게 생성할 수 있도록 편의성 제공
     * 
     * @param meeting 약속 엔티티
     * @param userId 행동 주체 사용자 ID
     * @return MeetingHistory 객체
     */
    public static MeetingHistory joinHistory(Meeting meeting, Long userId) {
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setUserId(userId);
        history.setAction(ActionType.JOINED);
        return history;
    }

    /**
     * 정적 팩토리 메서드 - 참여 거부 히스토리
     * 이유: 각 액션 타입별로 히스토리 객체를 쉽게 생성할 수 있도록 편의성 제공
     * 
     * @param meeting 약속 엔티티
     * @param userId 행동 주체 사용자 ID
     * @return MeetingHistory 객체
     */
    public static MeetingHistory declineHistory(Meeting meeting, Long userId) {
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setUserId(userId);
        history.setAction(ActionType.DECLINED);
        return history;
    }

    /**
     * 정적 팩토리 메서드 - 약속 수정 히스토리
     * 이유: 각 액션 타입별로 히스토리 객체를 쉽게 생성할 수 있도록 편의성 제공
     * 
     * @param meeting 약속 엔티티
     * @param userId 행동 주체 사용자 ID
     * @return MeetingHistory 객체
     */
    public static MeetingHistory updateHistory(Meeting meeting, Long userId) {
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setUserId(userId);
        history.setAction(ActionType.UPDATED);
        return history;
    }

    /**
     * 정적 팩토리 메서드 - 약속 완료 히스토리
     * 이유: 각 액션 타입별로 히스토리 객체를 쉽게 생성할 수 있도록 편의성 제공
     * 
     * @param meeting 약속 엔티티
     * @param userId 행동 주체 사용자 ID
     * @return MeetingHistory 객체
     */
    public static MeetingHistory completeHistory(Meeting meeting, Long userId) {
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setUserId(userId);
        history.setAction(ActionType.COMPLETED);
        return history;
    }

    /**
     * 정적 팩토리 메서드 - 약속 취소 히스토리
     * 이유: 각 액션 타입별로 히스토리 객체를 쉽게 생성할 수 있도록 편의성 제공
     * 
     * @param meeting 약속 엔티티
     * @param userId 행동 주체 사용자 ID
     * @return MeetingHistory 객체
     */
    public static MeetingHistory cancelHistory(Meeting meeting, Long userId) {
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setUserId(userId);
        history.setAction(ActionType.CANCELLED);
        return history;
    }
}