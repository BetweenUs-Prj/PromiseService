-- =====================================================
-- UserService Database Schema
-- Database: beetween_us_db
-- 엔티티와 정확히 일치하는 DDL
-- =====================================================

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS `beetween_us_db`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `beetween_us_db`;

-- ================================
-- 1. 장소 테이블
-- ================================
CREATE TABLE places (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                        name VARCHAR(255) NOT NULL COMMENT '장소 이름',
                        address VARCHAR(500) NULL COMMENT '장소 주소',
                        latitude DECIMAL(10, 8) NULL COMMENT '위도',
                        longitude DECIMAL(11, 8) NULL COMMENT '경도',
                        category VARCHAR(50) NULL COMMENT '장소 카테고리',
                        is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
                        created_at DATETIME NOT NULL COMMENT '생성 시간',
                        updated_at DATETIME NULL COMMENT '수정 시간'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_places_name ON places(name);
CREATE INDEX idx_places_category ON places(category);
CREATE INDEX idx_places_is_active ON places(is_active);


-- ================================
-- 2. 친구 관계 테이블
-- ================================
CREATE TABLE friendship (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                            user_id BIGINT NOT NULL COMMENT '사용자 ID',
                            friend_id BIGINT NOT NULL COMMENT '친구 사용자 ID',
                            status VARCHAR(50) NOT NULL COMMENT '친구 관계 상태 (PENDING, ACCEPTED, BLOCKED)',
                            created_at DATETIME NOT NULL COMMENT '친구 관계 생성 시간',
                            updated_at DATETIME NULL COMMENT '친구 관계 수정 시간',
                            CONSTRAINT unique_friendship UNIQUE (user_id, friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_friendship_user_id ON friendship(user_id);
CREATE INDEX idx_friendship_friend_id ON friendship(friend_id);

-- ================================
-- 3. 카카오 친구 매핑 테이블
-- ================================
CREATE TABLE kakao_friend_map (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                                  user_id BIGINT NOT NULL COMMENT '카카오 친구 목록을 조회한 사용자',
                                  friend_user_id BIGINT NOT NULL COMMENT '친구 사용자 ID',
                                  kakao_uuid VARCHAR(100) NOT NULL COMMENT '카카오 친구 UUID',
                                  kakao_nickname VARCHAR(100) NULL COMMENT '카카오 닉네임',
                                  kakao_profile_image TEXT NULL COMMENT '카카오 프로필 이미지 URL',
                                  is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '매핑 활성화 여부',
                                  last_synced_at DATETIME NULL COMMENT '마지막 동기화 시간',
                                  created_at DATETIME NOT NULL COMMENT '레코드 생성 시간',
                                  updated_at DATETIME NULL COMMENT '레코드 수정 시간',
                                  CONSTRAINT unique_kakao_friend_mapping UNIQUE (user_id, friend_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_kakao_friend_user_id ON kakao_friend_map(user_id);
CREATE INDEX idx_kakao_friend_friend_user_id ON kakao_friend_map(friend_user_id);
CREATE INDEX idx_kakao_friend_uuid ON kakao_friend_map(kakao_uuid);

-- ================================
-- 4. 약속(미팅) 테이블
-- ================================
CREATE TABLE meeting (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '약속 고유 식별자',
                         title VARCHAR(255) NOT NULL COMMENT '약속 제목',
                         description TEXT NULL COMMENT '약속 상세 설명',
                         meeting_time DATETIME NOT NULL COMMENT '약속 예정 시간',
                         max_participants INT DEFAULT 10 COMMENT '최대 참여 인원 수',
                         status VARCHAR(50) NOT NULL COMMENT '약속 진행 상태',
                         host_id BIGINT NOT NULL COMMENT '방장 사용자 ID',
                         location_name VARCHAR(500) NULL COMMENT '약속 장소명',
                         location_address VARCHAR(500) NULL COMMENT '약속 장소 상세 주소',
                         location_coordinates TEXT NULL COMMENT '좌표 JSON',
                         place_id BIGINT NULL COMMENT '장소 ID',
                         created_at DATETIME NOT NULL COMMENT '약속 생성 시각',
                         updated_at DATETIME NULL COMMENT '약속 수정 시각',
                         CONSTRAINT fk_meeting_place FOREIGN KEY (place_id) REFERENCES places(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_meeting_time ON meeting(meeting_time);
CREATE INDEX idx_status ON meeting(status);
CREATE INDEX idx_host_id ON meeting(host_id);
CREATE INDEX idx_place_id ON meeting(place_id);

-- ================================
-- 5. 약속 히스토리 테이블
-- ================================
CREATE TABLE meeting_history (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                                 meeting_id BIGINT NOT NULL COMMENT '약속 ID',
                                 user_id BIGINT NOT NULL COMMENT '행동 주체 사용자 ID',
                                 action VARCHAR(50) NOT NULL COMMENT '행동 타입',
                                 created_at DATETIME NOT NULL COMMENT '행동 시각',
                                 CONSTRAINT fk_meeting_history_meeting FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_history_meeting_id ON meeting_history(meeting_id);
CREATE INDEX idx_history_user_id ON meeting_history(user_id);
CREATE INDEX idx_history_timestamp ON meeting_history(created_at);

-- ================================
-- 6. 약속 참여자 테이블
-- ================================
CREATE TABLE meeting_participant (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                                     meeting_id BIGINT NOT NULL COMMENT '약속 ID',
                                     user_id BIGINT NOT NULL COMMENT '참여자 사용자 ID',
                                     response VARCHAR(50) NOT NULL COMMENT '참여 응답 상태',
                                     joined_at DATETIME NULL COMMENT '참여 확정 시각',
                                     invited_at DATETIME NOT NULL COMMENT '초대 발송 시각',
                                     CONSTRAINT unique_meeting_user UNIQUE (meeting_id, user_id),
                                     CONSTRAINT fk_participant_meeting FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_participant_meeting_id ON meeting_participant(meeting_id);
CREATE INDEX idx_participant_user_id ON meeting_participant(user_id);
CREATE INDEX idx_participant_response ON meeting_participant(response);

-- ================================
-- 7. 알림 로그 테이블
-- ================================
CREATE TABLE notification_log (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '로그 PK',
                                  meeting_id BIGINT NOT NULL COMMENT '약속 ID',
                                  user_id BIGINT NOT NULL COMMENT '알림 사용자 ID',
                                  channel VARCHAR(32) NOT NULL COMMENT '알림 채널',
                                  payload_json TEXT NOT NULL COMMENT '전송 페이로드',
                                  http_status INT NOT NULL COMMENT 'HTTP 상태',
                                  result_code INT NULL COMMENT '서비스 결과 코드',
                                  error_json TEXT NULL COMMENT '에러 JSON',
                                  trace_id VARCHAR(64) NOT NULL COMMENT '추적 ID',
                                  created_at DATETIME NOT NULL COMMENT '생성 시각'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notification_meeting_id ON notification_log(meeting_id);
CREATE INDEX idx_notification_user_id ON notification_log(user_id);
CREATE INDEX idx_notification_trace_id ON notification_log(trace_id);


-- ================================
-- 8. OAuth 사용자 신원 테이블
-- ================================
CREATE TABLE user_identity (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                               user_id BIGINT NOT NULL COMMENT '내부 사용자 ID',
                               provider VARCHAR(20) NOT NULL COMMENT '제공자',
                               provider_user_id VARCHAR(100) NOT NULL COMMENT '제공자 사용자 ID',
                               nickname VARCHAR(100) NULL COMMENT '닉네임',
                               profile_image_url VARCHAR(500) NULL COMMENT '프로필 URL',
                               access_token TEXT NULL COMMENT '액세스 토큰',
                               refresh_token TEXT NULL COMMENT '리프레시 토큰',
                               token_expires_at DATETIME NULL COMMENT '토큰 만료',
                               created_at DATETIME NOT NULL COMMENT '생성 시각',
                               updated_at DATETIME NOT NULL COMMENT '수정 시각',
                               CONSTRAINT uq_provider_user UNIQUE (provider, provider_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_identity_user_id ON user_identity(user_id);
CREATE INDEX idx_user_identity_provider ON user_identity(provider);

-- ================================
-- 9. 사용자 프로필 테이블
-- ================================
CREATE TABLE user_profiles (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                               name VARCHAR(100) NOT NULL COMMENT '사용자 이름',
                               bio TEXT NULL COMMENT '자기소개',
                               location VARCHAR(255) NULL COMMENT '거주 지역',
                               website VARCHAR(255) NULL COMMENT '개인 웹사이트',
                               phone_number VARCHAR(50) NULL COMMENT '전화번호',
                               avatar_url VARCHAR(500) NULL COMMENT '프로필 이미지',
                               preferred_transport VARCHAR(50) NULL COMMENT '선호 교통수단',
                               created_at DATETIME NOT NULL COMMENT '생성 시각',
                               updated_at DATETIME NOT NULL COMMENT '수정 시각'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
