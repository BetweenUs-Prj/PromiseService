# 카카오톡 알림 시스템 설정 가이드

## 개요
약속 확정 시 카카오톡으로 알림을 전송하는 기능을 위한 설정 가이드입니다.

## 카카오 Developers 콘솔 설정

### 1. 애플리케이션 등록 및 설정

1. [카카오 Developers 콘솔](https://developers.kakao.com/) 접속
2. **내 애플리케이션** → **애플리케이션 추가하기**
3. 앱 정보 입력 후 생성

### 2. 플랫폼 설정

**Android/iOS 플랫폼 (선택사항)**
- **플랫폼** → **Android/iOS** 추가
- 패키지명/번들 ID 설정

**웹 플랫폼**
- **플랫폼** → **Web** 추가
- 사이트 도메인: `http://localhost:8080` (개발), `https://yourdomain.com` (운영)

### 3. 카카오 로그인 설정

**카카오 로그인 활성화**
- **제품 설정** → **카카오 로그인** → **활성화 설정** → **ON**

**Redirect URI 설정**
- **제품 설정** → **카카오 로그인** → **Redirect URI**
- 개발: `http://localhost:8080/login/oauth2/code/kakao`
- 운영: `https://yourdomain.com/login/oauth2/code/kakao`

**동의항목 설정** (필수)
- **제품 설정** → **카카오 로그인** → **동의항목**
- **닉네임**: 필수 동의
- **카카오계정(이메일)**: 필수 동의
- **카카오톡 메시지 전송**: **필수 동의** ⭐ (가장 중요)
- **카카오톡 채널 관계 확인 및 대화 전송**: 선택 동의 (친구 목록 조회용)

### 4. 카카오톡 메시지 설정

**메시지 템플릿 등록**
- **제품 설정** → **카카오톡 메시지** → **메시지 템플릿**
- **텍스트 템플릿** 추가:

```text
템플릿명: meeting_invitation
템플릿 내용:
🎉 #{inviter}님의 약속 초대

📋 제목: #{title}
📅 일시: #{date}
📍 장소: #{place}

💬 #{description}

버튼:
- 제목: "약속 확인하기"
- 링크: #{meetingUrl}
```

**메시지 API 설정**
- **제품 설정** → **카카오톡 메시지** → **설정**
- **메시지 API 사용**: **ON** ⭐

### 5. 테스트 사용자 등록

**개발 단계에서 필수**
- **앱 설정** → **테스트 앱** → **테스트 사용자**
- 개발자 본인과 테스트할 사용자들의 카카오계정 추가
- 운영배포 전까지는 등록된 테스트 사용자만 기능 사용 가능

## 애플리케이션 설정

### 1. application.properties 설정

```properties
# 카카오 API 설정
kakao.api.base-url=https://kapi.kakao.com
kakao.api.timeout=10
kakao.notification.batch-size=20

# 애플리케이션 기본 URL (카카오톡 메시지 링크용)
app.base-url=http://localhost:8080

# 카카오 OAuth 설정 (Spring Security OAuth2 사용 시)
spring.security.oauth2.client.registration.kakao.client-id=${KAKAO_CLIENT_ID}
spring.security.oauth2.client.registration.kakao.client-secret=${KAKAO_CLIENT_SECRET}
spring.security.oauth2.client.registration.kakao.scope=profile_nickname,account_email,talk_message,friends
spring.security.oauth2.client.registration.kakao.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.kakao.redirect-uri={baseUrl}/login/oauth2/code/kakao

spring.security.oauth2.client.provider.kakao.authorization-uri=https://kauth.kakao.com/oauth/authorize
spring.security.oauth2.client.provider.kakao.token-uri=https://kauth.kakao.com/oauth/token
spring.security.oauth2.client.provider.kakao.user-info-uri=https://kapi.kakao.com/v2/user/me
spring.security.oauth2.client.provider.kakao.user-name-attribute=id
```

### 2. 환경변수 설정

```bash
# 개발환경
export KAKAO_CLIENT_ID="your-kakao-app-key"
export KAKAO_CLIENT_SECRET="your-kakao-client-secret"

# Docker 환경
KAKAO_CLIENT_ID=your-kakao-app-key
KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

## API 사용 방법

### 1. 카카오톡 알림 전송

**엔드포인트**: `POST /api/notifications/kakao`

**헤더**:
```
Authorization: Bearer {JWT_TOKEN}
X-User-ID: {USER_ID}
Content-Type: application/json
```

**요청 본문**:
```json
{
  "meetingId": 123,
  "receiverIds": [45, 78]
}
```

**응답 예시**:
```json
{
  "success": true,
  "sentCount": 2,
  "totalCount": 2,
  "failed": [],
  "message": "모든 메시지가 성공적으로 전송되었습니다"
}
```

### 2. 전송 가능 여부 확인

**엔드포인트**: `GET /api/notifications/kakao/availability`

**응답 예시**:
```json
{
  "available": true,
  "hasConsent": true,
  "hasKakaoInfo": true,
  "message": "카카오톡 알림 전송이 가능합니다"
}
```

### 3. 테스트 전송

**엔드포인트**: `POST /api/notifications/kakao/test`

**파라미터**:
- `meetingId`: 약속 ID
- `receiverIds`: 수신자 ID 목록 (선택사항)

## Postman 테스트 예제

### 1. 카카오톡 알림 전송 테스트

```http
POST http://localhost:8080/api/notifications/kakao
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
  X-User-ID: 1
  Content-Type: application/json

Body:
{
  "meetingId": 123,
  "receiverIds": [2, 3, 4]
}
```

### 2. 전송 가능 여부 확인

```http
GET http://localhost:8080/api/notifications/kakao/availability
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
  X-User-ID: 1
```

## 문제 해결

### 1. 일반적인 오류

**401 Unauthorized**
- JWT 토큰이 없거나 만료됨
- `Authorization` 헤더 확인

**409 Conflict**
- 사용자가 카카오 기능에 동의하지 않음
- 카카오 로그인 후 권한 재요청 필요

**400 Bad Request**
- 요청 파라미터 오류
- `meetingId`, `receiverIds` 확인

### 2. 카카오 API 오류

**-401: Invalid access token**
- 카카오 액세스 토큰 만료
- 리프레시 토큰으로 재발급 필요

**-9798: 메시지 전송 권한 없음**
- 카카오 Developers 콘솔에서 `talk_message` 권한 확인
- 동의항목 설정 재확인

### 3. 친구 관계 문제

**전송 대상자가 없음**
- 발송자와 수신자가 서비스 내에서 친구 관계인지 확인
- 카카오 친구 매핑 정보 확인

### 4. 테스트 환경 제한

**테스트 사용자만 메시지 수신 가능**
- 카카오 Developers 콘솔 → 테스트 사용자 등록
- 앱 심사 완료 후 일반 사용자 사용 가능

## 배포 시 주의사항

### 1. 운영 환경 설정

- Redirect URI를 운영 도메인으로 변경
- HTTPS 필수 (카카오 정책)
- 환경변수로 민감 정보 관리

### 2. 앱 심사

- 카카오 앱 심사 통과 후 일반 사용자 사용 가능
- 개발 단계에서는 테스트 사용자만 사용 가능

### 3. 모니터링

- 카카오 API 호출량 모니터링
- 실패 로그 분석 및 대응

## 지원 및 문의

- [카카오 Developers 가이드](https://developers.kakao.com/docs)
- [카카오톡 메시지 API 문서](https://developers.kakao.com/docs/latest/ko/message/common)
- [카카오톡 친구 API 문서](https://developers.kakao.com/docs/latest/ko/kakaotalk-social/common)
