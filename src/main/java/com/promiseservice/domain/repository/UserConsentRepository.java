package com.promiseservice.domain.repository;

import com.promiseservice.domain.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 동의 정보 리포지토리
 * 이유: 사용자의 각종 동의 상태 조회, 관리를 위한 데이터 액세스 계층 제공
 */
@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    /**
     * 사용자 ID로 동의 정보 조회
     * 이유: 특정 사용자의 동의 상태를 확인하기 위해
     */
    Optional<UserConsent> findByUserId(Long userId);

    /**
     * 카카오톡 메시지 전송에 동의한 사용자들 조회
     * 이유: 카카오 알림 전송 시 동의한 사용자들만 필터링하기 위해
     */
    @Query("SELECT c FROM UserConsent c WHERE c.talkMessageConsent = true")
    List<UserConsent> findUsersWithTalkMessageConsent();

    /**
     * 카카오 친구 목록 조회에 동의한 사용자들 조회
     * 이유: 카카오 친구 동기화 시 동의한 사용자들만 처리하기 위해
     */
    @Query("SELECT c FROM UserConsent c WHERE c.friendsConsent = true")
    List<UserConsent> findUsersWithFriendsConsent();

    /**
     * 카카오 모든 기능에 동의한 사용자들 조회
     * 이유: 카카오 알림 시스템을 전면적으로 사용할 수 있는 사용자들을 조회하기 위해
     */
    @Query("SELECT c FROM UserConsent c WHERE c.talkMessageConsent = true AND c.friendsConsent = true")
    List<UserConsent> findUsersWithFullKakaoConsent();

    /**
     * 특정 사용자 ID 목록의 동의 정보 조회
     * 이유: 약속 참여자들의 동의 상태를 일괄 확인하기 위해
     */
    @Query("SELECT c FROM UserConsent c WHERE c.userId IN :userIds")
    List<UserConsent> findByUserIdIn(@Param("userIds") List<Long> userIds);

    /**
     * 카카오톡 메시지 전송 동의 여부 확인
     * 이유: 카카오 알림 전송 전에 빠르게 동의 상태를 확인하기 위해
     */
    @Query("SELECT CASE WHEN c.talkMessageConsent = true THEN true ELSE false END " +
           "FROM UserConsent c WHERE c.userId = :userId")
    Boolean hasTalkMessageConsent(@Param("userId") Long userId);

    /**
     * 카카오 친구 목록 조회 동의 여부 확인
     * 이유: 카카오 친구 동기화 전에 빠르게 동의 상태를 확인하기 위해
     */
    @Query("SELECT CASE WHEN c.friendsConsent = true THEN true ELSE false END " +
           "FROM UserConsent c WHERE c.userId = :userId")
    Boolean hasFriendsConsent(@Param("userId") Long userId);

    /**
     * 동의 정보 존재 여부 확인
     * 이유: 사용자의 동의 정보 등록 여부를 확인하기 위해
     */
    boolean existsByUserId(Long userId);

    /**
     * 카카오톡 메시지 전송에 동의한 사용자 수 조회
     * 이유: 카카오 알림 서비스 이용 현황을 파악하기 위해
     */
    @Query("SELECT COUNT(c) FROM UserConsent c WHERE c.talkMessageConsent = true")
    long countUsersWithTalkMessageConsent();

    /**
     * 카카오 친구 목록 조회에 동의한 사용자 수 조회
     * 이루: 카카오 친구 동기화 서비스 이용 현황을 파악하기 위해
     */
    @Query("SELECT COUNT(c) FROM UserConsent c WHERE c.friendsConsent = true")
    long countUsersWithFriendsConsent();

    /**
     * 특정 사용자 ID 목록에서 카카오톡 메시지 전송에 동의한 사용자들의 ID 조회
     * 이유: 약속 참여자 중에서 카카오 알림 전송이 가능한 사용자들만 필터링하기 위해
     */
    @Query("SELECT c.userId FROM UserConsent c WHERE c.userId IN :userIds AND c.talkMessageConsent = true")
    List<Long> findUserIdsWithTalkMessageConsentFromList(@Param("userIds") List<Long> userIds);

    /**
     * 특정 사용자 ID 목록에서 카카오 친구 목록 조회에 동의한 사용자들의 ID 조회
     * 이유: 카카오 친구 동기화가 가능한 사용자들을 필터링하기 위해
     */
    @Query("SELECT c.userId FROM UserConsent c WHERE c.userId IN :userIds AND c.friendsConsent = true")
    List<Long> findUserIdsWithFriendsConsentFromList(@Param("userIds") List<Long> userIds);
}
