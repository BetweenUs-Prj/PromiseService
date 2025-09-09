-- =====================================================
-- Unified Schema for beetween_us_db (MySQL 8.0+/9.x)
-- Includes: users, friend_requests, friendship, places, meeting,
--           meeting_history, meeting_participant
-- =====================================================

-- DB 생성 및 선택
CREATE DATABASE IF NOT EXISTS `beetween_us_db`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `beetween_us_db`;

-- =====================================================
-- 1) 사용자 계정 (카카오 OAuth2 연동)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
                                     user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name          VARCHAR(255) NOT NULL,
                                     profile_image VARCHAR(500),
                                     provider_id   VARCHAR(255),
                                     created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     CONSTRAINT uk_users_provider_id UNIQUE (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='사용자 계정 정보를 저장 (카카오 OAuth2 연동)';

CREATE INDEX idx_users_name ON users (name);

-- =====================================================
-- 2) 친구 요청
-- =====================================================
CREATE TABLE IF NOT EXISTS friend_requests (
                                               id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               sender_id   BIGINT NOT NULL,
                                               receiver_id BIGINT NOT NULL,
                                               status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                               message     VARCHAR(500),
                                               created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                               CONSTRAINT fk_friend_requests_sender  FOREIGN KEY (sender_id)  REFERENCES users (user_id) ON DELETE CASCADE,
                                               CONSTRAINT fk_friend_requests_receiver FOREIGN KEY (receiver_id) REFERENCES users (user_id) ON DELETE CASCADE,
                                               CONSTRAINT uk_friend_requests_sender_receiver UNIQUE (sender_id, receiver_id),
                                               CONSTRAINT ck_friend_requests_not_self CHECK (sender_id <> receiver_id),
                                               CONSTRAINT ck_friend_requests_status   CHECK (status IN ('PENDING','ACCEPTED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='친구 요청 (요청/수락/거절)';

CREATE INDEX idx_friend_requests_sender_id   ON friend_requests (sender_id);
# CREATE INDEX idx_friend_requests_receiver_id ON friend_requests (receiver_id);
CREATE INDEX idx_friend_requests_status      ON friend_requests (status);
CREATE INDEX idx_friend_requests_created_at  ON friend_requests (created_at);

-- =====================================================
-- 3) 친구 관계 (단수명 friendship 로 통일)
-- =====================================================
CREATE TABLE IF NOT EXISTS friendship (
                                          id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                                          user_id    BIGINT NOT NULL COMMENT '사용자 ID',
                                          friend_id  BIGINT NOT NULL COMMENT '친구 사용자 ID',
                                          status     VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '친구 관계 상태',
                                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
                                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
                                          CONSTRAINT unique_friendship UNIQUE (user_id, friend_id),
                                          CONSTRAINT ck_friendships_not_self CHECK (user_id <> friend_id),
                                          CONSTRAINT ck_friendships_status CHECK (status IN ('ACTIVE','BLOCKED','INACTIVE')),
                                          CONSTRAINT fk_friendship_user   FOREIGN KEY (user_id)   REFERENCES users(user_id)   ON DELETE CASCADE,
                                          CONSTRAINT fk_friendship_friend FOREIGN KEY (friend_id) REFERENCES users(user_id)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='친구 관계 (목록/차단/비활성 포함)';

CREATE INDEX idx_friendship_user_id   ON friendship(user_id);
CREATE INDEX idx_friendship_friend_id ON friendship(friend_id);
CREATE INDEX idx_friendship_status    ON friendship(status);

-- =====================================================
-- 4) 장소
-- =====================================================
CREATE TABLE IF NOT EXISTS places (
                                      id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                                      name       VARCHAR(255) NOT NULL COMMENT '장소 이름',
                                      address    VARCHAR(500) NULL COMMENT '장소 주소',
                                      category   VARCHAR(50)  NULL COMMENT '장소 카테고리',
                                      is_active  BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
                                      updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='약속 장소 마스터';

CREATE INDEX idx_places_name      ON places(name);
CREATE INDEX idx_places_category  ON places(category);
CREATE INDEX idx_places_is_active ON places(is_active);

-- =====================================================
-- 5) 약속(미팅)
-- =====================================================
CREATE TABLE IF NOT EXISTS meeting (
                                       id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '약속 고유 식별자',
                                       title             VARCHAR(255) NOT NULL COMMENT '약속 제목',
                                       description       TEXT NULL COMMENT '약속 상세 설명',
                                       meeting_time      DATETIME NOT NULL COMMENT '약속 예정 시간',
                                       max_participants  INT DEFAULT 10 COMMENT '최대 참여 인원 수',
                                       status            VARCHAR(50) NOT NULL COMMENT '약속 진행 상태',
                                       host_id           BIGINT NOT NULL COMMENT '방장 사용자 ID',
                                       location_name     VARCHAR(500) NULL COMMENT '약속 장소명',
                                       location_address  VARCHAR(500) NULL COMMENT '약속 장소 상세 주소',
                                       place_id          BIGINT NULL COMMENT '장소 ID',
                                       created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '약속 생성 시각',
                                       updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '약속 수정 시각',
                                       CONSTRAINT fk_meeting_place FOREIGN KEY (place_id) REFERENCES places(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='약속(미팅) 본문';

CREATE INDEX idx_meeting_time   ON meeting(meeting_time);
CREATE INDEX idx_meeting_status ON meeting(status);
CREATE INDEX idx_meeting_host   ON meeting(host_id);
CREATE INDEX idx_meeting_place  ON meeting(place_id);

-- =====================================================
-- 6) 약속 히스토리
-- =====================================================
CREATE TABLE IF NOT EXISTS meeting_history (
                                               id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                                               meeting_id    BIGINT NOT NULL COMMENT '약속 ID',
                                               action        VARCHAR(50) NOT NULL COMMENT '수행된 액션',
                                               action_by     BIGINT NOT NULL COMMENT '액션 수행 사용자 ID',
                                               action_details JSON NULL COMMENT '액션 상세 정보',
                                               created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '히스토리 생성 시간',
                                               CONSTRAINT fk_meeting_history_meeting FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='약속 관련 이벤트 이력';

CREATE INDEX idx_history_meeting_id ON meeting_history(meeting_id);
CREATE INDEX idx_history_action_by  ON meeting_history(action_by);
CREATE INDEX idx_history_timestamp  ON meeting_history(created_at);

-- =====================================================
-- 7) 약속 참여자
-- =====================================================
CREATE TABLE IF NOT EXISTS meeting_participant (
                                                   id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
                                                   meeting_id   BIGINT NOT NULL COMMENT '약속 ID',
                                                   user_id      BIGINT NOT NULL COMMENT '참여자 사용자 ID',
                                                   response     VARCHAR(50) NOT NULL COMMENT '참여 응답 상태',
                                                   joined_at    DATETIME NULL COMMENT '참여 확정 시각',
                                                   invited_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '초대 발송 시각',
                                                   CONSTRAINT unique_meeting_user UNIQUE (meeting_id, user_id),
                                                   CONSTRAINT fk_participant_meeting FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE,
                                                   CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='약속 참여자 및 응답 상태';

CREATE INDEX idx_participant_meeting_id ON meeting_participant(meeting_id);
CREATE INDEX idx_participant_user_id    ON meeting_participant(user_id);
CREATE INDEX idx_participant_response   ON meeting_participant(response);

-- =====================================================
-- Done.
-- =====================================================
