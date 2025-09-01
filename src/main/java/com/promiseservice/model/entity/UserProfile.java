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
 * 사용자 프로필 엔티티
 * 이유: 사용자의 기본 정보와 프로필을 관리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {

    /**
     * PK
     * 이유: 사용자를 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 이름
     * 이유: 사용자를 식별할 수 있는 기본적인 이름 정보
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 자기소개
     * 이유: 사용자의 개인적인 소개나 설명을 제공하기 위해
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /**
     * 거주 지역
     * 이유: 사용자의 위치 기반 서비스나 약속 장소 추천을 위해
     */
    @Column(name = "location", length = 255)
    private String location;

    /**
     * 개인 웹사이트
     * 이유: 사용자의 추가 정보나 링크를 제공하기 위해
     */
    @Column(name = "website", length = 255)
    private String website;

    /**
     * 전화번호
     * 이유: 긴급 연락이나 추가 인증을 위해
     */
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    /**
     * 프로필 이미지
     * 이유: 사용자의 프로필 이미지를 표시하기 위해
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 선호 교통수단
     * 이유: 약속 장소 추천이나 경로 안내 시 사용자의 선호도를 반영하기 위해
     */
    @Column(name = "preferred_transport", length = 50)
    private String preferredTransport;

    /**
     * 생성 시각
     * 이유: 프로필이 언제 생성되었는지 기록하기 위해
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     * 이유: 프로필이 언제 마지막으로 수정되었는지 기록하기 위해
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
