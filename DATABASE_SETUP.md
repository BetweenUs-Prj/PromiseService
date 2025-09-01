# 🗄️ PromiseService Database Setup Guide

## 📋 개요
PromiseService를 위한 MySQL 데이터베이스 설정 및 초기화 가이드입니다.

## 🎯 데이터베이스 정보
- **데이터베이스명**: `beetween_us_db`
- **문자셋**: `utf8mb4`
- **콜레이션**: `utf8mb4_unicode_ci`
- **포트**: `3306` (기본값)

## 🚀 빠른 시작

### 1. MySQL 서버 실행
```bash
# Docker를 사용하는 경우
docker run --name mysql-promise -e MYSQL_ROOT_PASSWORD=Root123! -e MYSQL_DATABASE=beetween_us_db -p 3306:3306 -d mysql:8.0

# 또는 기존 MySQL 서버 사용
```

### 2. 데이터베이스 연결
```bash
mysql -u root -p -h localhost
```

### 3. 스키마 생성
```sql
-- 스키마 생성 스크립트 실행
source src/main/resources/schema-mysql.sql;
```

### 4. 테스트 데이터 삽입
```sql
-- 테스트 데이터 삽입
source src/main/resources/test-data.sql;
```

## 📊 테이블 구조

### 🏠 핵심 테이블

#### 1. `meeting` - 약속 정보
- 약속의 기본 정보 (제목, 장소, 시간, 최대 인원 등)
- 상태 관리 (OPEN, CONFIRMED, COMPLETED, CANCELLED)

#### 2. `meeting_participant` - 약속 참가자
- 약속에 참가하는 사용자들의 정보
- 역할 구분 (HOST, MEMBER)
- 참가 상태 (INVITED, JOINED, LEFT, DECLINED)

#### 3. `user_profile` - 사용자 프로필
- 사용자의 기본 정보 (이름, 이메일, 전화번호 등)
- 선호 교통수단 정보

#### 4. `user_identity` - OAuth 신원 정보
- 소셜 로그인 제공자별 사용자 정보
- 토큰 관리 (액세스 토큰, 리프레시 토큰)

#### 5. `friendship` - 친구 관계
- 사용자 간의 친구 관계 관리
- 상태 관리 (ACTIVE, BLOCKED, DELETED 등)

#### 6. `friend_request` - 친구 요청
- 친구 요청의 생명주기 관리
- 상태 관리 (PENDING, ACCEPTED, REJECTED, CANCELLED)

#### 7. `notification_log` - 알림 로그
- 발송된 알림의 이력 관리
- 전송 상태 추적 (PENDING, SENT, FAILED, DELIVERED)

#### 8. `system_config` - 시스템 설정
- 애플리케이션 설정 정보
- API 키, URL 등 환경 설정

## 🔧 주요 기능

### 📈 인덱스 최적화
- **단일 인덱스**: 자주 조회되는 컬럼별 인덱스
- **복합 인덱스**: 여러 컬럼을 조합한 효율적인 조회
- **외래키 인덱스**: 관계 테이블 간 조인 성능 향상

### 🎯 뷰 (View)
- **`v_active_meetings`**: 활성 약속 및 참가자 정보
- **`v_user_friends`**: 사용자 친구 관계 정보

### 🔒 제약 조건
- **외래키 제약**: 데이터 무결성 보장
- **유니크 제약**: 중복 데이터 방지
- **체크 제약**: 데이터 유효성 검증

## 📝 샘플 쿼리

### 사용자별 약속 참여 현황
```sql
SELECT 
    up.name as user_name,
    m.title as meeting_title,
    m.scheduled_at,
    mp.role,
    mp.status as participation_status
FROM user_profile up
JOIN meeting_participant mp ON up.user_id = mp.user_id
JOIN meeting m ON mp.meeting_id = m.id
WHERE up.user_id = 1
ORDER BY m.scheduled_at;
```

### 친구 관계가 있는 사용자들의 약속 참여
```sql
SELECT 
    u1.name as user_name,
    u2.name as friend_name,
    m.title as meeting_title
FROM friendship f
JOIN user_profile u1 ON f.user_id = u1.user_id
JOIN user_profile u2 ON f.friend_id = u2.user_id
JOIN meeting_participant mp1 ON f.user_id = mp1.user_id
JOIN meeting_participant mp2 ON f.friend_id = mp2.user_id
JOIN meeting m ON mp1.meeting_id = m.id AND mp2.meeting_id = m.id
WHERE f.status = 'ACTIVE';
```

## ⚙️ 설정 파일

### application.properties
```properties
# MySQL 데이터베이스 설정
spring.datasource.url=jdbc:mysql://localhost:3306/beetween_us_db
spring.datasource.username=root
spring.datasource.password=Root123!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

## 🧪 테스트 데이터

### 포함된 테스트 데이터
- **사용자**: 5명 (김철수, 이영희, 박민수, 정수진, 최준호)
- **약속**: 5개 (저녁 모임, 주말 등산, 영화 관람, 카페 모임, 운동 모임)
- **친구 관계**: 8개 관계
- **알림 로그**: 9개 알림

### 테스트 데이터 특징
- 현실적인 시나리오 기반
- 다양한 상태 조합 포함
- 관계형 데이터의 복잡성 반영

## 🔍 모니터링 및 유지보수

### 성능 모니터링
```sql
-- 테이블 크기 확인
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'beetween_us_db';

-- 인덱스 사용률 확인
SHOW INDEX FROM meeting;
```

### 데이터 백업
```bash
# 전체 데이터베이스 백업
mysqldump -u root -p beetween_us_db > backup_$(date +%Y%m%d_%H%M%S).sql

# 특정 테이블만 백업
mysqldump -u root -p beetween_us_db meeting meeting_participant > meetings_backup.sql
```

## 🚨 주의사항

### 보안
- 프로덕션 환경에서는 강력한 비밀번호 사용
- 애플리케이션 전용 사용자 계정 생성 권장
- 토큰 정보는 암호화하여 저장

### 성능
- 대용량 데이터의 경우 파티셔닝 고려
- 정기적인 인덱스 재구성
- 쿼리 성능 모니터링

## 📞 문제 해결

### 일반적인 문제
1. **연결 실패**: MySQL 서버 상태 및 포트 확인
2. **권한 오류**: 사용자 계정 권한 확인
3. **문자셋 문제**: utf8mb4 설정 확인

### 로그 확인
```sql
-- MySQL 에러 로그 확인
SHOW VARIABLES LIKE 'log_error';

-- 슬로우 쿼리 로그 확인
SHOW VARIABLES LIKE 'slow_query_log';
```

---

**🎉 데이터베이스 설정이 완료되었습니다! 이제 PromiseService를 실행할 수 있습니다.**
