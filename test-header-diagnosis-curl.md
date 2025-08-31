# 🔍 헤더 진단 테스트 가이드 (curl)

## 📋 **진단 개요**
- **목적**: X-Provider-Id 헤더가 실제로 서버에 전달되는지 정확히 확인
- **방법**: 1분 증명용 에코 엔드포인트 + 개선된 컨트롤러 테스트
- **결과**: 헤더 전달 여부에 따른 정확한 원인 파악

## 🔍 **테스트 1: 헤더 에코 엔드포인트 (1분 증명용)**

```bash
curl -v http://localhost:8080/api/debug/echo-headers \
  -H "X-Provider-Id: kakao_4399986838"
```

**예상 응답:**
```json
{
  "seenProviderId": "4399986838",
  "springHasXProviderId": "4399986838",
  "headers": {
    "X-Provider-Id": "kakao_4399986838",
    "User-Agent": "curl/7.68.0",
    "Accept": "*/*"
  },
  "timestamp": 1703123456789
}
```

**진단 결과:**
- `seenProviderId`에 값이 보이면 → **헤더는 들어옴** (컨트롤러 로직만 손보면 됨)
- `seenProviderId`가 null이면 → **정말 요청에 헤더가 안 실려 있음** (도구/프록시/필터 문제)

## 🔍 **테스트 2: X-Provider-Id만으로 약속 생성**

```bash
curl -v -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-Provider-Id: kakao_4399986838" \
  -d '{
    "title": "헤더 진단 테스트",
    "meetingTime": "2025-08-30T21:00:00",
    "locationName": "온라인",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

**예상 결과:**
- 성공 시: 201 Created + 약속 정보
- 실패 시: 400 Bad Request + 구체적인 에러 메시지

## 🔍 **테스트 3: 다양한 헤더 이름으로 테스트**

### **3-1. 대문자 ID 형태**
```bash
curl -v -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-Provider-ID: kakao_4399986838" \
  -d '{
    "title": "X-Provider-ID 테스트",
    "meetingTime": "2025-08-30T21:30:00",
    "locationName": "하이브리드",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

### **3-2. 카멜케이스 형태**
```bash
curl -v -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-ProviderId: kakao_4399986838" \
  -d '{
    "title": "X-ProviderId 테스트",
    "meetingTime": "2025-08-30T22:00:00",
    "locationName": "카페",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

## 🔍 **테스트 4: 인증 정보 없는 경우 (에러 케이스)**

```bash
curl -v -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -d '{
    "title": "인증 정보 없는 테스트",
    "meetingTime": "2025-08-30T22:30:00",
    "locationName": "테스트",
    "participants": [1, 2, 3],
    "sendNotification": true
  }'
```

**예상 결과**: 400 Bad Request + "X-User-Id 또는 X-Provider-Id 중 하나는 필수입니다."

## 🔧 **서버 콘솔에서 확인할 내용**

### **1단계: 헤더 스니핑 필터**
```
🔍 SNIFF: X-Provider-Id=4399986838, X-Provider-ID=null, X-ProviderId=null
```

### **2단계: 헤더 에코 엔드포인트**
```
=== 🔍 헤더 에코 요청 감지 ===
서블릿 레벨 X-Provider-Id: 4399986838
Spring HttpHeaders X-Provider-Id: 4399986838
전체 헤더: {X-Provider-Id=4399986838, User-Agent=curl/7.68.0, Accept=*/*}
=== 🔍 헤더 에코 응답: {...} ===
```

### **3단계: 약속 생성 컨트롤러**
```
=== 🔍 약속 생성 요청 - 헤더 진단 시작 ===
진단 결과 - X-User-Id: null, Provider ID: 4399986838
사용자 ID 해결 완료: 4399986838 → 4399986838
약속방 생성 시작 - 방장: 4399986838, 제목: 헤더 진단 테스트
```

## 🎯 **진단 결과별 해결 방안**

### **✅ 헤더가 들어오는 경우**
- **원인**: 컨트롤러에서 읽는 로직 문제
- **해결**: 이미 개선된 컨트롤러로 해결됨
- **다음 단계**: 약속 생성 및 알림 전송 테스트

### **❌ 헤더가 안 들어오는 경우**
- **원인**: 도구/프록시/필터 문제
- **체크리스트**:
  1. 다른 인스턴스를 때리는 중
  2. Talend가 커스텀 헤더를 안 실어줌
  3. 필터가 헤더를 지움
  4. CORS 설정 문제

## 🚀 **최종 확인 루틴**

1. **GET /api/debug/echo-headers** → 헤더가 보이는지 확인
2. **POST /api/meetings** 성공 → 응답의 id 확보
3. **GET /api/notifications/meeting/{id}** → http_status=200 / result_code=0 확인
4. **카카오톡 나와의 채팅** → 수신 확인

## 🔍 **문제 해결 순서**

### **1단계: 헤더 에코 확인**
```bash
curl -v http://localhost:8080/api/debug/echo-headers -H "X-Provider-Id: kakao_4399986838"
```

### **2단계: 약속 생성 테스트**
```bash
curl -v -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-Provider-Id: kakao_4399986838" \
  -d '{"title":"테스트","meetingTime":"2025-08-30T21:00:00","locationName":"온라인","participants":[1,2,3]}'
```

### **3단계: 서버 콘솔 로그 분석**
- 헤더 스니핑 필터 로그 확인
- 헤더 에코 응답 확인
- 약속 생성 로그 확인

## 🎉 **성공 시나리오**

모든 테스트가 성공하면:
1. **X-Provider-Id 인증**: 정상 작동
2. **별칭 허용**: 다양한 헤더 이름 지원
3. **자동 알림**: 약속 생성 시 카카오톡 자동 발송
4. **운영 준비**: 실제 OAuth 시스템 연결 가능

## 🚨 **주의사항**

- **서버 재시작 필수**: 새로 추가된 `DebugController`와 `HeaderSniffFilter` 적용을 위해
- **로그 확인 필수**: 서버 콘솔에서 진단 정보를 정확히 파악
- **단계별 테스트**: 에코 → 약속 생성 → 알림 확인 순서로 진행
