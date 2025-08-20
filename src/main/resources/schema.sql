-- 약속 관리 서비스 데이터베이스 스키마
-- 참고: users 테이블은 UserService에서 관리됨
-- UserService.users 테이블 구조:
-- - id (PK)
-- - kakao_id (카카오 ID)
-- - username (사용자명)
-- - profile_image_url (프로필 이미지)
-- - base_location (기본 위치)
-- - preferred_transport (선호 교통수단)
-- - created_at, updated_at

-- 약속 테이블
CREATE TABLE meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    host_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    meeting_time DATETIME NOT NULL,
    max_participants INT DEFAULT 10 CHECK (max_participants <= 10),
    status ENUM('WAITING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') DEFAULT 'WAITING',
    location_name VARCHAR(500),
    location_address VARCHAR(500),
    location_coordinates TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_host_id (host_id),
    INDEX idx_meeting_time (meeting_time),
    INDEX idx_status (status)
);

-- 약속 참여자 테이블
CREATE TABLE meeting_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    response ENUM('INVITED', 'ACCEPTED', 'REJECTED') DEFAULT 'INVITED',
    joined_at DATETIME NULL,
    invited_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    responded_at DATETIME NULL,
    
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_meeting_user (meeting_id, user_id),
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_user_id (user_id),
    INDEX idx_response (response)
);

-- 약속 히스토리 테이블
CREATE TABLE meeting_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    action ENUM('CREATED', 'JOINED', 'DECLINED', 'COMPLETED', 'CANCELLED', 'UPDATED') NOT NULL,
    user_id BIGINT NOT NULL,
    details JSON,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp)
);
