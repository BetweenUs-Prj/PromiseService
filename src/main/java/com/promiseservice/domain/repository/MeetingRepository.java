package com.promiseservice.domain.repository;

import com.promiseservice.domain.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 약속 데이터 접근을 위한 리포지토리
 * 이유: 약속 엔티티의 CRUD 작업을 위한 데이터 접근 계층을 제공하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /**
     * 상태별로 약속 목록을 조회
     * 이유: 특정 상태의 약속들을 조회하기 위해
     *
     * @param status 약속 상태
     * @return 해당 상태의 약속 목록
     */
    List<Meeting> findByStatus(String status);
}
