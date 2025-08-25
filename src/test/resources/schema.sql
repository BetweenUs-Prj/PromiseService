-- 테스트용 데이터베이스 스키마
-- 이유: 테스트 환경에서 외부 서비스(UserService)와 독립적으로 동작할 수 있도록 필요한 테이블만 생성

-- 테스트용 users 테이블 (외래키 참조 목적)
-- 이유: meetings 테이블의 host_id 외래키 제약조건을 만족시키기 위해
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 테스트용 user_profiles 테이블 (JPA 관계 매핑 목적)
-- 이유: UserProfile 엔티티의 JPA 매핑을 위해
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bio TEXT,
    location VARCHAR(255),
    website VARCHAR(255),
    phone_number VARCHAR(20),
    avatar_url VARCHAR(500),
    preferred_transport VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- meetings 테이블
-- 이유: 약속 관리 서비스의 핵심 엔티티로, 약속 기본 정보를 저장하기 위해
CREATE TABLE meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    host_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    meeting_time DATETIME NOT NULL,
    max_participants INT DEFAULT 10 CHECK (max_participants <= 10),
    status VARCHAR(50) DEFAULT 'WAITING',  -- H2에서는 ENUM 대신 VARCHAR 사용
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

-- meeting_participants 테이블
-- 이유: 약속 참여자 정보와 응답 상태를 관리하기 위해
CREATE TABLE meeting_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    response VARCHAR(50) DEFAULT 'INVITED',  -- H2에서는 ENUM 대신 VARCHAR 사용
    invited_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    responded_at DATETIME,
    joined_at DATETIME,
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_meeting_user (meeting_id, user_id),
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_user_id (user_id),
    INDEX idx_response (response)
);

-- meeting_history 테이블
-- 이유: 약속과 관련된 모든 활동을 추적하여 감사 로그를 제공하기 위해
CREATE TABLE meeting_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,  -- H2에서는 ENUM 대신 VARCHAR 사용
    details TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_meeting_id (meeting_id),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_timestamp (timestamp)
);



