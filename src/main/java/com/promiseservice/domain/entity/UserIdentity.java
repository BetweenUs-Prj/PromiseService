package com.promiseservice.domain.entity;

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
    
    // 생성자
    public UserIdentity(Long id, Long userId, Provider provider, String providerUserId) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }
    
    // 정적 팩토리 메서드
    public static UserIdentity of(Long userId, Provider provider, String providerUserId) {
        return new UserIdentity(null, userId, provider, providerUserId);
    }
}
