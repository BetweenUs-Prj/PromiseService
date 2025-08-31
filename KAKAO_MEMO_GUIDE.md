# 카카오톡 "나와의 채팅" 방식 완전 구현 가이드

## 🎯 "나와의 채팅" 방식이란?

카카오톡 메시지 API는 두 가지 방식을 제공합니다:

### 1. 친구에게 보내기 (기존 방식)
- **API**: `/v1/api/talk/friends/message/default/send`
- **조건**: 발송자와 수신자가 카카오톡 친구 관계여야 함
- **제한**: 친구가 아니면 전송 불가 ❌

### 2. "나와의 채팅"으로 보내기 (새로운 방식) ✅
- **API**: `/v2/api/talk/memo/default/send`
- **조건**: 각 사용자가 본인의 카카오 로그인만 하면 됨
- **장점**: 친구 관계 불필요! 모든 참여자가 개별적으로 "나와의 채팅"으로 알림 수신

## 🔧 구현 방식

### 기존 vs 새로운 방식

#### ❌ 기존 (친구 방식)
```
발송자(A) → 친구(B,C,D)
조건: A와 B,C,D가 모두 친구 관계
```

#### ✅ 새로운 ("나와의 채팅" 방식)
```
참여자 A → A의 "나와의 채팅"
참여자 B → B의 "나와의 채팅"  
참여자 C → C의 "나와의 채팅"
참여자 D → D의 "나와의 채팅"

조건: 각자 본인의 카카오 로그인만 필요
```

## 📱 사용자 경험

### 약속에 초대된 모든 참여자는:
1. 각자 **본인의 카카오톡**에서
2. **"나와의 채팅"** 탭에서
3. **약속 알림**을 받게 됩니다!

### 메시지 예시:
```
🎉 김철수님의 약속 초대

📋 개발팀 회의
🕒 12월 20일(금) 14:00  
📍 회의실 A

약속 준비 완료! 😊

📱 상세보기: http://localhost:8080/meetings/123
```

## 🛠️ 기술적 구현

### 1. API 엔드포인트 변경
```properties
# application.properties
kakao.api.talk.memo=/v2/api/talk/memo/default/send
```

### 2. 토큰 수집 방식 변경
```java
// 기존: 발송자 한 명의 토큰으로 친구들에게 전송
String senderToken = getSenderToken(inviterId);

// 신규: 각 참여자의 개별 토큰으로 본인에게 전송
Map<Long, String> participantTokens = getTokensForEachUser(participants);
```

### 3. 전송 로직 변경
```java
// 기존: 친구 목록으로 전송
kakaoClient.sendToFriends(senderToken, friendUuids, template);

// 신규: 각자에게 메모 전송
kakaoClient.sendToMemo(participantTokens, template);
```

## 🚀 테스트 방법

### 1. 각 참여자의 카카오 로그인 필요
```bash
# 참여자 1의 토큰
export KAKAO_USER1_TOKEN="user1-access-token"

# 참여자 2의 토큰  
export KAKAO_USER2_TOKEN="user2-access-token"

# 테스트용으로는 동일한 토큰 사용 가능
export KAKAO_TEST_ACCESS_TOKEN="your-access-token"
```

### 2. API 테스트
```http
POST http://localhost:8080/api/notifications/kakao
X-User-ID: 1
Content-Type: application/json

{
  "meetingId": 1,
  "receiverIds": [2, 3, 4]
}
```

### 3. 결과 확인
- 각 참여자는 **본인의 카카오톡 > 나와의 채팅**에서 알림 확인
- 로그에서 `카카오 '나와의 채팅' 메시지 전송 성공` 확인

## 🎉 장점

1. **친구 관계 불필요**: 카카오톡 친구가 아니어도 알림 받을 수 있음
2. **개인정보 보호**: 다른 참여자의 연락처나 카카오 정보 노출 없음
3. **높은 전달률**: 친구 차단/삭제에 영향 받지 않음
4. **간편한 설정**: 각자 카카오 로그인만 하면 됨

## 🔍 로그 예시

### 성공 시:
```
[INFO] 카카오 '나와의 채팅' 메시지 전송 시작 - 대상: 3명
[DEBUG] 카카오 '나와의 채팅' 메시지 전송 성공 - 사용자 ID: 2
[DEBUG] 카카오 '나와의 채팅' 메시지 전송 성공 - 사용자 ID: 3
[DEBUG] 카카오 '나와의 채팅' 메시지 전송 성공 - 사용자 ID: 4
[INFO] 카카오 메모 전송 완료 - 성공: 3, 실패: 0
```

### 실패 시:
```
[WARN] KAKAO_TEST_ACCESS_TOKEN 환경변수가 설정되지 않았습니다.
[ERROR] 카카오 메모 API 호출 오류 - 사용자 ID: 2, 상태: 401, 응답: {"error":"invalid_token"}
```

## 🚨 주의사항

1. **각 사용자의 개별 토큰 필요**: 실제 서비스에서는 각 사용자의 카카오 액세스 토큰을 DB에 저장/관리해야 함
2. **토큰 만료 관리**: 액세스 토큰은 만료되므로 리프레시 토큰으로 갱신 필요
3. **동의 항목**: 카카오 개발자 콘솔에서 "카카오톡 메시지 전송" 권한을 **필수 동의**로 설정

---

**이제 친구 관계 없이도 모든 참여자가 "나와의 채팅"으로 약속 알림을 받을 수 있습니다! 🎊**


