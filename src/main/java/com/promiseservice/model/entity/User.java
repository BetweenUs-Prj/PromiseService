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
 * 사용자 엔티티
 * 이유: 사용자의 기본 정보와 프로필을 관리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    /**
     * PK
     * 이유: 사용자를 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /**
     * 사용자 이름
     * 이유: 사용자를 식별할 수 있는 기본적인 이름 정보
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * 프로필 이미지
     * 이유: 사용자의 프로필 이미지를 표시하기 위해
     */
    @Column(name = "profile_image", length = 500)
    private String profileImage;

    /**
     * OAuth 제공자 ID
     * 이유: 카카오 OAuth2 연동을 위한 제공자 ID
     */
    @Column(name = "provider_id", length = 255)
    private String providerId;

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
