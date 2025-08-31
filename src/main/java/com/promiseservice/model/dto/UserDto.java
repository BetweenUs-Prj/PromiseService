package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
    // 기본 사용자 정보
    private Long id;
    private String email;
    private String name;
    private String provider;
    private String providerId;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 프로필 정보
    private String bio;
    private String location;
    private String website;
    private String phoneNumber;
    private String avatarUrl;
    private String preferredTransport;
}

