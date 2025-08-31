package com.promiseservice.model.entity;

import com.promiseservice.enums.PreferredTransport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * 사용자 프로필 정보를 관리하는 엔티티 (스키마에 맞게 수정)
 * 이유: 사용자의 상세 정보를 별도 테이블로 분리하여 데이터베이스 정규화 및 성능 최적화
 */
@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {
    
    /**
     * 프로필 고유 식별자 (id)
     * 이유: 프로필 테이블의 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 사용자 ID 참조 (user_id)
     * 이유: 외부 UserService의 사용자와 매핑하기 위한 사용자 ID
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    /**
     * 사용자 위치 (location)
     * 이유: 지역 기반 서비스 및 친구 추천 기능을 위해
     */
    @Column(name = "location", length = 255)
    private String location;
    
    /**
     * 전화번호 (phone_number)
     * 이유: 연락처 정보 및 계정 보안을 위한 2차 인증
     */
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;
    
    /**
     * 아바타 이미지 URL (avatar_url)
     * 이유: 사용자의 프로필 이미지
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    /**
     * 선호하는 교통수단 (preferred_transport)
     * 이유: 사용자의 이동 패턴 정보
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_transport", nullable = true)
    private PreferredTransport preferredTransport;
    
    /**
     * 프로필 생성 시간 (created_at)
     * 이유: 프로필 생성 시점 기록
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 프로필 수정 시간 (updated_at)
     * 이유: 프로필 정보의 최신성 확인
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 프로필 업데이트 메서드
     * 이유: 사용자 프로필 정보를 안전하게 업데이트
     */
    public void updateProfile(String location, String phoneNumber, String avatarUrl, PreferredTransport preferredTransport) {
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.preferredTransport = preferredTransport;
    }

    /**
     * 정적 팩토리 메서드 - 새로운 사용자 프로필 생성
     * 이유: 사용자 프로필 생성 시 필수 필드 검증과 기본값 설정을 제공
     */
    public static UserProfile create(Long userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        return profile;
    }

    /**
     * 정적 팩토리 메서드 - 완전한 프로필 정보로 생성
     * 이유: 모든 프로필 정보를 한번에 설정할 때 사용
     */
    public static UserProfile createComplete(Long userId, String location, String phoneNumber, 
                                           String avatarUrl, PreferredTransport preferredTransport) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setLocation(location);
        profile.setPhoneNumber(phoneNumber);
        profile.setAvatarUrl(avatarUrl);
        profile.setPreferredTransport(preferredTransport);
        return profile;
    }
}