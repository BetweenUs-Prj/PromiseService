# 카카오톡 약속 알림 서비스 테스트 가이드

## 개요

카카오톡 약속 알림 서비스가 정상적으로 작동하는지 확인하기 위한 다양한 테스트 도구와 방법을 제공합니다.

**이유:** 카카오톡 알림 기능의 안정성과 신뢰성을 검증하고, 다양한 오류 상황에서도 적절하게 처리되는지 확인하기 위해

## 테스트 도구 목록

### 1. 쉘 스크립트 (kakao-notification-test-script.sh)
- **용도:** 터미널에서 자동화된 테스트 실행
- **특징:** 컬러 출력, 다양한 시나리오 테스트, 자동/수동 모드 지원

### 2. HTTP 파일 (kakao-notification-test.http)  
- **용도:** IntelliJ IDEA, VS Code에서 직접 API 테스트
- **특징:** 환경 변수 지원, 응답 예시 포함

### 3. Postman 컬렉션 (kakao-notification-postman.json)
- **용도:** Postman에서 GUI 기반 테스트
- **특징:** 자동 검증 스크립트 포함, 환경 변수 관리

## 사전 준비사항

### 1. 서버 실행
```bash
# Spring Boot 애플리케이션 실행
./gradlew bootRun
```

### 2. 테스트 데이터 준비
- 테스트용 사용자 계정 (ID: 1, 2, 3, 4 등)
- 테스트용 약속 데이터 (Meeting ID: 1, 2, 3 등)
- 카카오 로그인 및 동의 설정

### 3. 환경 설정
- JWT 토큰 발급 (카카오 로그인 후)
- 카카오 앱 키 설정 (`application.properties`)
- 데이터베이스 초기화

## 테스트 실행 방법

### 방법 1: 쉘 스크립트 자동 실행

```bash
# 기본 실행 (사용자 입력 받음)
./kakao-notification-test-script.sh

# 자동 모드 (기본값으로 실행)
./kakao-notification-test-script.sh --auto

# 특정 사용자와 약속으로 테스트
./kakao-notification-test-script.sh --auto --user-id 2 --meeting-id 5

# JWT 토큰 지정하여 실행
./kakao-notification-test-script.sh --token "eyJhbGci..." --user-id 1 --meeting-id 1

# 다른 서버 URL로 테스트
./kakao-notification-test-script.sh --url "http://localhost:9090" --auto
```

### 방법 2: HTTP 파일 사용 (VS Code/IntelliJ)

1. `kakao-notification-test.http` 파일 열기
2. 환경 변수 설정 (파일 상단)
   ```
   @baseUrl = http://localhost:8080
   @jwtToken = 실제_JWT_토큰_입력
   @userId = 1
   @meetingId = 1
   ```
3. 각 요청 옆의 "Send Request" 클릭

### 방법 3: Postman 사용

1. Postman에서 `kakao-notification-postman.json` 임포트
2. 환경 변수 설정:
   - `baseUrl`: http://localhost:8080
   - `jwtToken`: 실제 JWT 토큰
   - `userId`: 테스트할 사용자 ID
   - `meetingId`: 테스트할 약속 ID
3. 컬렉션 실행 (Runner 사용 권장)

## 테스트 시나리오

### 1. 기본 기능 테스트
- ✅ 서버 상태 확인
- ✅ 카카오 알림 사용 가능 여부 확인
- ✅ 특정 수신자들에게 알림 전송
- ✅ 전체 참여자에게 알림 전송
- ✅ 단일 수신자에게 알림 전송

### 2. 오류 상황 테스트
- ❌ 존재하지 않는 약속 ID
- ❌ 수신자 수 제한 초과 (20명 초과)
- ❌ 필수 필드 누락
- ❌ 잘못된 데이터 형식

### 3. 인증/인가 테스트
- 🔒 JWT 토큰 없이 요청 (401 예상)
- 🔒 사용자 ID 헤더 없이 요청 (401 예상)
- 🔒 잘못된 JWT 토큰 (401 예상)

### 4. 동의 관련 테스트
- ⚠️ 카카오 기능에 동의하지 않은 사용자 (409 예상)
- ⚠️ 카카오 정보가 없는 사용자

### 5. 성능 테스트
- 📊 연속 요청 처리 (스트레스 테스트)
- 📊 배치 처리 성능

## 예상 응답 형태

### 성공 응답
```json
{
  "success": true,
  "sentCount": 3,
  "totalCount": 3,
  "failed": [],
  "message": "모든 메시지가 성공적으로 전송되었습니다"
}
```

### 부분 성공 응답
```json
{
  "success": true,
  "sentCount": 2,
  "totalCount": 3,
  "failed": [
    {
      "userId": 4,
      "reason": "전송 조건 불충족 (동의 없음, 친구 아님, 카카오 정보 없음 등)"
    }
  ],
  "message": "2/3 메시지가 전송되었습니다"
}
```

### 동의 필요 오류 (409)
```json
{
  "success": false,
  "error": "CONSENT_REQUIRED",
  "message": "발송자가 카카오 기능 사용에 동의하지 않았습니다",
  "guide": "카카오 로그인 후 알림 전송 권한을 허용해주세요"
}
```

### 잘못된 파라미터 오류 (400)
```json
{
  "success": false,
  "error": "INVALID_PARAMETER",
  "message": "존재하지 않는 약속입니다: 99999"
}
```

## 트러블슈팅

### 1. 서버 연결 실패
- 서버가 실행 중인지 확인: `curl http://localhost:8080/api/health`
- 포트 번호 확인 (`application.properties`의 `server.port`)

### 2. 401 인증 오류
- JWT 토큰이 유효한지 확인
- 토큰 만료 시간 확인
- `X-User-ID` 헤더 포함 여부 확인

### 3. 409 동의 필요 오류
- 사용자의 카카오 로그인 상태 확인
- 카카오 기능 사용 동의 여부 확인
- 카카오 앱 키 설정 확인

### 4. 전송 실패 (sent_count = 0)
- 카카오 친구 관계 확인
- 수신자의 카카오 정보 등록 여부 확인
- 수신자의 카카오톡 메시지 수신 동의 여부 확인

### 5. 스크립트 실행 권한 오류
```bash
chmod +x kakao-notification-test-script.sh
```

## 로그 확인

### 애플리케이션 로그
- 카카오 알림 전송 과정의 상세 로그 확인
- `log.info`, `log.warn`, `log.error` 메시지 추적

### 스크립트 로그
- 컬러 코딩된 출력으로 결과 확인
- `[INFO]`, `[SUCCESS]`, `[WARNING]`, `[ERROR]` 태그 확인

## 추가 팁

### 1. 환경별 테스트
- 로컬 개발 환경: `http://localhost:8080`
- 개발 서버: `http://dev-server:8080`
- 스테이징 서버: `http://staging-server:8080`

### 2. 배치 테스트
```bash
# 여러 환경에서 연속 테스트
for env in local dev staging; do
  echo "Testing $env environment..."
  ./kakao-notification-test-script.sh --url "http://$env-server:8080" --auto
done
```

### 3. CI/CD 통합
```yaml
# GitHub Actions 예시
- name: Test Kakao Notification Service
  run: |
    ./kakao-notification-test-script.sh --auto --url ${{ secrets.TEST_SERVER_URL }}
```

### 4. 모니터링 연동
- 테스트 결과를 모니터링 시스템에 전송
- 실패 시 알림 설정
- 성능 지표 수집

## 주의사항

1. **실제 카카오톡 메시지 전송 주의**
   - 테스트 환경에서는 실제 메시지 전송이 발생할 수 있음
   - 필요시 Mock 서비스 사용 권장

2. **API 호출 제한**
   - 카카오 API 호출 제한 고려
   - 스트레스 테스트 시 간격 조정

3. **개인정보 보호**
   - 실제 사용자 데이터 사용 시 개인정보 보호 주의
   - 테스트 완료 후 로그 정리

4. **환경 설정**
   - 프로덕션 환경에서 테스트 금지
   - 테스트 전용 카카오 앱 사용 권장

---

**문의사항이나 개선 제안이 있으시면 개발팀에 연락해주세요!**


