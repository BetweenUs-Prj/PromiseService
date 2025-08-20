package com.promiseservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_participants")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MeetingParticipant {

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
    private ResponseStatus response = ResponseStatus.INVITED;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @CreatedDate
    @Column(name = "invited_at", nullable = false, updatable = false)
    private LocalDateTime invitedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public enum ResponseStatus {
        INVITED, ACCEPTED, REJECTED
    }
}


