# 실제 카카오톡 전송 설정 가이드

## 🎯 실제 카카오톡을 받기 위한 필수 단계

### 1. 카카오 Developers 앱 설정

1. **[카카오 Developers](https://developers.kakao.com/) 접속**
2. **내 애플리케이션 → 애플리케이션 추가하기**
3. **REST API 키** 복사해두기

### 2. 카카오 로그인 설정

1. **제품 설정 → 카카오 로그인 → 활성화 ON**
2. **Redirect URI 설정**: `http://localhost:8080/login/oauth2/code/kakao`
3. **동의항목 설정** (중요!):
   - ✅ **카카오톡 메시지 전송**: **필수 동의**
   - ✅ **닉네임**: 필수 동의
   - ✅ **카카오계정(이메일)**: 필수 동의

### 3. 환경 변수 설정

**Windows PowerShell**:
```powershell
# 카카오 앱 키 설정
$env:KAKAO_CLIENT_ID="your-rest-api-key"

# 카카오 액세스 토큰 설정 (로그인 후 발급받은 토큰)
$env:KAKAO_TEST_ACCESS_TOKEN="your-access-token"
```

**Linux/Mac**:
```bash
export KAKAO_CLIENT_ID="your-rest-api-key"
export KAKAO_TEST_ACCESS_TOKEN="your-access-token"
```

### 4. 카카오 액세스 토큰 발급받기

#### 방법 1: 브라우저로 직접 발급
```
https://kauth.kakao.com/oauth/authorize?client_id={REST_API_KEY}&redirect_uri=http://localhost:8080/login/oauth2/code/kakao&response_type=code&scope=talk_message
```

#### 방법 2: 카카오톡 테스트 페이지 사용
1. 서버 실행: `./gradlew bootRun`
2. 브라우저에서 `http://localhost:8080/kakao-test.html` 접속
3. **카카오 로그인** 버튼 클릭
4. 로그인 후 **액세스 토큰** 복사

### 5. 실제 테스트 방법

#### 5.1 환경변수로 토큰 설정 후 테스트
```powershell
# 1. 토큰 설정
$env:KAKAO_TEST_ACCESS_TOKEN="your-real-access-token"

# 2. 서버 시작
./gradlew bootRun

# 3. 다른 터미널에서 테스트
.\kakao-notification-test-script.ps1 -Auto
```

#### 5.2 HTTP 파일로 직접 테스트
```http
### 실제 카카오톡 알림 전송
POST http://localhost:8080/api/notifications/kakao
X-User-ID: 1
Content-Type: application/json

{
  "meetingId": 1,
  "receiverIds": [2, 3]
}
```

## 📱 실제 카카오톡 메시지 형태

전송되는 메시지:
```
🎉 김철수님의 약속 초대

📋 개발팀 회의
🕒 12월 20일(금) 14:00
📍 회의실 A

약속 준비 완료! 😊

📱 상세보기: http://localhost:8080/meetings/123
```

## 🔍 로그로 확인하기

### 성공 시 로그:
```
[INFO] 실제 카카오톡 알림 전송 시작 - 발송자: 1, 대상: 2명
[INFO] 카카오 메시지 전송 시작 - 수신자: 2명
[DEBUG] 카카오 메시지 전송 성공 - UUID: mock-uuid-2
[DEBUG] 카카오 메시지 전송 성공 - UUID: mock-uuid-3
[INFO] 카카오 메시지 전송 완료 - 성공: 2, 실패: 0
[INFO] 실제 카카오톡 알림 전송 완료 - 성공: 2/2
```

### 실패 시 로그:
```
[WARN] 카카오 액세스 토큰을 찾을 수 없음 - 사용자 ID: 1
[ERROR] 카카오 API 호출 오류 - UUID: mock-uuid-2, 상태: 401, 응답: {"error":"invalid_token"}
```

## 🚨 주의사항

1. **실제 카카오톡 전송**: 이제 실제로 카카오톡이 전송됩니다!
2. **테스트 사용자**: 카카오 앱 설정에서 테스트 사용자로 등록된 계정만 메시지 수신 가능
3. **API 제한**: 카카오 API 호출 제한이 있으니 과도한 테스트 주의
4. **토큰 만료**: 액세스 토큰은 만료되므로 주기적으로 갱신 필요

## 🛠️ 트러블슈팅

### 토큰 관련 오류
- `invalid_token`: 토큰이 만료되었거나 잘못됨 → 재로그인 필요
- `insufficient_scope`: 카카오톡 메시지 권한 없음 → 동의항목 확인

### 친구 관련 오류  
- `not_friend`: 메시지 수신자가 카카오톡 친구가 아님
- `blocked`: 차단된 사용자

### API 제한 오류
- `rate_limit_exceeded`: API 호출 제한 초과 → 잠시 후 재시도

---

**이제 실제 카카오톡으로 약속 알림을 받을 수 있습니다! 🎉**


