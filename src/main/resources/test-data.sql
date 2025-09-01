-- =====================================================
-- PromiseService Test Data Insertion Script
-- Database: beetween_us_db
-- Description: 개발 및 테스트를 위한 샘플 데이터 삽입
-- =====================================================

USE `beetween_us_db`;

-- =====================================================
-- 1. 테스트 사용자 프로필 데이터
-- =====================================================

INSERT INTO `user_profile` (`user_id`, `name`, `email`, `phone_number`, `location`, `preferred_transport`) VALUES
(1, '김철수', 'kim@example.com', '010-1234-5678', '서울시 강남구', 'PUBLIC_TRANSPORT'),
(2, '이영희', 'lee@example.com', '010-2345-6789', '서울시 서초구', 'CAR'),
(3, '박민수', 'park@example.com', '010-3456-7890', '서울시 마포구', 'WALKING'),
(4, '정수진', 'jung@example.com', '010-4567-8901', '서울시 송파구', 'BICYCLE'),
(5, '최준호', 'choi@example.com', '010-5678-9012', '서울시 종로구', 'PUBLIC_TRANSPORT')
ON DUPLICATE KEY UPDATE 
    `name` = VALUES(`name`),
    `email` = VALUES(`email`),
    `phone_number` = VALUES(`phone_number`),
    `location` = VALUES(`location`),
    `preferred_transport` = VALUES(`preferred_transport`),
    `updated_at` = CURRENT_TIMESTAMP;

-- =====================================================
-- 2. 테스트 OAuth 신원 정보 데이터
-- =====================================================

INSERT INTO `user_identity` (`user_id`, `provider`, `provider_user_id`, `nickname`, `profile_image_url`) VALUES
(1, 'KAKAO', 'kakao_123456', '철수킹', 'https://example.com/avatar1.jpg'),
(2, 'GOOGLE', 'google_789012', '영희공주', 'https://example.com/avatar2.jpg'),
(3, 'KAKAO', 'kakao_345678', '민수맨', 'https://example.com/avatar3.jpg'),
(4, 'EMAIL', 'email_901234', '수진이', 'https://example.com/avatar4.jpg'),
(5, 'KAKAO', 'kakao_567890', '준호형', 'https://example.com/avatar5.jpg')
ON DUPLICATE KEY UPDATE 
    `nickname` = VALUES(`nickname`),
    `profile_image_url` = VALUES(`profile_image_url`),
    `updated_at` = CURRENT_TIMESTAMP;

-- =====================================================
-- 3. 테스트 친구 관계 데이터
-- =====================================================

INSERT INTO `friendship` (`user_id`, `friend_id`, `status`) VALUES
(1, 2, 'ACTIVE'),
(1, 3, 'ACTIVE'),
(2, 1, 'ACTIVE'),
(2, 4, 'ACTIVE'),
(3, 1, 'ACTIVE'),
(3, 5, 'ACTIVE'),
(4, 2, 'ACTIVE'),
(5, 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE 
    `status` = VALUES(`status`),
    `updated_at` = CURRENT_TIMESTAMP;

-- =====================================================
-- 4. 테스트 약속 데이터
-- =====================================================

INSERT INTO `meeting` (`title`, `place_id`, `scheduled_at`, `max_participants`, `memo`, `status`) VALUES
('저녁 모임', 901, '2025-01-15 19:30:00', 6, '7시까지 오세요! 맛있는 음식 먹으러 갈 예정입니다.', 'OPEN'),
('주말 등산', 902, '2025-01-18 08:00:00', 4, '아침 8시에 등산로 입구에서 만나요. 간단한 간식과 물 준비해주세요.', 'OPEN'),
('영화 관람', 903, '2025-01-20 20:00:00', 8, '최신 영화 보러 갈 예정입니다. 영화관에서 만나요!', 'OPEN'),
('카페 모임', 904, '2025-01-22 14:00:00', 5, '오후 2시에 카페에서 만나서 이야기 나누어요.', 'OPEN'),
('운동 모임', 905, '2025-01-25 18:00:00', 6, '저녁 6시에 체육관에서 만나서 운동해요!', 'OPEN')
ON DUPLICATE KEY UPDATE 
    `title` = VALUES(`title`),
    `memo` = VALUES(`memo`),
    `updated_at` = CURRENT_TIMESTAMP;

-- =====================================================
-- 5. 테스트 약속 참가자 데이터
-- =====================================================

INSERT INTO `meeting_participant` (`meeting_id`, `user_id`, `role`, `status`) VALUES
-- 저녁 모임 참가자
(1, 1, 'HOST', 'JOINED'),
(1, 2, 'MEMBER', 'JOINED'),
(1, 3, 'MEMBER', 'INVITED'),
(1, 4, 'MEMBER', 'INVITED'),

-- 주말 등산 참가자
(2, 2, 'HOST', 'JOINED'),
(2, 1, 'MEMBER', 'JOINED'),
(2, 5, 'MEMBER', 'JOINED'),

-- 영화 관람 참가자
(3, 3, 'HOST', 'JOINED'),
(3, 1, 'MEMBER', 'JOINED'),
(3, 2, 'MEMBER', 'JOINED'),
(3, 4, 'MEMBER', 'INVITED'),
(3, 5, 'MEMBER', 'INVITED'),

-- 카페 모임 참가자
(4, 4, 'HOST', 'JOINED'),
(4, 1, 'MEMBER', 'JOINED'),
(4, 2, 'MEMBER', 'JOINED'),

-- 운동 모임 참가자
(5, 5, 'HOST', 'JOINED'),
(5, 1, 'MEMBER', 'JOINED'),
(5, 3, 'MEMBER', 'JOINED')
ON DUPLICATE KEY UPDATE 
    `role` = VALUES(`role`),
    `status` = VALUES(`status`),
    `updated_at` = CURRENT_TIMESTAMP;

-- =====================================================
-- 6. 테스트 알림 로그 데이터
-- =====================================================

INSERT INTO `notification_log` (`user_id`, `template`, `title`, `content`, `status`, `provider`) VALUES
(2, 'MEETING_INVITE', '새로운 약속에 초대되었습니다', '김철수님이 "저녁 모임"에 초대했습니다.', 'SENT', 'KAKAO'),
(3, 'MEETING_INVITE', '새로운 약속에 초대되었습니다', '김철수님이 "저녁 모임"에 초대했습니다.', 'SENT', 'KAKAO'),
(4, 'MEETING_INVITE', '새로운 약속에 초대되었습니다', '김철수님이 "저녁 모임"에 초대했습니다.', 'SENT', 'KAKAO'),
(1, 'MEETING_INVITE', '새로운 약속에 초대되었습니다', '이영희님이 "주말 등산"에 초대했습니다.', 'SENT', 'KAKAO'),
(5, 'MEETING_INVITE', '새로운 약속에 초대되었습니다', '이영희님이 "주말 등산"에 초대했습니다.', 'SENT', 'KAKAO'),
(1, 'MEETING_INVITE', '새로운 약속에 초대되었습니다', '박민수님이 "영화 관람"에 초대했습니다.', 'SENT', 'KAKAO'),
(2, 'MEETING_INVITE', '새로운 약속에 초대되었습니다', '박민수님이 "영화 관람"에 초대했습니다.', 'SENT', 'KAKAO'),
(4, 'MEETING_INVITE', '새로운 약속에 초대되었습니다', '박민수님이 "영화 관람"에 초대했습니다.', 'SENT', 'KAKAO'),
(5, 'MEETING_INVITE', '새로운 약속에 초대되었습니다', '박민수님이 "영화 관람"에 초대했습니다.', 'SENT', 'KAKAO')
ON DUPLICATE KEY UPDATE 
    `title` = VALUES(`title`),
    `content` = VALUES(`content`),
    `updated_at` = CURRENT_TIMESTAMP;

-- =====================================================
-- 7. 테스트 친구 요청 데이터
-- =====================================================

INSERT INTO `friend_request` (`requester_id`, `addressee_id`, `status`) VALUES
(1, 5, 'PENDING'),
(4, 3, 'PENDING'),
(5, 2, 'PENDING')
ON DUPLICATE KEY UPDATE 
    `status` = VALUES(`status`),
    `updated_at` = CURRENT_TIMESTAMP;

-- =====================================================
-- 8. 데이터 삽입 완료 확인
-- =====================================================

SELECT 'Test data insertion completed successfully!' as message;

-- =====================================================
-- 9. 데이터 확인 쿼리
-- =====================================================

-- 사용자 수 확인
SELECT COUNT(*) as total_users FROM `user_profile`;

-- 약속 수 확인
SELECT COUNT(*) as total_meetings FROM `meeting`;

-- 활성 약속 수 확인
SELECT COUNT(*) as active_meetings FROM `meeting` WHERE `status` = 'OPEN';

-- 친구 관계 수 확인
SELECT COUNT(*) as total_friendships FROM `friendship` WHERE `status` = 'ACTIVE';

-- 알림 로그 수 확인
SELECT COUNT(*) as total_notifications FROM `notification_log`;

-- =====================================================
-- 10. 샘플 조회 쿼리 예시
-- =====================================================

-- 사용자별 참가한 약속 목록
SELECT 
    up.name as user_name,
    m.title as meeting_title,
    m.scheduled_at,
    mp.role,
    mp.status as participation_status
FROM `user_profile` up
JOIN `meeting_participant` mp ON up.user_id = mp.user_id
JOIN `meeting` m ON mp.meeting_id = m.id
WHERE up.user_id IN (1, 2, 3)
ORDER BY up.name, m.scheduled_at;

-- 친구 관계가 있는 사용자들의 약속 참여 현황
SELECT 
    u1.name as user_name,
    u2.name as friend_name,
    m.title as meeting_title,
    mp1.status as user_status,
    mp2.status as friend_status
FROM `friendship` f
JOIN `user_profile` u1 ON f.user_id = u1.user_id
JOIN `user_profile` u2 ON f.friend_id = u2.user_id
JOIN `meeting_participant` mp1 ON f.user_id = mp1.user_id
JOIN `meeting_participant` mp2 ON f.friend_id = mp2.user_id
JOIN `meeting` m ON mp1.meeting_id = m.id AND mp2.meeting_id = m.id
WHERE f.status = 'ACTIVE'
ORDER BY u1.name, m.scheduled_at;
