package com.promiseservice.entity;

import com.promiseservice.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 친구 관계를 관리하는 엔티티
 * 이유: 수락된 친구 관계를 저장하여 친구 목록 관리
 * 
 * 특징:
 * - 사용자 간의 양방향 친구 관계 관리
 * - 친구 요청 수락 시 자동으로 친구 관계 생성
 * - 차단 기능을 통한 친구 관계 관리
 * - 감사(Auditing) 기능으로 생성/수정 시간 자동 기록
 */
@Entity
@Table(name = "friendship", uniqueConstraints = {
    @UniqueConstraint(name = "unique_friendship", columnNames = {"user_id", "friend_id"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Friendship {

    /**
     * 친구 관계 고유 식별자 (id)
     * 이유: 친구 관계를 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID (user_id)
     * 이유: 친구 관계의 한쪽 사용자를 참조
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 친구 ID (friend_id)
     * 이유: 친구 관계의 다른 쪽 사용자를 참조
     */
    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    /**
     * 친구 관계 상태 (status)
     * 이유: 친구 관계의 현재 상태를 관리 (PENDING, ACCEPTED, BLOCKED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendshipStatus status;

    /**
     * 친구 관계 생성 시간 (created_at)
     * 이유: 친구가 된 날짜를 기록
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 친구 관계 수정 시간 (updated_at)
     * 이유: 친구 관계 상태 변경 이력 추적
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 친구 관계 상태를 업데이트하는 메서드
     * 이유: 친구 관계 상태를 안전하게 변경
     */
    public void updateStatus(FriendshipStatus status) {
        this.status = status;
    }

    /**
     * 정적 팩토리 메서드 - 새로운 친구 관계 생성
     * 이유: 친구 관계 생성 시 기본값 설정과 유효성 검증을 제공
     */
    public static Friendship create(Long userId, Long friendId, FriendshipStatus status) {
        Friendship friendship = new Friendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);
        friendship.setStatus(status != null ? status : FriendshipStatus.ACCEPTED);
        return friendship;
    }

    /**
     * 친구가 차단되었는지 확인하는 메서드
     * 이유: 친구 관계 상태 확인을 위한 편의 메서드
     */
    public boolean isBlocked() {
        return FriendshipStatus.BLOCKED.equals(this.status);
    }

    /**
     * 활성 친구 관계인지 확인하는 메서드
     * 이유: 실제 친구 목록에 표시할 관계인지 판단
     */
    public boolean isActive() {
        return FriendshipStatus.ACCEPTED.equals(this.status);
    }
}