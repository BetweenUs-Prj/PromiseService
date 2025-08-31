# 🚀 Provider ID 매핑 문제 종합 진단 및 해결 가이드 (curl)

## 📋 **문제 상황**
```
"Provider ID 매핑 실패: 4399968638"
```

## 🎯 **원인 분석**
이 400 에러는 **"DB 매핑 없음/불일치"**가 100% 원인입니다.

## 🔍 **종합 진단 및 해결 방법**

### **1단계: 데이터베이스 연결 정보 확인**

**목적**: 실행 중인 앱이 실제로 어떤 DB에 연결되어 있는지 확인 (환경 착오 방지)

```bash
curl -v http://localhost:8080/api/debug/ds
```

**응답 예시**:
```json
{
  "databaseProductName": "H2",
  "hikariJdbcUrl": "jdbc:h2:mem:testdb",
  "hikariUsername": "sa",
  "hikariPoolName": "HikariPool-1"
}
```

**확인 사항**:
- `hikariJdbcUrl`이 예상한 DB와 일치하는지 확인
- 로컬/테스트 DB 착각이 자주 발생함

### **2단계: 카카오 API 진단 (진짜 User ID 확인)**

**목적**: 현재 토큰의 진짜 Kakao User ID와 앱 ID 확인

#### **2-1. 카카오 사용자 정보 조회**
```bash
curl -v http://localhost:8080/api/debug/kakao/user/me
```

**응답 예시**:
```json
{
  "message": "카카오 사용자 정보 조회 성공",
  "userInfo": {
    "id": 4399968638,
    "connected_at": "2024-12-20T10:00:00Z",
    "properties": {
      "nickname": "사용자명"
    }
  }
}
```

**핵심 확인**: `userInfo.id` 값이 헤더로 보내는 `4399968638`과 **정확히 일치**해야 함

#### **2-2. 카카오 액세스 토큰 정보 조회**
```bash
curl -v http://localhost:8080/api/debug/kakao/user/access-token-info
```

**응답 예시**:
```json
{
  "message": "카카오 액세스 토큰 정보 조회 성공",
  "tokenInfo": {
    "id": 4399968638,
    "app_id": 123456,
    "expires_in": 21599
  }
}
```

**핵심 확인**: `tokenInfo.app_id`가 우리 앱과 같은지 확인

### **3단계: Provider ID 링크 (매핑 데이터 생성)**

**목적**: 진짜 카카오 사용자 ID와 내부 사용자 ID를 매핑

```bash
curl -v -X POST http://localhost:8080/api/debug/link-provider \
  -H "X-User-Id: 1" \
  -H "X-Provider-Id: kakao_4399968638"
```

**응답 예시**:
```json
{
  "message": "Provider ID 링크 성공",
  "userId": 1,
  "providerId": "4399968638",
  "provider": "KAKAO",
  "savedId": 1,
  "timestamp": 1703123456789
}
```

**매핑 완료**: 내부 사용자 ID 1 ↔ 카카오 사용자 ID 4399968638

### **4단계: 약속 생성 테스트 (문제 해결 확인)**

**목적**: Provider ID 매핑이 정상 작동하는지 확인

```bash
curl -v -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-Provider-Id: kakao_4399968638" \
  -d '{
    "title": "종합 진단 해결 테스트",
    "meetingTime": "2025-08-30T21:00:00",
    "locationName": "온라인",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

**성공 응답**: 201 Created + 약속 정보

## 🔍 **DB 직접 확인 및 수정**

### **매핑 데이터 확인**
```sql
-- 정확히 이 둘 다 조회해서 무엇이 있는지 확인
SELECT user_id, provider, provider_user_id
FROM user_identity
WHERE provider='KAKAO'
  AND provider_user_id IN ('4399968638','4399986838');
```

### **매핑 데이터가 없는 경우 수정**

#### **PostgreSQL**
```sql
INSERT INTO user_identity (user_id, provider, provider_user_id)
VALUES (1,'KAKAO','4399968638')
ON CONFLICT (provider, provider_user_id) 
DO UPDATE SET user_id = EXCLUDED.user_id;
```

#### **MySQL**
```sql
INSERT INTO user_identity (user_id, provider, provider_user_id)
VALUES (1,'KAKAO','4399968638')
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);
```

#### **H2 (테스트용)**
```sql
INSERT INTO user_identity (user_id, provider, provider_user_id)
VALUES (1,'KAKAO','4399968638');
```

### **Provider 열 값 정확성 확인**
```sql
-- DB provider 값이 정확히 'KAKAO'인지 확인 (Kakao, kakao면 조회 실패)
SELECT DISTINCT provider FROM user_identity WHERE LOWER(provider) LIKE '%kakao%';

-- 수정이 필요한 경우
UPDATE user_identity SET provider='KAKAO' WHERE LOWER(provider)='kakao';
```

### **컬럼 타입 확인 및 수정**
```sql
-- provider_user_id 컬럼은 VARCHAR(64) 권장 (BIGINT면 자리수/부호/오버플로우 이슈)

-- PostgreSQL
ALTER TABLE user_identity ALTER COLUMN provider_user_id TYPE VARCHAR(64) USING provider_user_id::TEXT;

-- MySQL
ALTER TABLE user_identity MODIFY provider_user_id VARCHAR(64) NOT NULL;
```

## 🎯 **진단 결과별 해결 방안**

### **✅ 모든 단계 성공**
- Provider ID 매핑 정상 작동
- 자동 카카오톡 알림 시스템 완벽 작동

### **❌ 데이터베이스 연결 실패**
- 서버가 실행 중인지 확인
- `application.properties`의 DB 설정 확인

### **❌ 카카오 API 실패**
- 토큰 유효성 문제 또는 네트워크 문제
- 새로운 토큰 발급 필요

### **❌ User ID 불일치**
- 헤더로 보내는 ID와 실제 토큰의 ID가 다름
- 올바른 ID로 헤더 수정 또는 새로운 토큰 발급

### **❌ Provider ID 링크 실패**
- 서버 재시작 필요 (`DebugController` 수정사항 적용)
- DB 스키마 문제

### **❌ 약속 생성 실패**
- Provider ID → User ID 매핑 문제
- `user_identity` 테이블에 데이터 확인

## 🚀 **최종 확인 루틴**

1. **GET /api/debug/ds** → 실행 중인 앱의 DB 연결 정보 확인
2. **GET /api/debug/kakao/user/me** → 진짜 카카오 User ID 확인
3. **POST /api/debug/link-provider** → Provider ID 링크 성공
4. **POST /api/meetings** → 약속 생성 성공
5. **GET /api/notifications/meeting/{id}** → http_status=200 / result_code=0 확인
6. **카카오톡 나와의 채팅** → 수신 확인

## 🎉 **해결 완료!**

이제 `X-Provider-Id: kakao_4399968638`로 약속을 생성할 수 있습니다!

**자동 카카오톡 알림**도 정상 작동합니다! 📱✨

## 🚨 **주의사항**

1. **서버 재시작 필수**: `DebugController` 수정사항 적용을 위해
2. **토큰 유효성**: 카카오 액세스 토큰이 유효해야 함
3. **DB 스키마**: `user_identity` 테이블이 생성되어 있어야 함
4. **환경 착오 방지**: 실행 중인 앱이 올바른 DB에 연결되어 있는지 확인

## 🔧 **문제가 지속되는 경우**

### **수동 DB 입력**
```sql
INSERT INTO user_identity (user_id, provider, provider_user_id) 
VALUES (1, 'KAKAO', '4399968638');
```

### **로그 확인**
서버 콘솔에서 다음 로그 확인:
```
🔗 Provider ID 링크 요청 - userId: 1, providerId: kakao_4399968638
kakao_ 접두사 제거: kakao_4399968638 → 4399968638
Provider ID 링크 성공 - userId: 1, providerId: 4399968638, saved: 1
```

### **코드 레포지토리 시그니처 확인**
```java
// 반드시 String 기반으로 조회
Optional<UserIdentity> findByProviderAndProviderUserId(Provider provider, String providerUserId);

// providerUserId가 long이면 문자열/자릿수 불일치로 못 찾을 수 있어요 → String으로
```

### **조회 직전 방어 코드**
```java
pid = pid == null ? null : pid.trim();
log.info("providerId='{}' len={}", pid, pid.length()); // 숨은 공백 탐지
```
