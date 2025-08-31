-- ==============================================
-- 🟢 MySQL 데이터베이스용 스키마
-- 이유: MySQL 데이터베이스의 문법에 맞춰 프로덕션 환경에서 사용하기 위해
-- ==============================================

-- ==============================================
-- 🟢 약속 테이블 (meeting 관리용)
-- ==============================================
CREATE TABLE meeting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    meeting_time DATETIME NOT NULL,
    max_participants INT DEFAULT 10 CHECK (max_participants <= 10),
    status ENUM('WAITING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') DEFAULT 'WAITING',
    location_name VARCHAR(500),
    location_address VARCHAR(500),
    location_coordinates TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- 🟢 약속 참여자 테이블
-- ==============================================
CREATE TABLE meeting_participant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    response ENUM('INVITED', 'ACCEPTED', 'REJECTED') DEFAULT 'INVITED',
    joined_at DATETIME NULL,
    invited_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE,
    UNIQUE KEY unique_meeting_user (meeting_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- 🟢 약속 히스토리 테이블
-- ==============================================
CREATE TABLE meeting_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action ENUM('CREATED', 'JOINED', 'DECLINED', 'COMPLETED', 'CANCELLED', 'UPDATED') NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE,
    INDEX idx_meeting_history_meeting_id (meeting_id),
    INDEX idx_meeting_history_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- 🟢 사용자 카카오 정보 테이블
-- ==============================================
CREATE TABLE user_kakao_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    kakao_uuid VARCHAR(100),
    kakao_access_token TEXT,
    kakao_refresh_token TEXT,
    kakao_scopes_json TEXT,
    token_expires_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_kakao_user_id (user_id),
    INDEX idx_user_kakao_uuid (kakao_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- 🟢 친구 관계 테이블
-- ==============================================
CREATE TABLE friends (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_user_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'BLOCKED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_friends_user_id (user_id),
    INDEX idx_friends_friend_user_id (friend_user_id),
    UNIQUE KEY unique_friendship (user_id, friend_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- 🟢 카카오 친구 매핑 테이블
-- ==============================================
CREATE TABLE kakao_friend_map (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_user_id BIGINT NOT NULL,
    kakao_uuid VARCHAR(100) NOT NULL,
    kakao_nickname VARCHAR(100),
    kakao_profile_image TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_synced_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_kakao_friend_user_id (user_id),
    INDEX idx_kakao_friend_friend_user_id (friend_user_id),
    INDEX idx_kakao_friend_uuid (kakao_uuid),
    UNIQUE KEY unique_kakao_friend_mapping (user_id, friend_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- 🟢 사용자 동의 정보 테이블
-- ==============================================
CREATE TABLE user_consents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    talk_message_consent BOOLEAN NOT NULL DEFAULT FALSE,
    friends_consent BOOLEAN NOT NULL DEFAULT FALSE,
    location_consent BOOLEAN NOT NULL DEFAULT FALSE,
    data_collection_consent BOOLEAN NOT NULL DEFAULT FALSE,
    marketing_consent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_consents_user_id (user_id),
    INDEX idx_user_consents_talk_message (talk_message_consent),
    INDEX idx_user_consents_friends (friends_consent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- 🟢 약속 시스템 전용 테이블들
-- ==============================================

-- 약속 테이블 (Appointment)
CREATE TABLE appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    max_participants INTEGER,
    recommended_place VARCHAR(500),
    title VARCHAR(200) NOT NULL,
    place VARCHAR(300),
    latitude DOUBLE,
    longitude DOUBLE,
    start_at DATETIME NOT NULL,
    remind_at DATETIME,
    status ENUM('DRAFT', 'CONFIRMED', 'COMPLETED', 'CANCELLED') NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    detail_url VARCHAR(500),
    host_user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_appointment_host_user_id (host_user_id),
    INDEX idx_appointment_start_at (start_at),
    INDEX idx_appointment_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 약속 참여자 테이블 (AppointmentParticipant)
CREATE TABLE appointment_participant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    state ENUM('INVITED', 'ACCEPTED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'INVITED',
    notified_at DATETIME,
    notify_status ENUM('PENDING', 'SENT', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_appointment_participant_appointment_id (appointment_id),
    INDEX idx_appointment_participant_user_id (user_id),
    INDEX idx_appointment_participant_state (state),
    UNIQUE KEY unique_appointment_user (appointment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


