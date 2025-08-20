package com.promiseservice.service;

import com.promiseservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

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
     * 친구 목록 조회
     */
    public List<UserDto> getFriendsByUserId(Long userId) {
        try {
            String url = userServiceBaseUrl + usersApiPath + "/" + userId + "/friends";
            List<UserDto> friends = restTemplate.getForObject(url, List.class);
            log.info("친구 목록 조회 성공 - 사용자 ID: {}, 친구 수: {}", userId, friends != null ? friends.size() : 0);
            return friends;
        } catch (Exception e) {
            log.error("친구 목록 조회 실패 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
            throw new RuntimeException("친구 목록을 가져올 수 없습니다: " + userId);
        }
    }

    /**
     * 사용자 존재 여부 확인
     */
    public boolean existsUser(Long userId) {
        try {
            getUserById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

