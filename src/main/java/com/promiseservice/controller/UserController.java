package com.promiseservice.controller;

import com.promiseservice.dto.UserDto;
import com.promiseservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 정보 조회
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
     * 친구 목록 조회
     * GET /api/users/{userId}/friends
     */
    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<UserDto>> getFriendsByUserId(@PathVariable Long userId) {
        log.info("친구 목록 조회 요청 - 사용자 ID: {}", userId);
        
        try {
            List<UserDto> friends = userService.getFriendsByUserId(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            log.error("친구 목록 조회 실패 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * 사용자 존재 여부 확인
     * GET /api/users/{userId}/exists
     */
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Object> checkUserExists(@PathVariable Long userId) {
        log.info("사용자 존재 여부 확인 요청 - ID: {}", userId);
        
        try {
            boolean exists = userService.existsUser(userId);
            return ResponseEntity.ok(new Object() {
                public final Long userIdValue = userId;
                public final boolean existsValue = exists;
            });
        } catch (Exception e) {
            log.error("사용자 존재 여부 확인 실패 - ID: {}, 에러: {}", userId, e.getMessage());
            throw e;
        }
    }
}
