package com.promiseservice.repository;

import com.promiseservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 사용자 레포지토리
 * 이유: 사용자 엔티티에 대한 데이터 접근 로직을 제공하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 여러 사용자 ID에 해당하는 사용자 수 조회
     * 이유: 약속 참가자 검증 시 모든 사용자가 존재하는지 확인하기 위해
     * 
     * @param userIds 사용자 ID 목록
     * @return 존재하는 사용자 수
     */
    long countByIdIn(List<Long> userIds);

    /**
     * 이름으로 사용자 검색
     * 이유: 사용자 이름을 기반으로 검색 기능을 제공하기 위해
     * 
     * @param name 검색할 이름
     * @return 해당 이름을 포함하는 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);

    /**
     * 전화번호로 사용자 조회
     * 이유: 전화번호를 통한 사용자 인증이나 조회 기능을 제공하기 위해
     * 
     * @param phoneNumber 전화번호
     * @return 해당 전화번호를 가진 사용자
     */
    User findByPhoneNumber(String phoneNumber);

    /**
     * 위치 기반 사용자 검색
     * 이유: 특정 지역의 사용자들을 조회하기 위해
     * 
     * @param location 위치
     * @return 해당 위치의 사용자 목록
     */
    List<User> findByLocationContaining(String location);
}