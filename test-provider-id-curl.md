# 🔍 X-Provider-Id 헤더 디버깅 테스트 가이드

## 📋 **테스트 개요**
- **목적**: 서버가 실제로 어떤 헤더를 받는지 확인
- **문제**: X-Provider-Id 헤더가 컨트롤러까지 전달되지 않는 경우
- **해결**: 헤더 디버그 출력 → 별칭 허용 코드 → CORS 설정 순서로 처리

## 🔍 **테스트 1: X-Provider-Id만으로 약속 생성**

```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-Provider-Id: 4399986838" \
  -d '{
    "title": "X-Provider-Id 디버깅 테스트",
    "meetingTime": "2025-08-30T21:00:00",
    "locationName": "온라인",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

**예상 결과**: 
- 성공 시: 201 Created + 약속 정보
- 실패 시: 400 Bad Request + "X-User-Id 또는 X-Provider-Id 중 하나는 필수입니다."

## 🔍 **테스트 2: 다양한 헤더 이름으로 테스트**

### **2-1. 대문자 ID 형태**
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-Provider-ID: 4399986838" \
  -d '{
    "title": "X-Provider-ID 테스트",
    "meetingTime": "2025-08-30T21:30:00",
    "locationName": "하이브리드",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

### **2-2. 카멜케이스 형태**
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-ProviderId: 4399986838" \
  -d '{
    "title": "X-ProviderId 테스트",
    "meetingTime": "2025-08-30T22:00:00",
    "locationName": "카페",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

### **2-3. 소문자 형태**
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "x-provider-id: 4399986838" \
  -d '{
    "title": "x-provider-id 테스트",
    "meetingTime": "2025-08-30T22:30:00",
    "locationName": "공원",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

## 🔍 **테스트 3: X-User-Id와 X-Provider-Id 둘 다 있는 경우**

```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "X-Provider-Id: 4399986838" \
  -d '{
    "title": "둘 다 있는 경우 테스트",
    "meetingTime": "2025-08-30T23:00:00",
    "locationName": "테스트",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

**예상 결과**: 201 Created + 약속 정보 (X-User-Id 우선)

## 🔍 **테스트 4: 인증 정보 없는 경우 (에러 케이스)**

```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -d '{
    "title": "인증 정보 없는 테스트",
    "meetingTime": "2025-08-30T23:30:00",
    "locationName": "테스트",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

**예상 결과**: 400 Bad Request + "X-User-Id 또는 X-Provider-Id 중 하나는 필수입니다."

## 🔍 **서버 콘솔에서 확인할 내용**

테스트 실행 후 서버 콘솔에서 다음 정보를 확인하세요:

```
=== 🔍 헤더 디버그 정보 ===
REQ HEADERS 전체 = {Content-Type=[application/json], X-Provider-Id=[4399986838], ...}
X-User-Id        = null
X-Provider-Id    = 4399986838
헤더 [Content-Type] = [application/json]
헤더 [X-Provider-Id] = [4399986838]
=== 🔍 헤더 디버그 끝 ===
```

## 🔧 **문제 해결 순서**

### **1단계: 헤더 디버그 확인**
- 서버 콘솔에서 `X-Provider-Id` 헤더가 보이는지 확인
- 보이지 않으면 CORS/필터 문제

### **2단계: 별칭 허용 코드 확인**
- `readProviderId()` 메서드가 다양한 헤더 이름을 지원하는지 확인
- 로그에서 "Provider ID 헤더 발견" 메시지 확인

### **3단계: CORS 설정 확인**
- `CorsConfig`에서 `X-Provider-Id` 관련 헤더가 허용되었는지 확인
- 브라우저에서 호출 시 CORS 오류가 발생하지 않는지 확인

### **4단계: 매핑 데이터 확인**
- `X-Provider-Id: 4399986838`이 실제 사용자 ID로 매핑되는지 확인
- Mock 구현에서는 숫자 변환으로 처리

## 📊 **성공 시 확인 사항**

1. **약속 생성 성공**: 201 Created 응답
2. **알림 전송 결과**: `GET /api/notifications/meeting/{meetingId}`
3. **카카오톡 수신**: "나와의 채팅"에서 약속 초대 메시지 확인

## 🎯 **예상 결과**

- **모든 테스트 성공**: X-Provider-Id 인증이 정상 작동
- **일부 테스트 실패**: 특정 헤더 이름 형태에 문제가 있음
- **모든 테스트 실패**: 기본적인 헤더 전달에 문제가 있음

## 🚀 **다음 단계**

테스트 완료 후:
1. 성공한 경우: 자동 카카오톡 알림 시스템 테스트
2. 실패한 경우: 서버 콘솔 로그 분석하여 문제점 파악
3. 문제 해결 후: 다시 테스트 실행
