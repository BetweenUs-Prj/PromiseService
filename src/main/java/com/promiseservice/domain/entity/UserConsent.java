package com.promiseservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자의 각종 동의 정보를 저장하는 엔티티
 * 이유: 카카오톡 메시지 전송, 친구 목록 조회 등의 기능에 대한 사용자 동의 상태를 관리하여
 * 개인정보보호법과 카카오 정책을 준수하기 위해
 */
@Entity
@Table(name = "user_consents", indexes = {
    @Index(name = "idx_user_consents_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_user_consents_talk_message", columnList = "talk_message_consent"),
    @Index(name = "idx_user_consents_friends", columnList = "friends_consent")
})
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId; // UserService의 users.id 참조

    @Column(name = "talk_message_consent", nullable = false)
    private Boolean talkMessageConsent = false; // 카카오톡 메시지 전송 동의

    @Column(name = "friends_consent", nullable = false)
    private Boolean friendsConsent = false; // 카카오 친구 목록 조회 동의

    @Column(name = "marketing_consent", nullable = false)
    private Boolean marketingConsent = false; // 마케팅 정보 수신 동의

    @Column(name = "consent_details", columnDefinition = "TEXT")
    private String consentDetails; // 추가 동의 정보 (JSON 형태)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 카카오톡 메시지 전송이 가능한지 확인하는 메서드
     * 이유: 카카오톡 알림 전송 전에 사용자의 동의 상태를 확인하여 규정 위반을 방지하기 위해
     * 
     * @return 카카오톡 메시지 전송 가능 여부
     */
    public boolean canSendKakaoMessage() {
        return talkMessageConsent != null && talkMessageConsent;
    }

    /**
     * 카카오 친구 목록 조회가 가능한지 확인하는 메서드
     * 이유: 카카오 친구 목록 동기화 전에 사용자의 동의 상태를 확인하기 위해
     * 
     * @return 친구 목록 조회 가능 여부
     */
    public boolean canAccessKakaoFriends() {
        return friendsConsent != null && friendsConsent;
    }

    /**
     * 카카오 관련 모든 기능 사용이 가능한지 확인하는 메서드
     * 이유: 카카오 알림 시스템의 모든 기능을 사용하기 위한 종합적인 동의 상태를 확인하기 위해
     * 
     * @return 카카오 기능 사용 가능 여부
     */
    public boolean canUseKakaoFeatures() {
        return canSendKakaoMessage() && canAccessKakaoFriends();
    }

    /**
     * 카카오톡 메시지 전송 동의를 업데이트하는 메서드
     * 이유: 동의 상태 변경 시 관련 로직을 함께 처리하고 일관성을 보장하기 위해
     * 
     * @param consent 동의 여부
     */
    public void updateTalkMessageConsent(boolean consent) {
        this.talkMessageConsent = consent;
    }

    /**
     * 카카오 친구 목록 조회 동의를 업데이트하는 메서드
     * 이유: 동의 상태 변경 시 관련 로직을 함께 처리하고 일관성을 보장하기 위해
     * 
     * @param consent 동의 여부
     */
    public void updateFriendsConsent(boolean consent) {
        this.friendsConsent = consent;
    }

    /**
     * 정적 팩토리 메서드 - 기본 동의 정보 생성
     * 이유: 사용자 가입 시 기본 동의 정보를 일관되게 생성하기 위해
     * 
     * @param userId 사용자 ID
     * @return UserConsent 엔티티
     */
    public static UserConsent createDefault(Long userId) {
        UserConsent consent = new UserConsent();
        consent.setUserId(userId);
        consent.setTalkMessageConsent(false);
        consent.setFriendsConsent(false);
        consent.setMarketingConsent(false);
        return consent;
    }

    /**
     * 모든 동의를 철회하는 메서드
     * 이유: 사용자가 서비스 탈퇴 시 모든 동의를 일괄 철회하기 위해
     */
    public void revokeAllConsents() {
        this.talkMessageConsent = false;
        this.friendsConsent = false;
        this.marketingConsent = false;
    }
}
