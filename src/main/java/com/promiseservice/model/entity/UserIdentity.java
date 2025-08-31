package com.promiseservice.model.entity;

import com.promiseservice.enums.Provider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * OAuth 사용자 신원 정보 엔티티
 * 이유: 카카오 등 OAuth 제공자의 사용자 ID와 내부 사용자 ID를 매핑하기 위해
 * OAuth 로그인 시 사용자 인증 및 식별에 사용
 */
@Entity
@Table(name = "user_identity", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIdentity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private Provider provider;
    
    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;
    
    /**
     * 제공자별 사용자 닉네임 (카카오 닉네임 등)
     * 이유: 메시지 발송 시 사용자 식별 및 개인화된 메시지 작성에 활용
     */
    @Column(name = "nickname", length = 100)
    private String nickname;
    
    /**
     * 제공자별 프로필 이미지 URL
     * 이유: 사용자 프로필 표시 및 UI에서 활용
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    /**
     * 액세스 토큰 (암호화 저장 권장)
     * 이유: API 호출 시 필요한 토큰 정보 저장
     */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;
    
    /**
     * 리프레시 토큰 (암호화 저장 권장)
     * 이유: 액세스 토큰 갱신 시 필요
     */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    /**
     * 토큰 만료 시간
     * 이유: 토큰 유효성 검증 및 갱신 시점 판단
     */
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 정적 팩토리 메서드 - OAuth 사용자 신원 생성
     * 이유: 객체 생성 시 필수 및 선택 필드를 안전하게 설정하기 위해
     */
    public static UserIdentity create(Long userId, Provider provider, String providerUserId, 
                                    String nickname, String profileImageUrl) {
        UserIdentity identity = new UserIdentity();
        identity.setUserId(userId);
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        identity.setNickname(nickname);
        identity.setProfileImageUrl(profileImageUrl);
        return identity;
    }
    
    /**
     * 정적 팩토리 메서드 - 기본 OAuth 정보만
     * 이유: 엔티티 생성의 의도를 명확히 하고 유효성 검증을 일관되게 적용하기 위해
     */
    public static UserIdentity of(Long userId, Provider provider, String providerUserId) {
        UserIdentity identity = new UserIdentity();
        identity.setUserId(userId);
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        return identity;
    }
    
    /**
     * 토큰 정보 업데이트 메서드
     * 이유: 토큰 갱신 시 관련 정보를 원자적으로 업데이트하기 위해
     */
    public void updateTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
    }
    
    /**
     * 프로필 정보 업데이트 메서드
     * 이유: 사용자 프로필 정보 변경 시 일관되게 업데이트하기 위해
     */
    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
    
    /**
     * 토큰 만료 여부 확인 메서드
     * 이유: 토큰 유효성 검증을 위해
     */
    public boolean isTokenExpired() {
        return tokenExpiresAt != null && tokenExpiresAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * 카카오 사용자인지 확인하는 메서드
     * 이유: 카카오 특화 기능 사용 시 판별을 위해
     */
    public boolean isKakaoUser() {
        return Provider.KAKAO.equals(this.provider);
    }
}
