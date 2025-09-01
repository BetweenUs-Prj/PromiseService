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
 * 약속 히스토리 엔티티
 * 이유: 약속의 변경 이력을 추적하고 감사하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "meeting_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MeetingHistory {

    /**
     * PK
     * 이유: 약속 히스토리를 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약속 ID
     * 이유: 어떤 약속의 히스토리인지 식별하기 위해
     */
    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    /**
     * 수행된 액션
     * 이유: 어떤 작업이 수행되었는지 기록하기 위해
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * 액션을 수행한 사용자 ID
     * 이유: 누가 해당 작업을 수행했는지 기록하기 위해
     */
    @Column(name = "action_by", nullable = false)
    private Long actionBy;

    /**
     * 액션 상세 정보
     * 이유: 액션에 대한 추가적인 상세 정보를 저장하기 위해
     */
    @Column(name = "action_details", columnDefinition = "JSON")
    private String actionDetails;

    /**
     * 히스토리 생성 시간
     * 이유: 언제 해당 히스토리가 생성되었는지 기록하기 위해
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
     * 액션 수행자와의 관계
     * 이유: 사용자 정보를 조회하기 위해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_by", insertable = false, updatable = false)
    private UserProfile actionByUser;
}
