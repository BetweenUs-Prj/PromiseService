package com.promiseservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 친구 관계 관리 서비스 (Mock 구현)
 * 이유: 복잡한 친구 관계 기능 없이 기본적인 테스트를 위한 Mock 서비스를 제공하기 위해
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

    /**
     * 친구 요청 전송 (Mock)
     * 이유: 테스트 목적으로 항상 성공하는 Mock 응답을 제공하기 위해
     */
    @Transactional
    public String sendFriendRequest(Long requesterUserId, Long receiverUserId) {
        
        if (requesterUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("자기 자신에게는 친구 요청을 보낼 수 없습니다.");
        }

        log.info("Mock 친구 요청 전송: {} -> {}", requesterUserId, receiverUserId);
        return "친구 요청을 보냈습니다. (Mock)";
    }

    /**
     * 사용자의 친구 목록 조회 (Mock)
     * 이유: 테스트 목적으로 빈 목록을 반환하는 Mock 구현을 제공하기 위해
     */
    public List<String> getFriends(Long userId) {
        log.info("Mock 친구 목록 조회: {}", userId);
        return new ArrayList<>(); // 빈 목록 반환
    }

    /**
     * 두 사용자가 친구인지 확인 (Mock)
     * 이유: 테스트 목적으로 항상 true를 반환하는 Mock 구현을 제공하기 위해
     */
    public boolean areFriends(Long userId1, Long userId2) {
        log.info("Mock 친구 관계 확인: {} <-> {}", userId1, userId2);
        return true; // 항상 친구로 간주 (테스트 목적)
    }
}


