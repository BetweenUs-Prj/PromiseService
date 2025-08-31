# PromiseService - 약속 관리 서비스

## 개요
사용자들이 약속을 생성하고 관리할 수 있는 웹 서비스입니다. 카카오톡 알림 시스템을 통해 약속 확정 시 자동으로 참여자들에게 알림을 전송합니다.

## 주요 기능
- 📅 **약속 생성 및 관리**: 약속 생성, 참여, 상태 관리
- 👥 **참여자 관리**: 약속 참여자 초대 및 응답 관리
- 📱 **카카오톡 알림**: 약속 확정 시 카카오톡으로 자동 알림 전송
- 👫 **친구 시스템**: 웹 서비스 내 친구 관계 관리
- 🔔 **다양한 알림**: 카카오톡 알림, 푸시 알림, 알림톡 등 다중 알림 채널

## 기술 스택
- **Backend**: Spring Boot 3.x, Java 17
- **Database**: MySQL (운영), H2 (테스트)
- **ORM**: JPA/Hibernate
- **Authentication**: OAuth2 (카카오 로그인)
- **HTTP Client**: WebClient (카카오 API 호출)
- **Test**: JUnit 5, Mockito
- **Build**: Gradle

## 아키텍처
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UserService   │    │ PromiseService  │    │   KakaoAPI      │
│   (Port 8081)   │◄──►│   (Port 8080)   │◄──►│   (External)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 설치 및 실행

### 사전 요구사항
- Java 17+
- MySQL 8.0+
- Gradle 7.0+

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-org/PromiseService.git
cd PromiseService
```

### 2. 데이터베이스 설정
```sql
-- MySQL에서 데이터베이스 생성
CREATE DATABASE promise_service;
CREATE USER 'promise_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON promise_service.* TO 'promise_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 환경변수 설정
```bash
# application.properties 또는 환경변수로 설정
export KAKAO_CLIENT_ID="your-kakao-app-key"
export KAKAO_CLIENT_SECRET="your-kakao-client-secret"
export DB_URL="jdbc:mysql://localhost:3306/promise_service"
export DB_USERNAME="promise_user"
export DB_PASSWORD="password"
```

### 4. 빌드 및 실행
```bash
# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 또는 JAR 파일 실행
java -jar build/libs/PromiseService-0.0.1-SNAPSHOT.jar
```

## 카카오톡 알림 시스템 설정

### 카카오 Developers 콘솔 설정
카카오톡 알림 기능을 사용하려면 카카오 Developers 콘솔에서 다음 설정이 필요합니다:

1. **애플리케이션 등록**: [Kakao Developers](https://developers.kakao.com/)
2. **동의항목 설정** (필수):
   - `talk_message`: 카카오톡 메시지 전송 (필수)
   - `friends`: 카카오 친구 목록 조회 (선택)
3. **테스트 사용자 등록**: 개발 단계에서 필수

자세한 설정 방법은 [KAKAO_SETUP_GUIDE.md](./KAKAO_SETUP_GUIDE.md)를 참조하세요.

## API 사용법

### 카카오톡 알림 전송

**POST** `/api/notifications/kakao`

```bash
curl -X POST http://localhost:8080/api/notifications/kakao \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "X-User-ID: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "meetingId": 123,
    "receiverIds": [2, 3, 4]
  }'
```

**응답 예시**:
```json
{
  "success": true,
  "sentCount": 3,
  "totalCount": 3,
  "failed": [],
  "message": "모든 메시지가 성공적으로 전송되었습니다"
}
```

### 전송 가능 여부 확인

**GET** `/api/notifications/kakao/availability`

```bash
curl -X GET http://localhost:8080/api/notifications/kakao/availability \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "X-User-ID: 1"
```

### 기타 API
- **약속 관리**: `/api/meetings/*`
- **참여자 관리**: `/api/meetings/{id}/participants/*`
- **알림 관리**: `/api/notifications/*`

자세한 API 문서는 실행 후 `http://localhost:8080/swagger-ui.html`에서 확인할 수 있습니다.

## 데이터베이스 스키마

### 핵심 테이블
- `meeting`: 약속 정보
- `meeting_participant`: 약속 참여자
- `friends`: 사용자 간 친구 관계
- `user_kakao_info`: 사용자 카카오 연동 정보
- `user_consents`: 사용자 동의 정보
- `kakao_friend_map`: 카카오 친구 매핑

전체 ERD는 [docs/ERD.md](./docs/ERD.md)를 참조하세요.

## 테스트

### 단위 테스트 실행
```bash
./gradlew test
```

### 커버리지 리포트 생성
```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### API 테스트
Postman 컬렉션을 사용하여 API를 테스트할 수 있습니다:
```bash
# Postman 컬렉션 파일
./postman-collection.json
```

## 모니터링 및 로깅

### 헬스체크
```bash
# 전체 서비스 상태 확인
curl http://localhost:8080/actuator/health

# 알림 서비스 상태 확인
curl http://localhost:8080/api/notifications/health

# 카카오 알림 사용 가능 여부 확인
curl -H "X-User-ID: 1" http://localhost:8080/api/notifications/kakao/availability
```

### 로그 확인
```bash
# 애플리케이션 로그
tail -f logs/application.log

# 카카오 API 호출 로그 (DEBUG 레벨)
grep "KakaoClient" logs/application.log
```

## 트러블슈팅

### 자주 발생하는 문제

**1. 카카오톡 메시지 전송 실패**
- 카카오 Developers 콘솔에서 `talk_message` 권한 확인
- 테스트 사용자 등록 여부 확인
- 액세스 토큰 만료 여부 확인

**2. 친구 관계 없음**
- 발송자와 수신자가 서비스 내에서 친구인지 확인
- `friends` 테이블의 상태가 `ACCEPTED`인지 확인

**3. 동의 정보 없음**
- `user_consents` 테이블에서 `talk_message_consent` 확인
- 카카오 로그인 시 권한 동의 여부 확인

### 상태 코드별 대응

| 상태 코드 | 원인 | 해결 방법 |
|-----------|------|-----------|
| 400 | 잘못된 요청 | 요청 파라미터 확인 |
| 401 | 인증 실패 | JWT 토큰 확인 |
| 409 | 동의 없음 | 카카오 권한 재요청 |
| 500 | 서버 오류 | 로그 확인 및 재시도 |

## 배포

### Docker 배포
```bash
# Docker 이미지 빌드
docker build -t promise-service .

# 컨테이너 실행
docker run -p 8080:8080 \
  -e KAKAO_CLIENT_ID=your-app-key \
  -e KAKAO_CLIENT_SECRET=your-secret \
  promise-service
```

### 운영 환경 고려사항
- HTTPS 필수 (카카오 정책)
- 환경변수로 민감 정보 관리
- 카카오 API 호출량 모니터링
- 데이터베이스 커넥션 풀 설정

## 기여하기

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 라이선스
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 연락처
- **개발팀**: dev-team@company.com
- **이슈 리포트**: [GitHub Issues](https://github.com/your-org/PromiseService/issues)
- **문서**: [Project Wiki](https://github.com/your-org/PromiseService/wiki)