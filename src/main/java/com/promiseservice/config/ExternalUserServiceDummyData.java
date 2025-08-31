package com.promiseservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 외부 UserService(포트 8081)에서 사용할 더미 사용자 데이터 정보
 * 이유: Talend API 테스트 시 참조할 수 있는 더미 사용자 정보를 제공하기 위해
 * 
 * 주의: 이 클래스는 실제로 데이터를 생성하지 않고, 
 * 외부 UserService에서 생성해야 할 데이터 구조를 참고용으로 제공합니다.
 */
@Slf4j
@Component
public class ExternalUserServiceDummyData {

    /**
     * 외부 UserService에서 생성해야 할 더미 사용자 데이터 구조
     * 이유: API 테스트 시 일관된 사용자 ID와 정보를 사용하기 위해
     * 
     * 외부 UserService(포트 8081)에서 다음과 같은 사용자 데이터를 생성해야 합니다:
     */
    public void printDummyUserDataForExternalService() {
        log.info("=== 외부 UserService(포트 8081)에서 생성할 더미 사용자 데이터 ===");
        
        String[] dummyUsers = {
            // 사용자 ID: 1
            "{\n" +
            "  \"id\": 1,\n" +
            "  \"name\": \"김철수\",\n" +
            "  \"email\": \"kim.cs@example.com\",\n" +
            "  \"phoneNumber\": \"010-1234-5678\",\n" +
            "  \"nickname\": \"철수\",\n" +
            "  \"profileImageUrl\": \"https://example.com/profiles/1.jpg\",\n" +
            "  \"friends\": [2, 3, 4, 5]\n" +
            "}",
            
            // 사용자 ID: 2
            "{\n" +
            "  \"id\": 2,\n" +
            "  \"name\": \"이영희\",\n" +
            "  \"email\": \"lee.yh@example.com\",\n" +
            "  \"phoneNumber\": \"010-2345-6789\",\n" +
            "  \"nickname\": \"영희\",\n" +
            "  \"profileImageUrl\": \"https://example.com/profiles/2.jpg\",\n" +
            "  \"friends\": [1, 3, 6, 7]\n" +
            "}",
            
            // 사용자 ID: 3
            "{\n" +
            "  \"id\": 3,\n" +
            "  \"name\": \"박민수\",\n" +
            "  \"email\": \"park.ms@example.com\",\n" +
            "  \"phoneNumber\": \"010-3456-7890\",\n" +
            "  \"nickname\": \"민수\",\n" +
            "  \"profileImageUrl\": \"https://example.com/profiles/3.jpg\",\n" +
            "  \"friends\": [1, 2, 4, 8]\n" +
            "}",
            
            // 사용자 ID: 4
            "{\n" +
            "  \"id\": 4,\n" +
            "  \"name\": \"최지은\",\n" +
            "  \"email\": \"choi.je@example.com\",\n" +
            "  \"phoneNumber\": \"010-4567-8901\",\n" +
            "  \"nickname\": \"지은\",\n" +
            "  \"profileImageUrl\": \"https://example.com/profiles/4.jpg\",\n" +
            "  \"friends\": [1, 3, 5, 6]\n" +
            "}",
            
            // 사용자 ID: 5
            "{\n" +
            "  \"id\": 5,\n" +
            "  \"name\": \"정준호\",\n" +
            "  \"email\": \"jung.jh@example.com\",\n" +
            "  \"phoneNumber\": \"010-5678-9012\",\n" +
            "  \"nickname\": \"준호\",\n" +
            "  \"profileImageUrl\": \"https://example.com/profiles/5.jpg\",\n" +
            "  \"friends\": [1, 4, 6, 7]\n" +
            "}",
            
            // 사용자 ID: 6
            "{\n" +
            "  \"id\": 6,\n" +
            "  \"name\": \"한소영\",\n" +
            "  \"email\": \"han.sy@example.com\",\n" +
            "  \"phoneNumber\": \"010-6789-0123\",\n" +
            "  \"nickname\": \"소영\",\n" +
            "  \"profileImageUrl\": \"https://example.com/profiles/6.jpg\",\n" +
            "  \"friends\": [2, 4, 5, 8]\n" +
            "}",
            
            // 사용자 ID: 7
            "{\n" +
            "  \"id\": 7,\n" +
            "  \"name\": \"윤대현\",\n" +
            "  \"email\": \"yoon.dh@example.com\",\n" +
            "  \"phoneNumber\": \"010-7890-1234\",\n" +
            "  \"nickname\": \"대현\",\n" +
            "  \"profileImageUrl\": \"https://example.com/profiles/7.jpg\",\n" +
            "  \"friends\": [2, 3, 5, 8]\n" +
            "}",
            
            // 사용자 ID: 8
            "{\n" +
            "  \"id\": 8,\n" +
            "  \"name\": \"강미래\",\n" +
            "  \"email\": \"kang.mr@example.com\",\n" +
            "  \"phoneNumber\": \"010-8901-2345\",\n" +
            "  \"nickname\": \"미래\",\n" +
            "  \"profileImageUrl\": \"https://example.com/profiles/8.jpg\",\n" +
            "  \"friends\": [3, 6, 7]\n" +
            "}"
        };
        
        log.info("총 {}명의 사용자 데이터 구조를 출력했습니다.", dummyUsers.length);
        log.info("이 데이터는 외부 UserService(포트 8081)에서 생성해야 합니다.");
        
        // 필요한 API 엔드포인트 정보도 출력
        printRequiredUserServiceAPIs();
    }
    
    /**
     * 외부 UserService에서 구현해야 할 API 엔드포인트 정보
     * 이유: PromiseService가 호출하는 UserService API의 명세를 명확히 하기 위해
     */
    private void printRequiredUserServiceAPIs() {
        log.info("=== 외부 UserService(포트 8081)에서 구현해야 할 API 엔드포인트 ===");
        
        String[] requiredAPIs = {
            "GET http://localhost:8081/api/users/{userId} - 특정 사용자 정보 조회",
            "GET http://localhost:8081/api/users/{userId}/exists - 사용자 존재 여부 확인 (Boolean 반환)",
            "GET http://localhost:8081/api/users/{userId}/friends - 사용자의 친구 목록 조회"
        };
        
        for (String api : requiredAPIs) {
            log.info("- {}", api);
        }
        
        log.info("=== UserDto 응답 형식 ===");
        log.info("GET /api/users/{userId} 응답 형식:");
        log.info("{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"김철수\",\n" +
                "  \"email\": \"kim.cs@example.com\",\n" +
                "  \"phoneNumber\": \"010-1234-5678\",\n" +
                "  \"nickname\": \"철수\",\n" +
                "  \"profileImageUrl\": \"https://example.com/profiles/1.jpg\"\n" +
                "}");
        
        log.info("GET /api/users/{userId}/friends 응답 형식:");
        log.info("[\n" +
                "  {\n" +
                "    \"id\": 2,\n" +
                "    \"name\": \"이영희\",\n" +
                "    \"email\": \"lee.yh@example.com\",\n" +
                "    \"phoneNumber\": \"010-2345-6789\",\n" +
                "    \"nickname\": \"영희\",\n" +
                "    \"profileImageUrl\": \"https://example.com/profiles/2.jpg\"\n" +
                "  }\n" +
                "]");
    }
    
    /**
     * Talend 테스트용 데이터 구조 출력
     * 이유: Talend에서 API 테스트 시 사용할 수 있는 데이터 예시 제공
     */
    public void printTalendTestData() {
        log.info("=== Talend API 테스트용 데이터 ===");
        
        log.info("# 존재하는 사용자 ID: 1, 2, 3, 4, 5, 6, 7, 8");
        log.info("# 각 사용자별 전화번호:");
        log.info("- 사용자 1: 010-1234-5678");
        log.info("- 사용자 2: 010-2345-6789");
        log.info("- 사용자 3: 010-3456-7890");
        log.info("- 사용자 4: 010-4567-8901");
        log.info("- 사용자 5: 010-5678-9012");
        log.info("- 사용자 6: 010-6789-0123");
        log.info("- 사용자 7: 010-7890-1234");
        log.info("- 사용자 8: 010-8901-2345");
        
        log.info("# 약속 ID: 1, 2, 3, 4, 5");
        log.info("- 약속 1: 주말 맛집 탐방 (WAITING)");
        log.info("- 약속 2: 영화 관람 (CONFIRMED)");
        log.info("- 약속 3: 등산 모임 (WAITING)");
        log.info("- 약속 4: 카페 모임 (COMPLETED)");
        log.info("- 약속 5: 보드게임 카페 (CANCELLED)");
    }
}















