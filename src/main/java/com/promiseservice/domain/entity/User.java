package com.promiseservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 계정 정보를 저장하는 엔티티
 * 이유: 서비스의 핵심 사용자 계정 정보를 저장하고 인증 및 식별을 위해 필요
 * 실제 데이터는 UserService에서 관리되며, PromiseService는 외래키로만 참조
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * UserService의 user_profile 테이블과의 관계
     * 이유: 사용자 프로필 정보를 통해 약속 참여자의 상세 정보 조회를 위해
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;
}

