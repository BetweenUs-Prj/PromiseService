-- ==============================================
-- 🟢 H2 데이터베이스용 스키마
-- 이유: H2 데이터베이스의 문법에 맞춰 개발 및 테스트 환경에서 사용하기 위해
-- ==============================================

-- ==============================================
-- 🟢 약속 테이블 (meeting 관리용)
-- ==============================================
CREATE TABLE meeting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    meeting_time TIMESTAMP NOT NULL,
    max_participants INT DEFAULT 10 CHECK (max_participants <= 10),
    status VARCHAR(20) DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    location_name VARCHAR(500),
    location_address VARCHAR(500),
    location_coordinates TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================
-- 🟢 약속 참여자 테이블
-- ==============================================
CREATE TABLE meeting_participant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    response VARCHAR(10) DEFAULT 'INVITED' CHECK (response IN ('INVITED', 'ACCEPTED', 'REJECTED')),
    joined_at TIMESTAMP NULL,
    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_meeting_participant_meeting FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE,
    CONSTRAINT unique_meeting_user UNIQUE (meeting_id, user_id)
);

-- ==============================================
-- 🟢 약속 히스토리 테이블
-- ==============================================
CREATE TABLE meeting_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('CREATED', 'JOINED', 'DECLINED', 'COMPLETED', 'CANCELLED', 'UPDATED')),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_meeting_history_meeting FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE
);

-- ==============================================
-- 🟢 OAuth 사용자 신원 정보 테이블
-- 이유: 카카오 등 OAuth 제공자의 사용자 ID와 내부 사용자 ID를 매핑하기 위해
-- ==============================================
CREATE TABLE user_identity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL CHECK (provider IN ('KAKAO', 'GOOGLE', 'NAVER', 'APPLE')),
    provider_user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 멱등성 보장: 동일한 제공자/사용자 ID 조합은 중복 불가
    CONSTRAINT uk_user_identity_provider_user UNIQUE (provider, provider_user_id)
);

CREATE INDEX idx_user_identity_user_id ON user_identity (user_id);
CREATE INDEX idx_user_identity_provider ON user_identity (provider);
CREATE INDEX idx_user_identity_provider_user ON user_identity (provider, provider_user_id);

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
    token_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_kakao_user_id ON user_kakao_info (user_id);
CREATE INDEX idx_user_kakao_uuid ON user_kakao_info (kakao_uuid);

-- ==============================================
-- 🟢 친구 관계 테이블
-- ==============================================
CREATE TABLE friends (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_user_id BIGINT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'BLOCKED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_friendship UNIQUE (user_id, friend_user_id)
);

CREATE INDEX idx_friends_user_id ON friends (user_id);
CREATE INDEX idx_friends_friend_user_id ON friends (friend_user_id);

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
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_kakao_friend_mapping UNIQUE (user_id, friend_user_id)
);

CREATE INDEX idx_kakao_friend_user_id ON kakao_friend_map (user_id);
CREATE INDEX idx_kakao_friend_friend_user_id ON kakao_friend_map (friend_user_id);
CREATE INDEX idx_kakao_friend_uuid ON kakao_friend_map (kakao_uuid);

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_consents_talk_message ON user_consents (talk_message_consent);
CREATE INDEX idx_user_consents_friends ON user_consents (friends_consent);

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
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    start_at TIMESTAMP NOT NULL,
    remind_at TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    detail_url VARCHAR(500),
    host_user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 약속 참여자 테이블 (AppointmentParticipant)
CREATE TABLE appointment_participant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'INVITED' CHECK (state IN ('INVITED', 'ACCEPTED', 'REJECTED', 'CANCELLED')),
    notified_at TIMESTAMP,
    notify_status VARCHAR(20) DEFAULT 'PENDING' CHECK (notify_status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_appointment_user UNIQUE (appointment_id, user_id)
);

-- ==============================================
-- 🟢 테스트 데이터 (H2 전용)
-- ==============================================

-- 기본 사용자 동의 정보 (테스트용)
INSERT INTO user_consents (user_id, talk_message_consent, friends_consent, location_consent, data_collection_consent) 
VALUES 
(1, true, true, true, true),
(2, true, true, false, true),
(3, false, true, true, false),
(4, true, false, true, true);

-- 테스트용 약속 데이터
INSERT INTO meeting (title, description, meeting_time, location_name, status) 
VALUES 
('개발팀 회의', '주간 스프린트 회의', '2024-12-20 14:00:00', '회의실 A', 'WAITING'),
('카페 모임', '친구들과 커피 한 잔', '2024-12-21 15:30:00', '스타벅스 강남점', 'CONFIRMED'),
('영화 관람', '신작 영화 보기', '2024-12-22 19:00:00', 'CGV 강남', 'WAITING');

-- 테스트용 참여자 데이터
INSERT INTO meeting_participant (meeting_id, user_id, response) 
VALUES 
(1, 1, 'ACCEPTED'),
(1, 2, 'INVITED'),
(2, 1, 'ACCEPTED'),
(2, 3, 'ACCEPTED'),
(3, 2, 'INVITED'),
(3, 4, 'INVITED');

-- ==============================================
-- 🟢 알림 전송 로그 테이블 (운영용)
-- 이유: 카카오톡/SMS 등 알림 전송 결과를 기록하여 전송 상태 추적 및 디버깅 지원
-- ==============================================
CREATE TABLE notification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    channel VARCHAR(32) NOT NULL CHECK (channel IN ('KAKAO', 'SMS', 'EMAIL')),
    payload_json TEXT NOT NULL,
    http_status INT NOT NULL,
    result_code INT,
    error_json TEXT,
    trace_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 멱등성 보장: 동일한 약속/사용자/채널/추적ID로 중복 전송 방지
    CONSTRAINT uk_notification_log UNIQUE (meeting_id, user_id, channel, trace_id)
);

-- 인덱스 추가 (조회 성능 최적화)
CREATE INDEX idx_notification_log_meeting ON notification_log(meeting_id);
CREATE INDEX idx_notification_log_user ON notification_log(user_id);
CREATE INDEX idx_notification_log_trace ON notification_log(trace_id);


