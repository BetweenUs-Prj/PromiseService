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
 * 친구 요청 엔티티
 * 이유: 사용자 간 친구 관계를 맺기 위한 요청 정보를 관리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "friend_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FriendRequest {

    /**
     * PK
     * 이유: 친구 요청을 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 요청을 보내는 사용자 ID
     * 이유: 누가 친구 요청을 보냈는지 식별하기 위해
     */
    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    /**
     * 요청을 받는 사용자 ID
     * 이유: 누구에게 친구 요청을 보냈는지 식별하기 위해
     */
    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    /**
     * 친구 요청 상태
     * 이유: 요청의 현재 상태를 관리하기 위해
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    /**
     * 요청 메시지
     * 이유: 친구 요청 시 전달할 메시지를 저장하기 위해
     */
    @Column(name = "message", length = 500)
    private String message;

    /**
     * 생성 시각
     * 이유: 친구 요청이 언제 생성되었는지 기록하기 위해
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 처리 시각
     * 이유: 친구 요청이 언제 수락/거절되었는지 기록하기 위해
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 버전
     * 이유: 낙관적 락을 위한 버전 필드
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * 요청을 보내는 사용자와의 관계
     * 이유: 사용자 정보를 조회하기 위해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", insertable = false, updatable = false)
    private UserProfile fromUser;

    /**
     * 요청을 받는 사용자와의 관계
     * 이유: 사용자 정보를 조회하기 위해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", insertable = false, updatable = false)
    private UserProfile toUser;
}
