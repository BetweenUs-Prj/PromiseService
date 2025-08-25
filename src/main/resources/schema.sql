
-- ==============================================
-- 🟢 약속 테이블 (meeting 관리용)
-- ==============================================
CREATE TABLE meeting (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY, -- PK
                         title VARCHAR(255) NOT NULL,          -- 약속 제목
                         description TEXT,                     -- 약속 설명
                         meeting_time DATETIME NOT NULL,       -- 약속 시간
                         max_participants INT DEFAULT 10 CHECK (max_participants <= 10), -- 최대 참여자 (10명 제한)
                         status ENUM('WAITING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') DEFAULT 'WAITING', -- 약속 상태
                         location_name VARCHAR(500),           -- 장소 이름
                         location_address VARCHAR(500),        -- 장소 주소
                         location_coordinates TEXT,            -- 좌표 정보 (JSON 저장 가능)
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 생성 시간
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 수정 시간


);

-- ==============================================
-- 🟢 약속 참여자 테이블
-- ==============================================
CREATE TABLE meeting_participant (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY, -- PK
                                     meeting_id BIGINT NOT NULL,    -- FK: meetings.id
                                     user_id BIGINT NOT NULL,       -- FK: users.user_id
                                     response ENUM('INVITED', 'ACCEPTED', 'REJECTED') DEFAULT 'INVITED', -- 응답 상태
                                     joined_at DATETIME NULL,       -- 실제 참여 시간
                                     invited_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 초대된 시간

                                     FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE, -- 약속 삭제 시 참여자 삭제
                                     FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

                                     UNIQUE KEY unique_meeting_user (meeting_id, user_id) -- 중복 방지
);

-- ==============================================
-- 🟢 약속 히스토리 테이블
-- ==============================================
CREATE TABLE meeting_history (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY, -- PK
                                 meeting_id BIGINT NOT NULL,           -- FK: meetings.id
                                 user_id BIGINT NOT NULL,              -- FK: users.user_id (행동 주체)
                                 action ENUM('CREATED', 'JOINED', 'DECLINED', 'COMPLETED', 'CANCELLED', 'UPDATED') NOT NULL, -- 수행된 액션
                                 timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, -- 기록된 시간

                                 FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE,
                                 FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,


);
