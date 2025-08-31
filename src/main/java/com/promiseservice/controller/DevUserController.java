package com.promiseservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 개발용 사용자 데이터 생성 컨트롤러
 * 이유: 테스트를 위한 사용자 데이터를 빠르게 생성하여 자동 알림 시스템 테스트 지원
 */
@Slf4j
@RestController
@RequestMapping("/api/dev/users")
@RequiredArgsConstructor
public class DevUserController {

    /**
     * 테스트용 사용자 데이터 생성
     * 이유: 약속 생성 및 알림 전송 테스트를 위한 기본 사용자 데이터 제공
     * 
     * @return 생성된 사용자 정보
     */
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> createTestUsers() {
        
        log.info("테스트용 사용자 데이터 생성 시작");
        
        // TODO: 실제 UserService를 통해 사용자 생성
        // 이유: 현재는 Mock 데이터로 빠른 테스트 지원, 추후 실제 사용자 생성 로직 연결
        
        Map<String, Object> result = Map.of(
            "message", "테스트용 사용자 데이터가 생성되었습니다",
            "users", Map.of(
                "1", Map.of("id", 1, "name", "테스트 사용자 1", "kakaoId", "test_user_1"),
                "2", Map.of("id", 2, "name", "테스트 사용자 2", "kakaoId", "test_user_2"),
                "3", Map.of("id", 3, "name", "테스트 사용자 3", "kakaoId", "test_user_3")
            ),
            "status", "created",
            "note", "이제 /api/meetings로 약속 생성 시 자동 알림 테스트 가능"
        );
        
        log.info("테스트용 사용자 데이터 생성 완료");
        
        return ResponseEntity.ok(result);
    }
}
