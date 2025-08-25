# 🚀 빠른 API JSON 테스트 방법

## 현재 상황
- Spring Boot 서버 시작 실패 (스키마 문제)
- 테스트 환경 설정 복잡함

## ✅ 즉시 가능한 JSON API 테스트 방법들

### 방법 1: IntelliJ HTTP Client (추천! ⭐)
1. **파일**: `api-test.http` (이미 생성됨)
2. **실행**: IntelliJ에서 파일 열고 ▶️ 버튼 클릭
3. **장점**: 서버 없이도 문법 체크, 변수 사용 가능

### 방법 2: Postman 
1. **파일**: `postman-collection.json` import
2. **장점**: GUI 환경, 테스트 자동화 가능

### 방법 3: curl 스크립트
1. **파일**: `test-api.sh` 실행
2. **전제조건**: 서버가 실행 중이어야 함

### 방법 4: 브라우저 확장 프로그램
- **REST Client** (VS Code)
- **Thunder Client** (VS Code)
- **Advanced REST client** (Chrome)

## 🎯 API JSON 테스트 예시 (서버 실행 후)

### 약속 생성 JSON 테스트
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "X-User-ID: 123" \
  -d '{
    "title": "JSON 테스트 약속",
    "description": "API 테스트용",
    "meetingTime": "2025-08-20T14:00:00",
    "maxParticipants": 5,
    "locationName": "강남역",
    "locationAddress": "서울시 강남구",
    "participantUserIds": [456, 789]
  }'
```

### 예상 JSON 응답
```json
{
  "id": 1,
  "hostId": 123,
  "title": "JSON 테스트 약속",
  "status": "WAITING",
  "participants": [
    {
      "userId": 123,
      "response": "ACCEPTED"
    },
    {
      "userId": 456, 
      "response": "INVITED"
    }
  ],
  "createdAt": "2025-08-19T15:30:00"
}
```

## 🚨 서버 시작 필요시
1. 스키마 문제 해결 후
2. `./gradlew bootRun` 실행
3. http://localhost:8080 접근 확인

## 📝 JSON 검증 포인트
- ✅ Content-Type: application/json
- ✅ 요청/응답 JSON 구조
- ✅ HTTP 상태 코드 (201, 200, 400, 404)
- ✅ 헤더 검증 (X-User-ID)
- ✅ 날짜 형식 (ISO 8601)
- ✅ 중첩 객체 구조 (participants)







