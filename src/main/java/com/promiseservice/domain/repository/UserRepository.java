package com.promiseservice.domain.repository;

import com.promiseservice.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 정보 데이터 접근 레포지토리
 * 이유: UserService에서 관리하는 사용자 정보를 PromiseService에서 참조하기 위해 필요
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * provider_id로 사용자 조회
     * 이유: 외부 인증 서비스(카카오 등)의 ID로 사용자를 찾기 위해
     */
    Optional<User> findByProviderId(String providerId);

    /**
     * 사용자명으로 사용자 조회
     * 이유: 사용자 검색 기능에서 이름으로 찾기 위해
     */
    List<User> findByNameContaining(String name);

    /**
     * 사용자 존재 여부 확인
     * 이유: 약속 참여자 초대 시 유효한 사용자인지 빠르게 확인하기 위해
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}




