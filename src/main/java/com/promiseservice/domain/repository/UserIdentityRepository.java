package com.promiseservice.domain.repository;

import com.promiseservice.domain.entity.UserIdentity;
import com.promiseservice.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OAuth 사용자 신원 정보 Repository
 * 이유: 카카오 등 OAuth 제공자의 사용자 ID로 내부 사용자 ID를 조회하기 위해
 * OAuth 로그인 시 사용자 인증 및 식별에 사용
 */
@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
    
    /**
     * 제공자와 제공자 사용자 ID로 사용자 신원 정보 조회
     * 이유: OAuth 로그인 시 제공자별 사용자 ID로 내부 사용자 ID를 찾기 위해
     * 
     * @param provider OAuth 제공자 (예: KAKAO, GOOGLE)
     * @param providerUserId OAuth 제공자의 사용자 ID
     * @return 사용자 신원 정보 (Optional)
     */
    Optional<UserIdentity> findByProviderAndProviderUserId(Provider provider, String providerUserId);
    
    /**
     * 제공자 사용자 ID로 사용자 신원 정보 조회 (제공자 무관)
     * 이유: 다양한 OAuth 제공자에서 동일한 사용자 ID를 사용하는 경우 대응
     * 
     * @param providerUserId OAuth 제공자의 사용자 ID
     * @return 사용자 신원 정보 (Optional)
     */
    Optional<UserIdentity> findByProviderUserId(String providerUserId);
    
    /**
     * 내부 사용자 ID로 사용자 신원 정보 조회
     * 이유: 사용자별로 등록된 OAuth 계정 정보를 조회하기 위해
     * 
     * @param userId 내부 사용자 ID
     * @return 사용자 신원 정보 (Optional)
     */
    Optional<UserIdentity> findByUserId(Long userId);
}
