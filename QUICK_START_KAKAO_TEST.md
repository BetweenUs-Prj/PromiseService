# 카카오톡 알림 서비스 빠른 테스트 가이드

## 🚀 빠른 시작

### 1. 서버 실행 확인
```bash
# Spring Boot 애플리케이션이 실행중인지 확인
curl http://localhost:8080/api/health
```

### 2. 테스트 스크립트 실행

#### Windows (PowerShell)
```powershell
# 기본 실행 (자동 모드)
.\kakao-notification-test-script.ps1 -Auto

# 특정 사용자/약속으로 테스트
.\kakao-notification-test-script.ps1 -Auto -UserId 2 -MeetingId 5

# JWT 토큰 포함하여 실행
.\kakao-notification-test-script.ps1 -JwtToken "your_jwt_token_here" -UserId 1 -MeetingId 1
```

#### Linux/Mac (Bash)
```bash
# 기본 실행 (자동 모드)
./kakao-notification-test-script.sh --auto

# 특정 사용자/약속으로 테스트
./kakao-notification-test-script.sh --auto --user-id 2 --meeting-id 5

# JWT 토큰 포함하여 실행
./kakao-notification-test-script.sh --token "your_jwt_token_here" --user-id 1 --meeting-id 1
```

## 📋 주요 테스트 항목

### ✅ 성공 케이스
- 특정 수신자들에게 알림 전송
- 전체 참여자에게 알림 전송
- 서비스 상태 확인

### ⚠️ 오류 케이스
- 존재하지 않는 약속 ID
- 인증 정보 없이 요청
- 동의하지 않은 사용자

### 📊 성능 테스트
- 연속 요청 처리
- 응답 시간 측정

## 🔧 수동 테스트 (HTTP 클라이언트)

### VS Code / IntelliJ에서
1. `kakao-notification-test.http` 파일 열기
2. 환경 변수 수정:
   ```
   @jwtToken = 실제_JWT_토큰
   @userId = 1
   @meetingId = 1
   ```
3. 각 요청 옆의 "Send Request" 클릭

### Postman에서
1. `kakao-notification-postman.json` 컬렉션 임포트
2. Environment 변수 설정
3. 컬렉션 실행 (Runner 사용)

## 📱 예상 결과

### 정상 응답
```json
{
  "success": true,
  "sentCount": 3,
  "totalCount": 3,
  "failed": [],
  "message": "모든 메시지가 성공적으로 전송되었습니다"
}
```

### 부분 성공
```json
{
  "success": true,
  "sentCount": 2,
  "totalCount": 3,
  "failed": [
    {
      "userId": 4,
      "reason": "전송 조건 불충족"
    }
  ]
}
```

### 오류 응답
- **401**: 인증 필요
- **409**: 동의 필요  
- **400**: 잘못된 파라미터

## 🐛 문제 해결

### 서버 연결 실패
```bash
# 서버 상태 확인
curl http://localhost:8080/api/health

# 포트 확인
netstat -an | grep 8080
```

### 401 인증 오류
- JWT 토큰 만료 확인
- `X-User-ID` 헤더 포함 여부 확인

### 알림 전송 실패 (sent_count = 0)
- 카카오 친구 관계 확인
- 사용자 동의 상태 확인
- 카카오 앱 키 설정 확인

## 📞 빠른 확인 명령어

```bash
# 서버 실행 확인
curl http://localhost:8080/api/health

# 알림 서비스 상태 확인
curl -H "X-User-ID: 1" http://localhost:8080/api/notifications/health

# 카카오 사용 가능 여부 확인  
curl -H "X-User-ID: 1" http://localhost:8080/api/notifications/kakao/availability

# 간단 알림 테스트
curl -X POST -H "Content-Type: application/json" -H "X-User-ID: 1" \
  -d '{"meetingId": 1, "receiverIds": [2]}' \
  http://localhost:8080/api/notifications/kakao
```

---
**🎯 빠른 테스트 완료 후 `KAKAO_NOTIFICATION_TEST_GUIDE.md`에서 상세 가이드를 확인하세요!**


