package com.promiseservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 정보를 저장하는 엔티티
 * 이유: 유저 상세 정보(프로필)을 저장하고 user와 1:1 관계를 유지하기 위해
 * 각 user_id마다 하나만 존재 가능
 */
@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String location;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_transport")
    private PreferredTransport preferredTransport = PreferredTransport.WALK;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 선호 교통수단 열거형
     * 이유: 약속 장소 추천 시 사용자의 이동 수단을 고려하기 위해
     */
    public enum PreferredTransport {
        WALK, BICYCLE, PUBLIC_TRANSPORT, CAR, MOTORCYCLE
    }
}

