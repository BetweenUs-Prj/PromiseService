-- 약속 관리 서비스 H2 호환 스키마
-- 이유: H2 데이터베이스에서 정상 동작하도록 MySQL 문법을 H2 호환으로 변경

-- 테스트용 users 테이블 (외부 UserService 대신)
-- 이유: 개발/테스트 환경에서 외래키 제약조건을 만족시키기 위해
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 약속 테이블 (H2 호환)
-- 이유: H2는 ENUM을 VARCHAR로, INDEX를 별도 생성으로 처리해야 함
CREATE TABLE IF NOT EXISTS meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    host_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    meeting_time TIMESTAMP NOT NULL,
    max_participants INT DEFAULT 10,
    status VARCHAR(20) DEFAULT 'WAITING',
    location_name VARCHAR(500),
    location_address VARCHAR(500),
    location_coordinates TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE
);

-- meetings 테이블 인덱스
-- 이유: H2에서는 CREATE INDEX 문으로 별도 생성해야 함
CREATE INDEX IF NOT EXISTS idx_meetings_host_id ON meetings(host_id);
CREATE INDEX IF NOT EXISTS idx_meetings_meeting_time ON meetings(meeting_time);
CREATE INDEX IF NOT EXISTS idx_meetings_status ON meetings(status);

-- 약속 참여자 테이블 (H2 호환)
-- 이유: ENUM을 VARCHAR로, UNIQUE KEY를 별도 처리
CREATE TABLE IF NOT EXISTS meeting_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    response VARCHAR(20) DEFAULT 'INVITED',
    joined_at TIMESTAMP NULL,
    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- meeting_participants 테이블 인덱스와 제약조건
-- 이유: H2에서는 UNIQUE 제약조건과 INDEX를 별도로 생성
CREATE UNIQUE INDEX IF NOT EXISTS unique_meeting_user ON meeting_participants(meeting_id, user_id);
CREATE INDEX IF NOT EXISTS idx_participants_meeting_id ON meeting_participants(meeting_id);
CREATE INDEX IF NOT EXISTS idx_participants_user_id ON meeting_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_participants_response ON meeting_participants(response);

-- 약속 히스토리 테이블 (H2 호환)
-- 이유: JSON 타입을 TEXT로, ENUM을 VARCHAR로 변경
CREATE TABLE IF NOT EXISTS meeting_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- meeting_history 테이블 인덱스
-- 이유: 히스토리 조회 성능을 위한 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_history_meeting_id ON meeting_history(meeting_id);
CREATE INDEX IF NOT EXISTS idx_history_user_id ON meeting_history(user_id);
CREATE INDEX IF NOT EXISTS idx_history_timestamp ON meeting_history(timestamp);

-- 테스트 데이터 삽입
-- 이유: API 테스트를 위한 기본 사용자 데이터 제공
INSERT INTO users (id, email, name) VALUES 
(123, 'test1@example.com', '테스트사용자1'),
(456, 'test2@example.com', '테스트사용자2'),
(789, 'test3@example.com', '테스트사용자3')
ON DUPLICATE KEY UPDATE email = VALUES(email);

