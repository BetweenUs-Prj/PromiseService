# 🚀 Provider ID 매핑 문제 즉시 해결 가이드 (curl)

## 📋 **문제 상황**
```
"Provider ID 매핑 실패: 4399968638"
```

## 🎯 **원인**
`user_identity` 테이블에 `provider='KAKAO'`와 `provider_user_id='4399968638'`인 매핑 데이터가 없습니다.

## 🚀 **즉시 해결 방법**

### **1단계: 실제 카카오 사용자 ID 확인**

```bash
curl -v http://localhost:8080/api/debug/kakao/user/me
```

**응답 예시:**
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

**실제 카카오 사용자 ID**: `4399968638`

### **2단계: Provider ID 링크 (매핑 데이터 생성)**

```bash
curl -v -X POST http://localhost:8080/api/debug/link-provider \
  -H "X-User-Id: 1" \
  -H "X-Provider-Id: kakao_4399968638"
```

**응답 예시:**
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

### **3단계: 약속 생성 테스트 (문제 해결 확인)**

```bash
curl -v -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-Provider-Id: kakao_4399968638" \
  -d '{
    "title": "매핑 해결 테스트",
    "meetingTime": "2025-08-30T21:00:00",
    "locationName": "온라인",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

**성공 응답**: 201 Created + 약속 정보

## 🔍 **DB 확인**

### **매핑 데이터 확인**
```sql
SELECT * FROM user_identity WHERE provider = 'KAKAO';
```

**예상 결과:**
```
id | user_id | provider | provider_user_id | created_at
1  | 1       | KAKAO   | 4399968638      | 2024-12-20 10:00:00
```

### **알림 전송 결과 확인**
```bash
# 약속 ID를 응답에서 추출하여 사용
curl http://localhost:8080/api/notifications/meeting/{meetingId}
```

## 🎉 **해결 완료!**

이제 `X-Provider-Id: kakao_4399968638`로 약속을 생성할 수 있습니다!

**자동 카카오톡 알림**도 정상 작동합니다! 📱✨

## 🚨 **주의사항**

1. **서버 재시작 필수**: `DebugController` 수정사항 적용을 위해
2. **토큰 유효성**: 카카오 액세스 토큰이 유효해야 함
3. **DB 스키마**: `user_identity` 테이블이 생성되어 있어야 함

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
