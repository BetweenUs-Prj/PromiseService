package com.promiseservice.controller;

import com.promiseservice.dto.UserDto;
import com.promiseservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 정보 관련 REST API 컨트롤러
 * 이유: UserService와 연동하여 사용자 정보 조회 및 친구 관련 기능을 제공하기 위해
 * 실제 사용자 데이터는 UserService(포트 8081)에서 관리되며, 이 컨트롤러는 프록시 역할
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 정보 조회 (UserService API 프록시)
     * 이유: PromiseService에서 필요한 사용자 정보를 UserService로부터 조회하기 위해
     * 
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        log.info("사용자 정보 조회 요청 - ID: {}", userId);
        
        try {
            UserDto user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패 - ID: {}, 에러: {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * 친구 목록 조회 (UserService API 프록시)
     * 이유: 약속 생성 시 친구 목록을 조회하여 참여자 초대 기능을 지원하기 위해
     * 실제 친구 관리는 UserService에서 처리하고, 이 API는 프록시 역할만 수행
     * 
     * GET /api/users/{userId}/friends
     */
    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<UserDto>> getFriendsByUserId(@PathVariable Long userId) {
        log.info("친구 목록 조회 요청 - 사용자 ID: {} (UserService API 호출)", userId);
        
        try {
            List<UserDto> friends = userService.getFriendsByUserId(userId);
            log.info("친구 목록 조회 성공 - 사용자 ID: {}, 친구 수: {}명", userId, friends.size());
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            log.error("친구 목록 조회 실패 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
            throw e;
        }
    }


    /**
     * 사용자 존재 여부 확인 (UserService API 연동)
     * 이유: 약속 참여자 초대 시 유효한 사용자인지 확인하기 위해
     * 로컬 캐시 우선 확인 후 UserService API 호출하여 최신 상태 확인
     * 
     * GET /api/users/{userId}/exists
     */
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable Long userId) {
        log.info("사용자 존재 여부 확인 요청 - ID: {} (UserService API 연동)", userId);
        
        try {
            boolean exists = userService.existsUser(userId);
            log.info("사용자 존재 여부 확인 결과 - ID: {}, 존재: {}", userId, exists);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            log.error("사용자 존재 여부 확인 실패 - ID: {}, 에러: {}", userId, e.getMessage());
            throw e;
        }
    }
}
