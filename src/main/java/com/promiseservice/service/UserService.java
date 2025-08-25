package com.promiseservice.service;

import com.promiseservice.dto.UserDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 사용자 정보 관리 서비스
 * 이유: 약속 서비스에서 사용자 정보 확인과 유효성 검증을 위해 필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class        UserService {

    private final RestTemplate restTemplate;


    @Value("${userservice.base-url}")
    private String userServiceBaseUrl;

    @Value("${userservice.api.users}")
    private String usersApiPath;

    @Value("${userservice.api.profiles}")
    private String profilesApiPath;

    /**
     * 사용자 정보 조회
     */
    public UserDto getUserById(Long userId) {
        try {
            String url = userServiceBaseUrl + usersApiPath + "/" + userId;
            UserDto user = restTemplate.getForObject(url, UserDto.class);
            log.info("사용자 정보 조회 성공 - ID: {}", userId);
            return user;
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패 - ID: {}, 에러: {}", userId, e.getMessage());
            throw new RuntimeException("사용자 정보를 가져올 수 없습니다: " + userId);
        }
    }



    /**
     * 사용자 존재 여부 확인 (UserService API 호출)
     * 이유: 약속 생성 및 참여자 초대 시 유효한 사용자인지 확인하기 위해
     * 포트 8081의 UserService API를 호출하여 실제 사용자 존재 여부를 확인
     */
    public boolean existsUser(Long userId) {
        try {
            // UserService API 호출하여 사용자 존재 여부 확인
            // 이유: 사용자 데이터는 UserService에서만 관리하므로 해당 서비스를 통해 확인
            String url = userServiceBaseUrl + usersApiPath + "/" + userId + "/exists";
            Boolean exists = restTemplate.getForObject(url, Boolean.class);
            
            if (exists != null && exists) {
                log.info("UserService에서 사용자 확인됨 - ID: {}", userId);
                return true;
            } else {
                log.warn("UserService에서 사용자를 찾을 수 없음 - ID: {}", userId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("UserService API 호출 실패 - ID: {}, 에러: {}", userId, e.getMessage());
            
            // API 호출 실패 시 안전하게 false 반환
            // 이유: 사용자 존재를 확인할 수 없으면 보안상 존재하지 않는 것으로 처리
            log.warn("UserService 연결 실패로 인해 사용자 존재하지 않음으로 처리 - ID: {}", userId);
            return false;
        }
    }

    /**
     * 친구 목록 조회 (UserService API 호출)
     * 이유: 친구 관련 기능은 UserService에서 관리하므로 외부 API를 통해 조회
     * 
     * @param userId 사용자 ID
     * @return 친구 목록
     */
    public List<UserDto> getFriendsByUserId(Long userId) {
        try {
            String url = userServiceBaseUrl + usersApiPath + "/" + userId + "/friends";
            
            // UserService에서 친구 목록 조회
            @SuppressWarnings("unchecked")
            List<UserDto> friends = restTemplate.getForObject(url, List.class);
            
            log.info("UserService에서 친구 목록 조회 성공 - 사용자 ID: {}, 친구 수: {}", 
                    userId, friends != null ? friends.size() : 0);
            
            return friends != null ? friends : new java.util.ArrayList<>();
            
        } catch (Exception e) {
            log.error("UserService 친구 목록 조회 실패 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
            
            // API 호출 실패 시 빈 목록 반환
            // 이유: 친구 목록 조회 실패가 전체 서비스를 중단시키지 않도록 함
            log.warn("친구 목록 조회 실패로 빈 목록 반환 - 사용자 ID: {}", userId);
            return new java.util.ArrayList<>();
        }
    }
}

