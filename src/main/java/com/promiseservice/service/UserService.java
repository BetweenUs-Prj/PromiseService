package com.promiseservice.service;

import com.promiseservice.model.entity.User;
import com.promiseservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 서비스
 * 이유: 사용자 관련 비즈니스 로직을 처리하고 다른 서비스에서 사용자 정보를 조회할 수 있도록 하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 ID로 사용자 정보 조회
     * 이유: 다른 서비스에서 사용자의 기본 정보를 조회할 수 있도록 하기 위해
     * 
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    public Optional<User> getUserById(Long userId) {
        log.debug("사용자 조회 - ID: {}", userId);
        return userRepository.findById(userId);
    }

    /**
     * 사용자 ID로 사용자 이름 조회
     * 이유: 약속 참가자 목록에서 사용자 이름을 표시하기 위해
     * 
     * @param userId 사용자 ID
     * @return 사용자 이름 (없으면 기본값)
     */
    public String getUserName(Long userId) {
        log.debug("사용자 이름 조회 - ID: {}", userId);
        return userRepository.findById(userId)
                .map(User::getName)
                .orElse("사용자" + userId);
    }

    /**
     * 여러 사용자 ID로 사용자 이름 목록 조회
     * 이유: 약속 참가자들의 이름을 한번에 조회하기 위해
     * 
     * @param userIds 사용자 ID 목록
     * @return 사용자 정보 목록
     */
    public List<User> getUsersByIds(List<Long> userIds) {
        log.debug("사용자 목록 조회 - IDs: {}", userIds);
        return userRepository.findAllById(userIds);
    }

    /**
     * 사용자 존재 여부 확인
     * 이유: 약속 생성 시 참가자들이 유효한 사용자인지 검증하기 위해
     * 
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    public boolean existsById(Long userId) {
        log.debug("사용자 존재 확인 - ID: {}", userId);
        return userRepository.existsById(userId);
    }

    /**
     * 여러 사용자 ID들의 존재 여부 확인
     * 이유: 약속 초대 시 모든 참가자가 유효한 사용자인지 검증하기 위해
     * 
     * @param userIds 사용자 ID 목록
     * @return 모든 사용자가 존재하면 true
     */
    public boolean allUsersExist(List<Long> userIds) {
        log.debug("사용자 목록 존재 확인 - IDs: {}", userIds);
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        
        long existingCount = userRepository.countByIdIn(userIds);
        return existingCount == userIds.size();
    }
}