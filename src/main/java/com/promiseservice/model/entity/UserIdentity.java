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
 * OAuth 사용자 신원 정보 엔티티
 * 이유: 외부 OAuth 제공자(Kakao, Google 등)의 사용자 정보를 내부 시스템과 연결하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_identity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserIdentity {

    /**
     * PK
     * 이유: OAuth 신원 정보를 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 내부 사용자 ID
     * 이유: UserProfile과의 관계를 설정하기 위해
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * OAuth 제공자
     * 이유: 어떤 OAuth 서비스를 통해 로그인했는지 구분하기 위해
     */
    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    /**
     * OAuth 제공자 사용자 ID
     * 이유: OAuth 제공자에서 발급한 고유 식별자를 저장하기 위해
     */
    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    /**
     * 제공자별 사용자 닉네임
     * 이유: OAuth 제공자에서 사용하는 닉네임을 저장하기 위해
     */
    @Column(name = "nickname", length = 100)
    private String nickname;

    /**
     * 제공자별 프로필 이미지 URL
     * 이유: OAuth 제공자에서 제공하는 프로필 이미지를 저장하기 위해
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    /**
     * OAuth 액세스 토큰
     * 이유: OAuth API 호출 시 인증을 위해
     */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    /**
     * OAuth 리프레시 토큰
     * 이유: 액세스 토큰 만료 시 새로운 토큰을 발급받기 위해
     */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    /**
     * 토큰 만료 시간
     * 이유: 토큰의 유효성을 확인하고 갱신 시점을 결정하기 위해
     */
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    /**
     * 생성 시각
     * 이유: 신원 정보가 언제 생성되었는지 기록하기 위해
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     * 이유: 신원 정보가 언제 마지막으로 수정되었는지 기록하기 위해
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * UserProfile과의 관계
     * 이유: 내부 사용자 정보와 연결하기 위해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserProfile userProfile;
}
