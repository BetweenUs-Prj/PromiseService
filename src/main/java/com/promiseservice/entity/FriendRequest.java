package com.promiseservice.entity;

import com.promiseservice.enums.FriendRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 친구 요청을 관리하는 엔티티
 * 이유: 친구 요청과 친구 관계를 분리하여 요청의 생명주기를 독립적으로 관리하기 위해
 */

@Getter
@Setter
@Entity
@Table(name = "friend_requests")
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 친구 요청을 보내는 사람 ID
    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    // 친구 요청을 받는 사람 ID
    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    // 친구 요청 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendRequestStatus status;

    // 요청 메시지 (선택사항)
    @Column(name = "message", length = 500)
    private String message;

    // 요청 생성 시간
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 요청 처리 시간 (수락/거절 시)
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // 낙관적 락을 위한 버전 필드
    // 이유: 교차 요청 처리 시 동시성 문제 방지
    @Version
    private Long version;

    // 기본 생성자
    public FriendRequest() {}

    // 생성자
    public FriendRequest(Long fromUserId, Long toUserId, String message) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.message = message;
        this.status = FriendRequestStatus.PENDING;
    }


    /**
     * 친구 요청을 수락하는 메서드
     * 이유: 요청 상태를 ACCEPTED로 변경하고 처리 시간을 기록하기 위해
     */
    public void accept() {
        this.status = FriendRequestStatus.ACCEPTED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 친구 요청을 거절하는 메서드
     * 이유: 요청 상태를 REJECTED로 변경하고 처리 시간을 기록하기 위해
     */
    public void reject() {
        this.status = FriendRequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 친구 요청을 취소하는 메서드
     * 이유: 요청 상태를 CANCELLED로 변경하고 처리 시간을 기록하기 위해
     */
    public void cancel() {
        this.status = FriendRequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 엔티티 저장 전 실행되는 메서드
     * 이유: 생성 시간을 자동으로 설정하여 데이터 일관성 보장
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    

    

}

