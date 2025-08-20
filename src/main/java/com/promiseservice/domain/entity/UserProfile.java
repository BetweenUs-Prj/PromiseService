package com.promiseservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * UserService의 user_profiles 테이블을 참조하는 엔티티
 * 실제 데이터는 UserService에서 관리되며, PromiseService는 외래키로만 참조
 */
@Entity
@Table(name = "user_profiles")
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

    @Column(length = 1000)  // H2 호환을 위해 VARCHAR로 변경
    private String bio;

    @Column
    private String location;

    @Column
    private String website;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_transport")
    private PreferredTransport preferredTransport;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PreferredTransport {
        WALK, BICYCLE, PUBLIC_TRANSPORT, CAR, MOTORCYCLE
    }
}

