package com.promiseservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 약속 참가자 엔티티
 * 이유: 약속에 참여하는 사용자들의 정보와 상태를 관리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "meeting_participant")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MeetingParticipant {

    /**
     * PK
     * 이유: 약속 참가자를 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약속 ID
     * 이유: 어떤 약속에 참여하는지 식별하기 위해
     */
    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    /**
     * 참가자 ID
     * 이유: 누가 약속에 참여하는지 식별하기 위해
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 참여 응답 상태
     * 이유: 참가자의 현재 상태를 관리하기 위해
     */
    @Column(name = "response", nullable = false, length = 50)
    private String response;

    /**
     * 참여 확정 시각
     * 이유: 참가자가 언제 약속에 참여했는지 기록하기 위해
     */
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    /**
     * 초대 발송 시각
     * 이유: 참가자에게 언제 초대가 발송되었는지 기록하기 위해
     */
    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;

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
