# 카카오톡 친구 초대 기능 설정 가이드

## 개요
이 문서는 카카오톡 친구 초대 기능을 사용하기 위해 Kakao Developers 콘솔에서 필요한 설정을 안내합니다.

## 전제 조건
- Kakao Developers 콘솔에서 앱이 생성되어 있어야 함
- 카카오 로그인이 이미 구현되어 있어야 함 (`talk_message` 동의는 활성화되어 있음)

## 1. 동의항목 설정

### 1.1 friends 동의항목 활성화
1. [Kakao Developers 콘솔](https://developers.kakao.com/) 접속
2. 해당 앱 선택 → **제품 설정** → **카카오 로그인** → **동의항목**
3. `friends` 동의항목 활성화:
   - **앱 친구 목록**: 필수 동의 또는 선택 동의로 설정
   - **설명**: "친구를 초대하여 함께 약속을 만들 수 있습니다"
   - **항목 고유 ID**: `friends`

### 1.2 talk_message 동의항목 확인
- `talk_message` 동의항목이 이미 활성화되어 있는지 확인
- **카카오톡 메시지 전송**: 필수 동의 또는 선택 동의로 설정되어 있어야 함

## 2. 개발/테스트 환경 설정

### 2.1 테스트 계정 등록 (중요!)
개발 단계에서는 반드시 **테스트 계정**을 등록해야 합니다.

1. Kakao Developers 콘솔 → **앱 설정** → **카카오 로그인** → **테스트 계정**
2. **보내는 사람(초대자)** 카카오 계정 추가
3. **받는 사람(피초대자)** 카카오 계정 추가
4. 양쪽 모두 등록되어야 친구 목록 조회 및 메시지 전송이 가능

### 2.2 앱 친구 관계 설정
- 테스트 계정들이 서로 카카오톡에서 친구여야 함
- 각 테스트 계정으로 최소 한 번씩 앱에 로그인하여 `friends` 동의를 완료해야 함

## 3. API 권한 확인

### 3.1 필요한 Scope
```
- openid (기본)
- profile_nickname (기본)
- friends (새로 추가)
- talk_message (기존)
```

### 3.2 Redirect URI 설정
카카오 로그인 시 사용할 Redirect URI가 등록되어 있는지 확인:
```
http://localhost:8080/login/oauth2/code/kakao (개발환경)
https://yourdomain.com/login/oauth2/code/kakao (운영환경)
```

## 4. 테스트 시나리오

### 4.1 성공 케이스
1. 테스트 계정 A로 앱 로그인 (friends 동의 완료)
2. `GET /api/kakao/friends` 호출 → 앱을 사용하는 친구 목록 반환
3. 테스트 계정 B가 목록에 포함되어 있는지 확인
4. `POST /api/kakao/invites` 호출 → 테스트 계정 B에게 메시지 전송
5. 테스트 계정 B의 카카오톡에서 메시지 수신 확인

### 4.2 실패 케이스 확인
- `friends` 동의 미완료 시 → 빈 친구 목록 반환
- 테스트 계정 미등록 시 → 403 Forbidden 오류
- 카카오 토큰 만료 시 → 401 Unauthorized 오류

## 5. 운영환경 배포 전 체크리스트

- [ ] `friends` 동의항목이 필수/선택 동의로 설정됨
- [ ] `talk_message` 동의항목이 활성화됨
- [ ] 운영 도메인의 Redirect URI가 등록됨
- [ ] 테스트 계정으로 전체 플로우 검증 완료
- [ ] 앱 심사 진행 (필요 시)

## 6. 문제 해결

### 친구 목록이 비어있는 경우
1. 친구가 해당 앱을 사용한 적이 있는지 확인
2. 친구가 `friends` 동의를 완료했는지 확인
3. 테스트 계정이 올바르게 등록되었는지 확인

### 메시지 전송이 실패하는 경우
1. `talk_message` 동의가 완료되었는지 확인
2. 카카오 액세스 토큰이 유효한지 확인
3. 수신자 UUID가 올바른지 확인

## 참고 문서
- [카카오 로그인 개발가이드](https://developers.kakao.com/docs/latest/ko/kakaologin/common)
- [카카오톡 메시지 API](https://developers.kakao.com/docs/latest/ko/message/common)
- [친구 목록 가져오기](https://developers.kakao.com/docs/latest/ko/kakaotalk-social/friends)













