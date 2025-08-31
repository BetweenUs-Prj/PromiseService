# 약속 관리 서비스 ERD (Entity Relationship Diagram)

## 개요
약속 생성, 상태 관리, 참여자 관리 기능을 제공하는 서비스의 데이터베이스 설계

## 테이블 구조

### 1. users (사용자 테이블) - UserService에서 관리
| 필드명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 사용자 고유 ID |
| kakao_id | BIGINT | UNIQUE, NOT NULL | 카카오 ID |
| username | VARCHAR | NOT NULL | 사용자명 |
| profile_image_url | VARCHAR | NULL | 프로필 이미지 URL |
| base_location | VARCHAR | NULL | 기본 위치 |
| preferred_transport | ENUM | NULL | 선호 교통수단 |
| created_at | DATETIME | NULL | 생성일시 |
| updated_at | DATETIME | NULL | 수정일시 |

**참고:** 이 테이블은 UserService에서 관리되며, PromiseService는 외래키로 참조만 함

### 2. meetings (약속 테이블)
| 필드명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 약속 고유 ID |
| host_id | BIGINT | FK → users.id, NOT NULL | 약속 생성자 |
| title | VARCHAR(255) | NOT NULL | 약속 제목 |
| description | TEXT | NULL | 약속 설명 |
| meeting_time | DATETIME | NOT NULL | 약속 시간 |
| max_participants | INT | DEFAULT 10, CHECK ≤ 10 | 최대 참여자 수 |
| status | ENUM | DEFAULT 'WAITING' | 약속 상태 |
| location | VARCHAR(500) | NULL | 약속 장소 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**상태값:**
- `WAITING`: 대기 중
- `CONFIRMED`: 확정됨
- `COMPLETED`: 완료됨
- `CANCELLED`: 취소됨

**인덱스:**
- `idx_host_id` (host_id)
- `idx_meeting_time` (meeting_time)
- `idx_status` (status)

### 3. meeting_participants (약속 참여자 테이블)
| 필드명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 참여자 레코드 ID |
| meeting_id | BIGINT | FK → meetings.id, NOT NULL | 약속 ID |
| user_id | BIGINT | FK → users.id, NOT NULL | 사용자 ID |
| response | ENUM | DEFAULT 'INVITED' | 응답 상태 |
| joined_at | DATETIME | NULL | 수락 시각 |
| invited_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 초대 시각 |
| responded_at | DATETIME | NULL | 응답 시각 |

**응답 상태:**
- `INVITED`: 초대됨
- `ACCEPTED`: 수락함
- `REJECTED`: 거절함

**제약조건:**
- `unique_meeting_user`: (meeting_id, user_id) 유니크 제약

**인덱스:**
- `idx_meeting_id` (meeting_id)
- `idx_user_id` (user_id)
- `idx_response` (response)

### 4. meeting_history (약속 히스토리 테이블)
| 필드명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 히스토리 레코드 ID |
| meeting_id | BIGINT | FK → meetings.id, NOT NULL | 약속 ID |
| action | ENUM | NOT NULL | 수행된 액션 |
| user_id | BIGINT | FK → users.id, NOT NULL | 액션 수행자 |
| details | JSON | NULL | 상세 정보 |
| timestamp | DATETIME | DEFAULT CURRENT_TIMESTAMP | 액션 수행 시각 |

**액션 타입:**
- `CREATED`: 약속 생성
- `JOINED`: 참여
- `DECLINED`: 거절
- `COMPLETED`: 완료
- `CANCELLED`: 취소
- `UPDATED`: 수정

**인덱스:**
- `idx_meeting_id` (meeting_id)
- `idx_user_id` (user_id)
- `idx_timestamp` (timestamp)

## 관계도

```
users (1) ←→ (N) meetings
  ↑                    ↑
  |                    |
  |                    |
  |                    |
  |                    ↓
  |              meeting_participants (N) ←→ (1) meetings
  |                    ↑
  |                    |
  |                    |
  ↓                    ↓
users (1) ←→ (N) meeting_history (N) ←→ (1) meetings
```

## 주요 기능별 테이블 활용

### 약속 생성
1. `meetings` 테이블에 새 레코드 생성
2. `meeting_history` 테이블에 'CREATED' 액션 기록

### 참여자 초대
1. `meeting_participants` 테이블에 초대 레코드 생성 (response = 'INVITED')

### 참여자 응답
1. `meeting_participants` 테이블의 response 업데이트
2. `meeting_history` 테이블에 'JOINED' 또는 'DECLINED' 액션 기록

### 약속 상태 변경
1. `meetings` 테이블의 status 업데이트
2. `meeting_history` 테이블에 해당 액션 기록

## 성능 최적화 고려사항

1. **인덱스**: 자주 조회되는 필드에 인덱스 설정
2. **파티셔닝**: meeting_history 테이블은 시간별 파티셔닝 고려
3. **캐싱**: Redis를 활용한 자주 조회되는 데이터 캐싱
4. **정규화**: 데이터 중복 최소화를 위한 적절한 정규화
