package com.promiseservice.entity;

import com.promiseservice.enums.PreferredTransport;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 정보를 관리하는 엔티티 (변경함)
 * 이유: 사용자의 상세 정보를 별도 테이블로 분리하여 데이터베이스 정규화 및 성능 최적화
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    
    /**
     * 프로필 고유 식별자 (변경함)
     * 이유: 프로필 테이블의 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 사용자 이름 (변경함)
     * 이유: 사용자의 기본 식별 정보
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 자기소개 (변경함)
     * 이유: 사용자가 자신을 소개할 수 있는 공간 제공
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    /**
     * 거주 지역 (변경함)
     * 이유: 지역 기반 서비스 및 친구 추천 기능을 위해 (base_location -> location으로 변경)
     */
    @Column(name = "location")
    private String location;
    
    /**
     * 개인 웹사이트 (변경함)
     * 이유: 사용자의 개인 블로그나 포트폴리오 링크 저장
     */
    @Column(name = "website")
    private String website;
    
    /**
     * 전화번호 (변경함)
     * 이유: 연락처 정보 및 계정 보안을 위한 2차 인증
     */
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;
    
    /**
     * 아바타 이미지 URL (변경함)
     * 이유: 사용자의 프로필 이미지 (profile_image_url -> avatar_url로 변경)
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    /**
     * 선호하는 교통수단 (변경함)
     * 이유: 사용자의 이동 패턴 정보를 프로필로 이동
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_transport")
    private PreferredTransport preferredTransport;
    
    /**
     * 프로필 생성 시간 (변경함)
     * 이유: 프로필 생성 시점 기록
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 프로필 수정 시간 (변경함)
     * 이유: 프로필 정보의 최신성 확인
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 엔티티 저장 전 실행되는 메서드 (변경함)
     * 이유: 생성 시간과 수정 시간을 자동으로 설정
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 엔티티 수정 전 실행되는 메서드 (변경함)
     * 이유: 수정 시간을 자동으로 업데이트
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 빌더 생성자 (변경함)
     * 이유: 제공받은 모델과 동일한 빌더 패턴 적용
     */
    @Builder
    public UserProfile(String name, String bio, String location, String website, String phoneNumber, String avatarUrl) {
        this.name = name;
        this.bio = bio;
        this.location = location;
        this.website = website;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
    }

    /**
     * 프로필 업데이트 메서드 (변경함)
     * 이유: 제공받은 모델과 동일한 업데이트 메서드 제공
     */
    public void updateProfile(String name, String bio, String location, String website, String phoneNumber, String avatarUrl) {
        this.name = name;
        this.bio = bio;
        this.location = location;
        this.website = website;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
    }
    
}
