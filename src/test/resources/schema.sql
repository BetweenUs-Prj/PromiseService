-- 테스트용 데이터베이스 스키마
-- 이유: 테스트 환경에서 외부 서비스(UserService)와 독립적으로 동작할 수 있도록 필요한 테이블만 생성

-- 테스트용 users 테이블 (외래키 참조 목적)
-- 이유: meetings 테이블의 host_id 외래키 제약조건을 만족시키기 위해

-- UserService Database Schema (변경함)

-- 사용자 계정 정보를 저장하는 테이블 (변경함)
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL, -- 제공받은 모델에 맞춰 필수로 변경 (변경함)
  `password` VARCHAR(255) NULL, -- 소셜 로그인은 비밀번호가 없음
  `name` VARCHAR(255) NOT NULL, -- username -> name으로 변경 (변경함)
  `provider` VARCHAR(50) NOT NULL, -- 예: KAKAO, GOOGLE, LOCAL (String 타입) (변경함)
  `provider_id` VARCHAR(255) NULL, -- 제공받은 모델에 맞춰 선택사항으로 변경 (변경함)
  `role` ENUM('USER', 'ADMIN', 'MODERATOR') NOT NULL DEFAULT 'USER', -- 권한 관리 (Enum 타입으로 타입 안전성 보장) (변경함)
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_email` (`email`) -- 제공받은 모델에 맞춰 이메일 유니크 제약 (변경함)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 프로필 정보를 저장하는 테이블 (변경함)
CREATE TABLE `user_profiles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `bio` TEXT NULL, -- 자기소개 (변경함)
  `location` VARCHAR(255) NULL, -- base_location -> location으로 변경 (변경함)
  `website` VARCHAR(255) NULL, -- 개인 웹사이트 (변경함)
  `phone_number` VARCHAR(50) NULL,
  `avatar_url` VARCHAR(500) NULL, -- profile_image_url -> avatar_url로 변경 (변경함)
  `preferred_transport` ENUM('WALK', 'BICYCLE', 'PUBLIC_TRANSPORT', 'CAR', 'MOTORCYCLE') DEFAULT 'WALK', -- 프로필로 이동 (변경함)
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_id` (`user_id` ASC), -- 한 명의 유저는 하나의 프로필만 가짐
  -- users 테이블의 id를 참조하는 외래키. 사용자가 삭제되면 프로필도 함께 삭제됨 (CASCADE)
  CONSTRAINT `fk_user_profiles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 친구 관계 테이블
CREATE TABLE friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'BLOCKED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 같은 사용자 간의 중복 친구 관계 방지
    UNIQUE KEY unique_friendship (user_id, friend_id),
    
    -- 자기 자신과 친구가 되는 것 방지
    CONSTRAINT check_not_self_friend CHECK (user_id != friend_id)
);

-- 친구 요청 테이블
CREATE TABLE friend_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    addressee_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (addressee_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 같은 사용자 간의 중복 요청 방지
    UNIQUE KEY unique_request (requester_id, addressee_id),
    
    -- 자기 자신에게 요청하는 것 방지
    CONSTRAINT check_not_self_request CHECK (requester_id != addressee_id)
);

-- 인덱스 생성 (변경함)
CREATE INDEX idx_users_provider_id ON users(provider, provider_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_friendships_user_id ON friendships(user_id);
CREATE INDEX idx_friendships_friend_id ON friendships(friend_id);
CREATE INDEX idx_friendships_status ON friendships(status);
CREATE INDEX idx_friend_requests_requester_id ON friend_requests(requester_id);
CREATE INDEX idx_friend_requests_addressee_id ON friend_requests(addressee_id);


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



