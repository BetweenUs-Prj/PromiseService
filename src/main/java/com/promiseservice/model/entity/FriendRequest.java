package com.promiseservice.model.entity;

import com.promiseservice.enums.FriendRequestStatus;
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
 * 친구 요청을 관리하는 엔티티 (스키마에 맞게 수정)
 * 이유: 친구 요청과 친구 관계를 분리하여 요청의 생명주기를 독립적으로 관리하기 위해
 */
@Entity
@Table(name = "friend_request", uniqueConstraints = {
    @UniqueConstraint(name = "unique_friend_request", columnNames = {"requester_id", "addressee_id"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FriendRequest {

    /**
     * 친구 요청 고유 식별자 (id)
     * 이유: 친구 요청을 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 친구 요청을 보내는 사람 ID (requester_id)
     * 이유: 누가 친구 요청을 보냈는지 식별
     */
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    /**
     * 친구 요청을 받는 사람 ID (addressee_id)
     * 이유: 누가 친구 요청을 받았는지 식별
     */
    @Column(name = "addressee_id", nullable = false)
    private Long addresseeId;

    /**
     * 친구 요청 상태 (status)
     * 이유: 요청의 현재 상태를 관리 (PENDING, ACCEPTED, REJECTED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendRequestStatus status;

    /**
     * 요청 생성 시간 (created_at)
     * 이유: 친구 요청이 언제 보내졌는지 기록
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 요청 수정 시간 (updated_at)
     * 이유: 요청 상태 변경 이력 추적
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 친구 요청을 수락하는 메서드
     * 이유: 요청 상태를 ACCEPTED로 변경
     */
    public void accept() {
        this.status = FriendRequestStatus.ACCEPTED;
    }

    /**
     * 친구 요청을 거절하는 메서드
     * 이유: 요청 상태를 REJECTED로 변경
     */
    public void reject() {
        this.status = FriendRequestStatus.REJECTED;
    }

    /**
     * 정적 팩토리 메서드
     * 이유: 친구 요청 생성 시 기본값 설정과 유효성 검증을 제공
     */
    public static FriendRequest create(Long requesterId, Long addresseeId) {
        FriendRequest request = new FriendRequest();
        request.setRequesterId(requesterId);
        request.setAddresseeId(addresseeId);
        request.setStatus(FriendRequestStatus.PENDING);
        return request;
    }
}