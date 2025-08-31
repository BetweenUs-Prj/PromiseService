# 🚀 개선된 MeetingController 테스트 가이드

## 📋 **테스트 개요**
- **X-User-Id 방식**: 직접 사용자 ID 전달 (테스트용)
- **X-Provider-Id 방식**: OAuth 제공자 ID로 사용자 조회 (운영용)
- **인증 정보 없음**: 에러 케이스 테스트

## 🔑 **테스트 1: X-User-Id 방식 (기존 방식)**

```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "title": "E2E 자동 알림 테스트 (X-User-Id)",
    "meetingTime": "2025-08-30T21:00:00",
    "locationName": "온라인",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

**예상 결과**: 201 Created + 약속 정보

## 🔑 **테스트 2: X-Provider-Id 방식 (다양한 헤더 이름 지원)**

### **2-1. 표준 형태**
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-Provider-Id: 2" \
  -d '{
    "title": "E2E 자동 알림 테스트 (X-Provider-Id)",
    "meetingTime": "2025-08-30T22:00:00",
    "locationName": "오프라인",
    "participants": [2, 3, 4],
    "sendNotification": true
  }'
```

### **2-2. 대문자 ID 형태**
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-Provider-ID: 3" \
  -d '{
    "title": "E2E 자동 알림 테스트 (X-Provider-ID)",
    "meetingTime": "2025-08-30T22:30:00",
    "locationName": "하이브리드",
    "participants": [3, 4, 5],
    "sendNotification": true
  }'
```

### **2-3. 카멜케이스 형태**
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-ProviderId: 4" \
  -d '{
    "title": "E2E 자동 알림 테스트 (X-ProviderId)",
    "meetingTime": "2025-08-30T23:00:00",
    "locationName": "카페",
    "participants": [4, 5, 6],
    "sendNotification": true
  }'
```

### **2-4. 소문자 형태**
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "x-provider-id: 5" \
  -d '{
    "title": "E2E 자동 알림 테스트 (x-provider-id)",
    "meetingTime": "2025-08-30T23:30:00",
    "locationName": "공원",
    "participants": [5, 6, 7],
    "sendNotification": true
  }'
```

**예상 결과**: 모든 형태 모두 201 Created + 약속 정보
- **X-Provider-Id: 2** → 사용자 ID 2로 매핑
- **X-Provider-ID: 3** → 사용자 ID 3으로 매핑
- **X-ProviderId: 4** → 사용자 ID 4로 매핑
- **x-provider-id: 5** → 사용자 ID 5로 매핑

## ❌ **테스트 3: 인증 정보 없음 (에러 케이스)**

```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -d '{
    "title": "인증 정보 없는 테스트",
    "meetingTime": "2025-08-30T23:00:00",
    "locationName": "테스트",
    "participants": [1, 2],
    "sendNotification": true
  }'
```

**예상 결과**: 400 Bad Request + "X-User-Id 또는 X-Provider-Id 중 하나는 필수입니다."

## 📊 **알림 전송 결과 확인**

약속 생성 후 생성된 약속 ID로 알림 전송 결과를 확인:

```bash
# 약속 ID를 실제 생성된 ID로 교체
curl http://localhost:8080/api/notifications/meeting/1
```

## 🎯 **테스트 시나리오**

1. **정상 케이스**: X-User-Id 또는 X-Provider-Id로 약속 생성
2. **자동 알림**: 약속 생성 시 자동으로 카카오톡 발송
3. **에러 케이스**: 인증 정보 없이 요청 시 적절한 에러 응답
4. **결과 확인**: 카카오톡 "나와의 채팅"에서 메시지 수신 확인

## 🔧 **Mock 구현 상세**

- **사용자 ID 1~8**: Mock으로 존재하는 것으로 처리
- **X-Provider-Id**: 숫자 문자열을 사용자 ID로 변환 (예: "2" → 2)
- **헤더 이름 다양성**: 6가지 형태의 헤더 이름 지원
  - `X-Provider-Id` (표준)
  - `X-Provider-ID` (대문자 ID)
  - `X-ProviderId` (카멜케이스)
  - `x-provider-id` (소문자)
  - `X-PROVIDER-ID` (전체 대문자)
  - `x-providerid` (소문자 카멜케이스)
- **실제 구현**: UserIdentityRepository 구현 후 Mock 로직 교체 필요

## 📱 **카카오톡 확인 방법**

1. 카카오톡 앱 실행
2. "나와의 채팅" 탭으로 이동
3. 약속 초대 메시지 확인
4. 메시지 내용: 제목, 장소, 시간, 링크 포함
