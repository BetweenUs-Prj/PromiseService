package com.promiseservice.repository;

import com.promiseservice.model.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 레포지토리
 * 이유: 친구 관계 데이터에 대한 CRUD 작업과 친구 목록 조회 기능을 제공하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * 사용자의 친구 목록 조회 (수락된 친구만)
     * 이유: 특정 사용자의 활성 친구 관계만 조회하기 위해
     */
    @Query("SELECT f FROM Friendship f WHERE f.userId = :userId AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendsByUserId(@Param("userId") Long userId);

    /**
     * 두 사용자 간 친구 관계 조회
     * 이유: 특정 두 사용자 간의 친구 관계 존재 여부를 확인하기 위해
     */
    @Query("SELECT f FROM Friendship f WHERE (f.userId = :userId1 AND f.friendId = :userId2) OR (f.userId = :userId2 AND f.friendId = :userId1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 사용자가 친구인지 확인
     * 이유: 두 사용자가 수락된 친구 관계인지 빠르게 확인하기 위해
     */
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
           "((f.userId = :userId1 AND f.friendId = :userId2) OR " +
           "(f.userId = :userId2 AND f.friendId = :userId1)) AND " +
           "f.status = 'ACCEPTED'")
    boolean areFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 사용자의 모든 친구 관계 조회 (상태 무관)
     * 이유: 사용자의 모든 친구 관계를 상태별로 관리하기 위해
     */
    @Query("SELECT f FROM Friendship f WHERE f.userId = :userId OR f.friendId = :userId")
    List<Friendship> findAllFriendshipsByUserId(@Param("userId") Long userId);

    /**
     * 특정 상태의 친구 관계 조회
     * 이유: 대기중, 차단된 친구 등 특정 상태의 관계를 조회하기 위해
     */
    @Query("SELECT f FROM Friendship f WHERE f.userId = :userId AND f.status = :status")
    List<Friendship> findFriendsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}