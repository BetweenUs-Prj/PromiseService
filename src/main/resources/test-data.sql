-- 1) 테스트 사용자 데이터
INSERT INTO users (user_id, name, profile_image, provider_id, created_at, updated_at)
VALUES
  (1, '김철수', 'https://example.com/avatar1.jpg', 'kakao_12345', NOW(), NOW()),
  (2, '이영희', 'https://example.com/avatar2.jpg', 'kakao_67890', NOW(), NOW()),
  (3, '박민수', 'https://example.com/avatar3.jpg', 'kakao_11111', NOW(), NOW()),
  (4, '정수진', 'https://example.com/avatar4.jpg', 'kakao_22222', NOW(), NOW()),
  (5, '최준호', 'https://example.com/avatar5.jpg', 'kakao_33333', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  profile_image = VALUES(profile_image),
  provider_id = VALUES(provider_id),
  updated_at = NOW();

-- 2) 더미 장소가 없으면 만든다 (이름 기준)
INSERT INTO places (name, address, is_active, created_at, updated_at)
SELECT 'DUMMY_PLACE', '서울시 테스트구 더미동 123', 1, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM places WHERE name = 'DUMMY_PLACE'
);

-- 2) 테스트용 더미 장소들 (자주 사용되는 ID들)
INSERT INTO places (id, name, address, is_active, created_at, updated_at)
VALUES
  (101, '더미 장소 101', '주소 미정', 1, NOW(), NOW()),
  (432, '더미 장소 432', '주소 미정', 1, NOW(), NOW()),
  (756, '더미 장소 756', '주소 미정', 1, NOW(), NOW()),
  (115, '더미 장소 115', '주소 미정', 1, NOW(), NOW()),
  (999, '더미 장소 999', '주소 미정', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  address = VALUES(address),
  is_active = VALUES(is_active),
  updated_at = NOW();

-- 2) 방금(혹은 기존) 더미 id 확인
SELECT id INTO @DUMMY_ID FROM places WHERE name = 'DUMMY_PLACE' LIMIT 1;
SELECT @DUMMY_ID AS dummy_place_id;
-- 더미/임시 구분
ALTER TABLE places
    ADD COLUMN status ENUM('DRAFT','ACTIVE','INACTIVE') NOT NULL DEFAULT 'DRAFT',
    ADD COLUMN source VARCHAR(20) NULL,
    ADD COLUMN external_id VARCHAR(100) NULL,
    ADD UNIQUE KEY uq_place_source_external (source, external_id);

-- external_id 컬럼이 없으면 추가 (기존 테이블에)
ALTER TABLE places 
ADD COLUMN IF NOT EXISTS external_id VARCHAR(255) NULL;

-- 기본값 정리
ALTER TABLE places
    MODIFY is_active TINYINT(1) NOT NULL DEFAULT 1;   -- 혹은 active 컬럼을 사용 중이면 그걸로

-- 3) meeting_participant 외래키 활성화 (users 테이블 참조)
ALTER TABLE meeting_participant 
ADD CONSTRAINT fk_participant_user 
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
