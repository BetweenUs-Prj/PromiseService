package com.promiseservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 웹 서비스 친구 관계와 카카오 친구 정보를 매핑하는 엔티티
 * 이유: 카카오 친구 동기화 시 웹 서비스의 친구 관계와 카카오 친구 UUID를 매핑하여
 * 카카오톡 메시지 전송 대상을 정확히 식별하기 위해
 */
@Entity
@Table(name = "kakao_friend_map", indexes = {
    @Index(name = "idx_kakao_friend_user_id", columnList = "user_id"),
    @Index(name = "idx_kakao_friend_friend_user_id", columnList = "friend_user_id"),
    @Index(name = "idx_kakao_friend_uuid", columnList = "kakao_uuid")
}, uniqueConstraints = {
    @UniqueConstraint(name = "unique_kakao_friend_mapping", columnNames = {"user_id", "friend_user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class KakaoFriendMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // UserService의 users.id 참조 (카카오 친구 목록을 조회한 사용자)

    @Column(name = "friend_user_id", nullable = false)
    private Long friendUserId; // UserService의 users.id 참조 (친구인 사용자)

    @Column(name = "kakao_uuid", nullable = false, length = 100)
    private String kakaoUuid; // 카카오 친구의 UUID

    @Column(name = "kakao_nickname", length = 100)
    private String kakaoNickname; // 카카오에서의 닉네임

    @Column(name = "kakao_profile_image", columnDefinition = "TEXT")
    private String kakaoProfileImage; // 카카오 프로필 이미지 URL

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 매핑이 활성화되어 있는지 여부

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt; // 마지막 동기화 시간

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 카카오 친구 매핑이 유효한지 확인하는 메서드
     * 이유: 카카오 알림 전송 시 활성화되고 최신 상태인 매핑 정보만 사용하기 위해
     * 
     * @return 매핑 유효 여부
     */
    public boolean isValidMapping() {
        return isActive && kakaoUuid != null && !kakaoUuid.trim().isEmpty();
    }

    /**
     * 동기화 정보를 업데이트하는 메서드
     * 이유: 카카오 친구 정보 동기화 시 최신 정보로 업데이트하고 동기화 시간을 기록하기 위해
     * 
     * @param nickname 카카오 닉네임
     * @param profileImage 카카오 프로필 이미지 URL
     */
    public void updateSyncInfo(String nickname, String profileImage) {
        this.kakaoNickname = nickname;
        this.kakaoProfileImage = profileImage;
        this.lastSyncedAt = LocalDateTime.now();
    }

    /**
     * 매핑을 비활성화하는 메서드
     * 이유: 더 이상 카카오 친구가 아니거나 서비스에서 탈퇴한 경우 매핑을 비활성화하기 위해
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 정적 팩토리 메서드 - 카카오 친구 매핑 생성
     * 이유: 카카오 친구 동기화 시 매핑 정보를 일관되게 생성하기 위해
     * 
     * @param userId 사용자 ID
     * @param friendUserId 친구 사용자 ID
     * @param kakaoUuid 카카오 UUID
     * @param nickname 카카오 닉네임
     * @param profileImage 카카오 프로필 이미지
     * @return KakaoFriendMap 엔티티
     */
    public static KakaoFriendMap create(Long userId, Long friendUserId, String kakaoUuid, 
                                       String nickname, String profileImage) {
        KakaoFriendMap mapping = new KakaoFriendMap();
        mapping.setUserId(userId);
        mapping.setFriendUserId(friendUserId);
        mapping.setKakaoUuid(kakaoUuid);
        mapping.setKakaoNickname(nickname);
        mapping.setKakaoProfileImage(profileImage);
        mapping.setIsActive(true);
        mapping.setLastSyncedAt(LocalDateTime.now());
        return mapping;
    }
}
