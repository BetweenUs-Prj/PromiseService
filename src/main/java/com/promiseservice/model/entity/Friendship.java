package com.promiseservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 친구 관계 엔티티
 * 이유: 사용자 간의 친구 관계를 관리하고 상태를 추적하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "friendship")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Friendship {

    /**
     * PK
     * 이유: 친구 관계를 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID
     * 이유: 친구 관계의 주체가 되는 사용자를 식별하기 위해
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 친구 ID
     * 이유: 친구 관계의 대상이 되는 사용자를 식별하기 위해
     */
    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    /**
     * 친구 관계 상태
     * 이유: 친구 관계의 현재 상태를 관리하기 위해
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    /**
     * 생성 시각
     * 이유: 친구 관계가 언제 생성되었는지 기록하기 위해
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     * 이유: 친구 관계가 언제 마지막으로 수정되었는지 기록하기 위해
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 사용자와의 관계
     * 이유: 사용자 정보를 조회하기 위해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserProfile user;

    /**
     * 친구와의 관계
     * 이유: 친구 정보를 조회하기 위해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", insertable = false, updatable = false)
    private UserProfile friend;
}
