# 🚀 API JSON 테스트 실행 단계별 가이드

## 📋 현재 상황
- ✅ 테스트 도구 파일들 생성 완료
- ⚠️ 서버 시작 중 (H2 호환성 문제 해결)
- ✅ JSON 요청/응답 예시 준비됨

## 🎯 단계별 테스트 방법

### 1단계: 서버 상태 확인
```bash
# 서버가 실행 중인지 확인
curl http://localhost:8080/actuator/health

# 예상 응답
{"status":"UP"}
```

### 2단계: 간단한 GET 요청 테스트
```bash
# 사용자 존재 확인 API
curl -H "X-User-ID: 123" http://localhost:8080/api/users/123/exists

# 예상 응답
{"userIdValue": 123, "existsValue": true}
```

### 3단계: JSON POST 요청 테스트
```bash
# 약속 생성 API 테스트
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-User-ID: 123" \
  -d '{
    "title": "첫 번째 JSON 테스트",
    "description": "API 동작 확인용",
    "meetingTime": "2025-08-20T14:00:00",
    "maxParticipants": 5,
    "locationName": "테스트 장소",
    "locationAddress": "서울시 강남구",
    "participantUserIds": []
  }'
```

### 4단계: 복잡한 JSON 구조 테스트
```bash
# 참여자 포함 약속 생성
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-User-ID: 123" \
  -d '{
    "title": "복잡한 JSON 테스트",
    "description": "중첩 구조 검증",
    "meetingTime": "2025-08-20T15:30:00",
    "maxParticipants": 10,
    "locationName": "홍대입구역",
    "locationAddress": "서울시 마포구 양화로",
    "locationCoordinates": "{\"lat\": 37.557527, \"lng\": 126.925320}",
    "participantUserIds": [456, 789]
  }'
```

## 📊 JSON 응답 검증 포인트

### ✅ 성공 응답 (201 Created)
```json
{
  "id": 1,
  "hostId": 123,
  "title": "첫 번째 JSON 테스트",
  "description": "API 동작 확인용",
  "status": "WAITING",
  "locationName": "테스트 장소",
  "participants": [
    {
      "userId": 123,
      "response": "ACCEPTED",
      "joinedAt": "2025-08-19T15:45:00"
    }
  ],
  "createdAt": "2025-08-19T15:45:00",
  "updatedAt": "2025-08-19T15:45:00"
}
```

### ❌ 에러 응답 (400 Bad Request)
```json
{
  "timestamp": "2025-08-19T15:45:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/meetings"
}
```

## 🛠️ 문제 해결 방법

### 1. 서버 연결 실패
```bash
# 서버 프로세스 확인
jps | grep -i gradle  # Linux/Mac
Get-Process | Where-Object {$_.ProcessName -like "*java*"}  # Windows

# 서버 재시작
./gradlew bootRun
```

### 2. JSON 형식 오류
- Content-Type 헤더 확인: `application/json`
- JSON 문법 검증: https://jsonlint.com
- 특수문자 이스케이프 처리

### 3. 인증 헤더 누락
- X-User-ID 헤더 필수
- 유효한 사용자 ID 사용 (123, 456, 789)

## 🎯 테스트 도구별 사용법

### IntelliJ HTTP Client
1. `api-test.http` 파일 열기
2. 각 요청 옆의 ▶️ 버튼 클릭
3. 응답 창에서 JSON 확인

### Postman
1. `postman-collection.json` import
2. Environment 설정 (baseUrl, userId)
3. 요청 실행 및 Tests 탭에서 검증

### VS Code REST Client
1. REST Client 확장 프로그램 설치
2. `.http` 파일에서 Send Request 클릭

## 📈 고급 JSON 테스트

### 1. 배열 처리 테스트
```json
{
  "participantUserIds": [456, 789, 101, 102]
}
```

### 2. 중첩 객체 테스트
```json
{
  "locationCoordinates": "{\"lat\": 37.498095, \"lng\": 127.027621, \"address\": {\"city\": \"서울\", \"district\": \"강남구\"}}"
}
```

### 3. 날짜 형식 테스트
```json
{
  "meetingTime": "2025-08-20T14:00:00Z",
  "deadline": "2025-08-19T23:59:59.999Z"
}
```

✅ **API JSON 테스트가 성공하면 다음 단계로 진행 가능합니다!**

























