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
 * 사용자 동의 엔티티
 * 이유: 사용자의 개인정보 처리 동의 현황을 관리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_consents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserConsent {

    /**
     * PK
     * 이유: 사용자 동의 정보를 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 ID
     * 이유: 어떤 사용자의 동의 정보인지 식별하기 위해
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * 카카오톡 동의
     * 이유: 사용자가 카카오톡 알림 수신에 동의했는지 여부를 저장하기 위해
     */
    @Column(name = "talk_message_consent", nullable = false)
    private Boolean talkMessageConsent;

    /**
     * 친구 동의
     * 이유: 사용자가 친구 목록 공유에 동의했는지 여부를 저장하기 위해
     */
    @Column(name = "friends_consent", nullable = false)
    private Boolean friendsConsent;

    /**
     * 마케팅 동의
     * 이유: 사용자가 마케팅 정보 수신에 동의했는지 여부를 저장하기 위해
     */
    @Column(name = "marketing_consent", nullable = false)
    private Boolean marketingConsent;

    /**
     * 추가 동의 JSON
     * 이유: 기타 동의 사항을 JSON 형태로 저장하기 위해
     */
    @Column(name = "consent_details", columnDefinition = "TEXT")
    private String consentDetails;

    /**
     * 생성 시각
     * 이유: 동의 정보가 언제 생성되었는지 기록하기 위해
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     * 이유: 동의 정보가 언제 마지막으로 수정되었는지 기록하기 위해
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 사용자와의 관계
     * 이유: 사용자 정보를 조회하기 위해
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserProfile user;
}
