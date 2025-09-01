package com.promiseservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * OAuth 사용자 신원 정보 엔티티
 * 
 * 용도: 카카오 등 OAuth 제공자의 사용자 ID와 내부 사용자 ID를 매핑하기 위해
 * 
 * 사용 시나리오:
 * 1. OAuth 로그인 시 사용자 인증 및 식별
 * 2. OAuth 제공자별 사용자 정보 관리
 * 3. 토큰 정보 저장 및 관리
 * 4. 사용자 프로필 정보 동기화
 * 
 * 특징:
 * - OAuth 제공자별 고유 식별자 저장
 * - 액세스 토큰과 리프레시 토큰 관리
 * - 사용자 프로필 정보 동기화
 * - 토큰 만료 시간 관리
 * 
 * @author UserService Team
 * @since 2025-01-01
 */
@Entity
@Table(name = "user_identity", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIdentity {
    
    /**
     * OAuth 사용자 신원 정보 고유 식별자
     * 
     * 설명: 시스템에서 생성된 OAuth 사용자 신원 정보의 고유 번호
     * 형식: Long 타입의 정수값
     * 특징: 데이터베이스의 auto-increment 값
     * 
     * 예시: 1L, 2L, 3L
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 내부 사용자 ID
     * 
     * 설명: 시스템 내부에서 관리하는 사용자의 고유 식별자
     * 형식: Long 타입의 정수값
     * 특징: UserService의 users 테이블과 연결
     * 
     * 예시: 12L, 45L, 77L
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * OAuth 제공자 이름
     * 
     * 설명: OAuth 인증을 제공하는 서비스의 이름
     * 형식: String 타입 (최대 20자)
     * 특징: 
     * - 현재는 "KAKAO"만 지원
     * - 향후 "GOOGLE", "NAVER", "APPLE" 등 확장 가능
     * - 대문자로 저장하여 일관성 유지
     * 
     * 예시: "KAKAO", "GOOGLE", "NAVER"
     * 
     * 제약사항: null이 아니어야 하며, 최대 20자까지 입력 가능
     */
    @Column(name = "provider", nullable = false, length = 20)
    private String provider;
    
    /**
     * OAuth 제공자에서 발급한 사용자 ID
     * 
     * 설명: OAuth 제공자에서 발급한 사용자의 고유 식별자
     * 형식: String 타입 (최대 100자)
     * 특징:
     * - 카카오의 경우 숫자로 구성된 문자열
     * - 제공자별로 다른 형식일 수 있음
     * - provider와 함께 유니크 제약조건 구성
     * 
     * 예시: "4399968638", "1234567890"
     * 
     * 제약사항: null이 아니어야 하며, 최대 100자까지 입력 가능
     */
    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;
    
    /**
     * 제공자별 사용자 닉네임
     * 
     * 설명: OAuth 제공자에서 제공하는 사용자의 닉네임
     * 형식: String 타입 (최대 100자)
     * 특징:
     * - 카카오 닉네임 등
     * - 메시지 발송 시 사용자 식별에 활용
     * - 개인화된 메시지 작성에 사용
     * 
     * 예시: "홍길동", "이몽룡", "성춘향"
     * 
     * 제약사항: null 허용, 최대 100자까지 입력 가능
     */
    @Column(name = "nickname", length = 100)
    private String nickname;
    
    /**
     * 제공자별 프로필 이미지 URL
     * 
     * 설명: OAuth 제공자에서 제공하는 사용자의 프로필 이미지
     * 형식: String 타입 (최대 500자)
     * 특징:
     * - 카카오 프로필 이미지 등
     * - UI에서 사용자 프로필 표시에 활용
     * - 이미지 로드 실패 시 기본 이미지로 대체 가능
     * 
     * 예시: "https://k.kakaocdn.net/dn/abc.jpg"
     * 
     * 제약사항: null 허용, 최대 500자까지 입력 가능
     * 
     * 주의사항: URL이 유효하지 않을 수 있으므로 클라이언트에서 이미지 로드 실패 처리 필요
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    /**
     * 액세스 토큰
     * 
     * 설명: OAuth 제공자 API 호출 시 필요한 액세스 토큰
     * 형식: String 타입 (TEXT)
     * 특징:
     * - OAuth 제공자 API 호출에 사용
     * - 보안을 위해 암호화 저장 권장
     * - 만료 시간이 있음
     * 
     * 예시: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * 
     * 제약사항: null 허용
     * 
     * 주의사항: 민감한 정보이므로 암호화하여 저장하고 안전하게 관리해야 함
     */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;
    
    /**
     * 리프레시 토큰
     * 
     * 설명: 액세스 토큰 갱신 시 필요한 리프레시 토큰
     * 형식: String 타입 (TEXT)
     * 특징:
     * - 액세스 토큰 만료 시 새로운 액세스 토큰 발급에 사용
     * - 보안을 위해 암호화 저장 권장
     * - 액세스 토큰보다 긴 유효기간
     * 
     * 예시: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * 
     * 제약사항: null 허용
     * 
     * 주의사항: 민감한 정보이므로 암호화하여 저장하고 안전하게 관리해야 함
     */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
    
    /**
     * 토큰 만료 시간
     * 
     * 설명: 액세스 토큰의 만료 시간
     * 형식: LocalDateTime 타입
     * 특징:
     * - 토큰 유효성 검증에 사용
     * - 토큰 갱신 시점 판단에 활용
     * - 만료 전 자동 갱신 처리 가능
     * 
     * 예시: "2025-01-01T12:00:00"
     * 
     * 제약사항: null 허용
     */
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    /**
     * 엔티티 생성 시간
     * 
     * 설명: OAuth 사용자 신원 정보가 시스템에 생성된 시간
     * 형식: LocalDateTime 타입
     * 특징: 자동으로 설정되며 수정 불가
     * 
     * 예시: "2025-01-01T10:00:00"
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 엔티티 수정 시간
     * 
     * 설명: OAuth 사용자 신원 정보가 마지막으로 수정된 시간
     * 형식: LocalDateTime 타입
     * 특징: 자동으로 업데이트됨
     * 
     * 예시: "2025-01-01T15:30:00"
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 엔티티 저장 전 실행되는 메서드
     * 
     * 용도: 생성 시간과 수정 시간을 자동으로 설정하여 데이터 일관성 보장
     * 
     * 동작 과정:
     * 1. 현재 시간을 생성 시간으로 설정
     * 2. 현재 시간을 수정 시간으로 설정
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 엔티티 수정 전 실행되는 메서드
     * 
     * 용도: 수정 시간을 자동으로 업데이트하여 데이터 최신성 보장
     * 
     * 동작 과정:
     * 1. 현재 시간을 수정 시간으로 설정
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * OAuth 사용자 신원 생성
     * 
     * 용도: OAuth 사용자 신원 정보를 생성할 때 필수 및 선택 필드를 안전하게 설정
     * 
     * 매개변수:
     * - userId: 내부 사용자 ID
     * - provider: OAuth 제공자 이름
     * - providerUserId: OAuth 제공자 사용자 ID
     * - nickname: 사용자 닉네임
     * - profileImageUrl: 프로필 이미지 URL
     * 
     * 반환값:
     * - UserIdentity: 생성된 OAuth 사용자 신원 정보
     * 
     * 사용 예시:
     * ```java
     * UserIdentity identity = UserIdentity.create(12L, "KAKAO", "4399968638", "홍길동", "https://...");
     * ```
     */
    public static UserIdentity create(Long userId, String provider, String providerUserId, 
                                    String nickname, String profileImageUrl) {
        UserIdentity identity = new UserIdentity();
        identity.setUserId(userId);
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        identity.setNickname(nickname);
        identity.setProfileImageUrl(profileImageUrl);
        return identity;
    }
    
    /**
     * 기본 OAuth 정보만으로 사용자 신원 생성
     * 
     * 용도: 최소한의 정보만으로 OAuth 사용자 신원을 생성할 때 사용
     * 
     * 매개변수:
     * - userId: 내부 사용자 ID
     * - provider: OAuth 제공자 이름
     * - providerUserId: OAuth 제공자 사용자 ID
     * 
     * 반환값:
     * - UserIdentity: 생성된 OAuth 사용자 신원 정보
     * 
     * 사용 예시:
     * ```java
     * UserIdentity identity = UserIdentity.of(12L, "KAKAO", "4399968638");
     * ```
     */
    public static UserIdentity of(Long userId, String provider, String providerUserId) {
        UserIdentity identity = new UserIdentity();
        identity.setUserId(userId);
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        return identity;
    }
    
    /**
     * 토큰 정보 업데이트
     * 
     * 용도: OAuth 토큰 갱신 시 관련 정보를 원자적으로 업데이트
     * 
     * 매개변수:
     * - accessToken: 새로운 액세스 토큰
     * - refreshToken: 새로운 리프레시 토큰
     * - expiresAt: 토큰 만료 시간
     * 
     * 사용 예시:
     * ```java
     * identity.updateTokens(newAccessToken, newRefreshToken, LocalDateTime.now().plusHours(1));
     * ```
     */
    public void updateTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
    }
    
    /**
     * 프로필 정보 업데이트
     * 
     * 용도: 사용자 프로필 정보 변경 시 일관되게 업데이트
     * 
     * 매개변수:
     * - nickname: 새로운 닉네임
     * - profileImageUrl: 새로운 프로필 이미지 URL
     * 
     * 사용 예시:
     * ```java
     * identity.updateProfile("새로운닉네임", "https://new-image.jpg");
     * ```
     */
    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
    
    /**
     * 토큰 만료 여부 확인
     * 
     * 용도: 토큰 유효성 검증을 위해 만료 여부를 확인
     * 
     * 반환값:
     * - boolean: 토큰이 만료되었으면 true, 유효하면 false
     * 
     * 사용 예시:
     * ```java
     * if (identity.isTokenExpired()) {
     *     // 토큰 갱신 로직
     * }
     * ```
     */
    public boolean isTokenExpired() {
        return tokenExpiresAt != null && tokenExpiresAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * 카카오 사용자인지 확인
     * 
     * 용도: 카카오 특화 기능 사용 시 사용자 타입을 판별
     * 
     * 반환값:
     * - boolean: 카카오 사용자이면 true, 아니면 false
     * 
     * 사용 예시:
     * ```java
     * if (identity.isKakaoUser()) {
     *     // 카카오 특화 기능 실행
     * }
     * ```
     */
    public boolean isKakaoUser() {
        return "KAKAO".equalsIgnoreCase(this.provider);
    }
}
