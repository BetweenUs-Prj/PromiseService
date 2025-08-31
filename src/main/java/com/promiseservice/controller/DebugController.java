package com.promiseservice.controller;

import com.promiseservice.domain.entity.UserIdentity;
import com.promiseservice.domain.repository.UserIdentityRepository;
import com.promiseservice.enums.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 디버깅용 컨트롤러
 * 이유: 헤더 전달 문제를 빠르게 진단하고 원인을 파악하기 위해
 * 서블릿 레벨과 Spring 레벨에서 헤더를 직접 확인하여 문제점을 정확히 파악
 */
@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserIdentityRepository userIdentityRepository;

    /**
     * 헤더 에코 엔드포인트
     * 이유: 서버가 실제로 어떤 헤더를 받는지 정확히 확인하기 위해
     * 
     * @param hh Spring의 HttpHeaders
     * @param req 서블릿 레벨의 HttpServletRequest
     * @return 헤더 정보와 Kakao ID 확인 결과
     */
    @GetMapping("/echo-headers")
    public Map<String, Object> echo(@RequestHeader HttpHeaders hh,
                                   HttpServletRequest req) {
        
        log.info("=== 🔍 헤더 에코 요청 감지 ===");
        
        // 서블릿 레벨에서 모든 헤더 직접 확인
        var all = new LinkedHashMap<String, String>();
        Collections.list(req.getHeaderNames())
            .forEach(n -> all.put(n, req.getHeader(n)));
        
        // Kakao ID 헤더 직접 확인
        String kakaoId = req.getHeader("X-Kakao-Id");
        
        // Spring HttpHeaders에서도 확인
        String springKakaoId = hh.getFirst("X-Kakao-Id");
        
        log.info("서블릿 레벨 X-Kakao-Id: {}", kakaoId);
        log.info("Spring HttpHeaders X-Kakao-Id: {}", springKakaoId);
        log.info("전체 헤더: {}", all);
        
        Map<String, Object> result = Map.of(
            "seenKakaoId", kakaoId,
            "springHasXKakaoId", springKakaoId,
            "headers", all,
            "timestamp", System.currentTimeMillis()
        );
        
        log.info("=== 🔍 헤더 에코 응답: {} ===", result);
        return result;
    }
    
    /**
     * Kakao ID와 User ID를 링크하는 디버그 엔드포인트
     * 이유: 로컬/테스트 환경에서 빠르게 Kakao ID와 User ID를 매핑하기 위해
     * 
     * @param userId 내부 사용자 ID
     * @param kakaoId 카카오 사용자 ID
     * @return 링크 결과
     */
    @PostMapping("/link-provider")
    public ResponseEntity<Map<String, Object>> linkProvider(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Kakao-Id") String kakaoId) {
        
        log.info("=== 🔗 Kakao ID 링크 요청 - userId: {}, kakaoId: {} ===", userId, kakaoId);
        
        try {
            // UserIdentity 생성 및 저장 (접두사 없이)
            UserIdentity userIdentity = UserIdentity.of(userId, Provider.KAKAO, kakaoId.trim());
            UserIdentity saved = userIdentityRepository.save(userIdentity);
            
            log.info("Kakao ID 링크 성공 - userId: {}, kakaoId: {}, saved: {}", userId, kakaoId, saved.getId());
            
            Map<String, Object> result = Map.of(
                "message", "Kakao ID 링크 성공",
                "userId", userId,
                "kakaoId", kakaoId,
                "provider", "KAKAO",
                "savedId", saved.getId(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Kakao ID 링크 실패 - userId: {}, kakaoId: {}, error: {}", 
                     userId, kakaoId, e.getMessage());
            
            Map<String, Object> error = Map.of(
                "message", "Kakao ID 링크 실패",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 사용자 목록 조회 엔드포인트
     * 이유: 현재 DB에 등록된 사용자들을 확인하여 초대 실패 원인을 파악하기 위해
     * 
     * @return 사용자 목록과 카카오 ID 매핑 정보
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers() {
        
        log.info("=== 👥 사용자 목록 조회 요청 ===");
        
        try {
            // 모든 UserIdentity 조회
            var allUserIdentities = userIdentityRepository.findAll();
            
            // 사용자별 카카오 ID 매핑 정보 구성
            var userMappings = allUserIdentities.stream()
                .filter(ui -> ui.getProvider() == Provider.KAKAO)
                .collect(LinkedHashMap::new, 
                    (map, ui) -> map.put(ui.getUserId().toString(), ui.getProviderUserId()),
                    LinkedHashMap::putAll);
            
            log.info("사용자 매핑 정보: {}", userMappings);
            
            Map<String, Object> result = Map.of(
                "totalUsers", allUserIdentities.size(),
                "kakaoMappings", userMappings,
                "allUserIdentities", allUserIdentities.stream()
                    .map(ui -> Map.of(
                        "id", ui.getId(),
                        "userId", ui.getUserId(),
                        "provider", ui.getProvider().name(),
                        "providerUserId", ui.getProviderUserId()
                    ))
                    .toList(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패 - error: {}", e.getMessage());
            
            Map<String, Object> error = Map.of(
                "message", "사용자 목록 조회 실패",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
