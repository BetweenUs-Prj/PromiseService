# PromiseService API 문서

## 약속방 생성 API

### POST /api/meetings

약속방을 생성하고 친구들을 초대합니다.

#### 요청 헤더
```
X-User-ID: 123  // 방장의 사용자 ID
Content-Type: application/json
```

#### 요청 본문
```json
{
  "title": "카페에서 만나기",
  "description": "오후에 카페에서 수다 떨어봐요!",
  "meetingTime": "2024-01-15T14:00:00",
  "maxParticipants": 5,
  "locationName": "스타벅스 강남점",
  "locationAddress": "서울특별시 강남구 테헤란로 123",
  "locationCoordinates": "{\"latitude\": 37.5665, \"longitude\": 126.9780}",
  "participantUserIds": [456, 789, 101]
}
```

#### 응답 (201 Created)
```json
{
  "id": 1,
  "hostId": 123,
  "title": "카페에서 만나기",
  "description": "오후에 카페에서 수다 떨어봐요!",
  "meetingTime": "2024-01-15T14:00:00",
  "maxParticipants": 5,
  "status": "WAITING",
  "locationName": "스타벅스 강남점",
  "locationAddress": "서울특별시 강남구 테헤란로 123",
  "locationCoordinates": "{\"latitude\": 37.5665, \"longitude\": 126.9780}",
  "createdAt": "2024-01-10T10:00:00",
  "updatedAt": "2024-01-10T10:00:00",
  "participants": [
    {
      "id": 1,
      "userId": 123,
      "response": "ACCEPTED",
      "joinedAt": "2024-01-10T10:00:00",
      "invitedAt": "2024-01-10T10:00:00",
      "respondedAt": null
    },
    {
      "id": 2,
      "userId": 456,
      "response": "INVITED",
      "joinedAt": null,
      "invitedAt": "2024-01-10T10:00:00",
      "respondedAt": null
    }
  ]
}
```

## 약속방 조회 API

### GET /api/meetings/{meetingId}

특정 약속방의 상세 정보를 조회합니다.

#### 응답 (200 OK)
```json
{
  "id": 1,
  "hostId": 123,
  "title": "카페에서 만나기",
  "description": "오후에 카페에서 수다 떨어봐요!",
  "meetingTime": "2024-01-15T14:00:00",
  "maxParticipants": 5,
  "status": "WAITING",
  "locationName": "스타벅스 강남점",
  "locationAddress": "서울특별시 강남구 테헤란로 123",
  "locationCoordinates": "{\"latitude\": 37.5665, \"longitude\": 126.9780}",
  "createdAt": "2024-01-10T10:00:00",
  "updatedAt": "2024-01-10T10:00:00",
  "participants": [...]
}
```

## 방장의 약속 목록 조회 API

### GET /api/meetings/host/{hostId}

특정 사용자가 방장인 약속 목록을 조회합니다.

#### 응답 (200 OK)
```json
[
  {
    "id": 1,
    "hostId": 123,
    "title": "카페에서 만나기",
    "status": "WAITING",
    "meetingTime": "2024-01-15T14:00:00",
    "locationName": "스타벅스 강남점",
    "createdAt": "2024-01-10T10:00:00"
  }
]
```

## 참여자의 약속 목록 조회 API

### GET /api/meetings/participant/{userId}

특정 사용자가 참여한 약속 목록을 조회합니다.

## 약속 상태 관리 API

### PUT /api/meetings/{meetingId}/status

약속 상태를 변경합니다 (새로운 API).

#### 요청 헤더
```
X-User-ID: 123  // 방장의 사용자 ID
Content-Type: application/json
```

#### 요청 본문
```json
{
  "status": "CONFIRMED",
  "reason": "참여자들이 모두 수락했습니다"
}
```

#### 응답 (200 OK)
```json
{
  "meetingId": 1,
  "currentStatus": "CONFIRMED",
  "previousStatus": "WAITING",
  "reason": "참여자들이 모두 수락했습니다",
  "updatedBy": 123,
  "updatedAt": "2024-01-10T11:00:00"
}
```

### GET /api/meetings/{meetingId}/status/history

약속 상태 변경 히스토리를 조회합니다.

#### 응답 (200 OK)
```json
[
  {
    "id": 1,
    "action": "STATUS_CHANGED",
    "details": "상태 변경: WAITING → CONFIRMED (사유: 참여자들이 모두 수락했습니다)",
    "userId": 123,
    "timestamp": "2024-01-10T11:00:00"
  },
  {
    "id": 2,
    "action": "CREATED",
    "details": "약속방 생성: 카페에서 만나기",
    "userId": 123,
    "timestamp": "2024-01-10T10:00:00"
  }
]
```

## 약속 통계 API

### GET /api/meetings/statistics/status

약속 상태별 통계를 조회합니다.

#### 응답 (200 OK)
```json
{
  "waiting": 5,
  "confirmed": 3,
  "completed": 2,
  "cancelled": 1,
  "total": 11
}
```

### GET /api/meetings/statistics/status/{status}

특정 상태의 약속 목록을 조회합니다.

#### 응답 (200 OK)
```json
[
  {
    "id": 1,
    "title": "카페에서 만나기",
    "status": "CONFIRMED",
    "meetingTime": "2024-01-15T14:00:00"
  }
]
```

## 약속 상태 변경 규칙

### 상태 전환 규칙
- **WAITING** → **CONFIRMED**: 최소 2명 이상 참여, 미래 시간
- **WAITING** → **CANCELLED**: 언제든지 가능
- **CONFIRMED** → **COMPLETED**: 약속 시간 이후, 확정된 상태에서만
- **CONFIRMED** → **CANCELLED**: 언제든지 가능
- **COMPLETED** → 다른 상태: 변경 불가
- **CANCELLED** → **WAITING**: 되돌리기 가능

### 상태별 제약 조건
- **CONFIRMED**: 최소 참여자 2명, 미래 시간
- **COMPLETED**: 약속 시간 이후, CONFIRMED 상태에서만
- **CANCELLED**: COMPLETED 상태가 아닌 경우만

## 약속 삭제 API

### DELETE /api/meetings/{meetingId}

약속을 삭제합니다.

#### 요청 헤더
```
X-User-ID: 123  // 방장의 사용자 ID
```

#### 응답 (204 No Content)

## 에러 응답

### 400 Bad Request
```json
{
  "error": "약속 제목은 필수입니다"
}
```

### 403 Forbidden
```json
{
  "error": "약속 상태 변경 권한이 없습니다"
}
```

### 404 Not Found
```json
{
  "error": "약속을 찾을 수 없습니다: 999"
}
```

### 500 Internal Server Error
```json
{
  "error": "사용자 정보를 가져올 수 없습니다: 123"
}
```

## 친구 초대 API

### POST /api/meetings/{meetingId}/participants/invite

기존 약속방에 추가로 참여자를 초대합니다.

#### 요청 헤더
```
X-User-ID: 123  // 방장의 사용자 ID
Content-Type: application/json
```

#### 요청 본문
```json
{
  "participantUserIds": [789, 101, 202]
}
```

#### 응답 (200 OK)
```json
{
  "meetingId": 1,
  "successfullyInvited": [789, 101],
  "alreadyInvited": [202],
  "failedToInvite": [],
  "message": "성공적으로 초대된 사용자: 2명, 이미 초대된 사용자: 1명"
}
```

## 초대 응답 API

### PUT /api/meetings/{meetingId}/participants/respond?response=ACCEPTED

초대에 대한 응답을 처리합니다.

#### 요청 헤더
```
X-User-ID: 456  // 응답하는 사용자의 ID
```

#### 응답 (200 OK)
초대 응답이 성공적으로 처리되었습니다.

## 참여자 제거 API

### DELETE /api/meetings/{meetingId}/participants/{participantUserId}

약속방에서 참여자를 제거합니다 (방장만 가능).

#### 요청 헤더
```
X-User-ID: 123  // 방장의 사용자 ID
```

#### 응답 (204 No Content)
참여자가 성공적으로 제거되었습니다.

## 참여자 목록 조회 API

### GET /api/meetings/{meetingId}/participants

약속방의 참여자 목록을 조회합니다.

#### 응답 (200 OK)
```json
[
  {
    "id": 1,
    "userId": 123,
    "response": "ACCEPTED",
    "joinedAt": "2024-01-10T10:00:00",
    "invitedAt": "2024-01-10T10:00:00",
    "respondedAt": "2024-01-10T10:00:00"
  },
  {
    "id": 2,
    "userId": 456,
    "response": "INVITED",
    "joinedAt": null,
    "invitedAt": "2024-01-10T10:00:00",
    "respondedAt": null
  }
]
```

## 친구 목록 조회 API

### GET /api/users/{userId}/friends

특정 사용자의 친구 목록을 조회합니다.

#### 응답 (200 OK)
```json
[
  {
    "id": 456,
    "name": "김철수",
    "email": "kim@example.com",
    "provider": "KAKAO",
    "role": "USER"
  },
  {
    "id": 789,
    "name": "이영희",
    "email": "lee@example.com",
    "provider": "KAKAO",
    "role": "USER"
  }
]
```

## 약속 검색 및 필터링 API

### POST /api/meetings/search

고급 약속 검색을 수행합니다.

#### 요청 헤더
```
X-User-ID: 123
Content-Type: application/json
```

#### 요청 본문
```json
{
  "keyword": "카페",
  "status": "WAITING",
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-12-31T23:59:59",
  "locationName": "강남",
  "sortBy": "meetingTime",
  "sortOrder": "ASC",
  "page": 0,
  "size": 20
}
```

#### 응답 (200 OK)
```json
{
  "meetings": [
    {
      "id": 1,
      "hostId": 123,
      "title": "카페에서 만나기",
      "description": "오후에 카페에서 수다 떨어봐요!",
      "meetingTime": "2024-01-15T14:00:00",
      "maxParticipants": 5,
      "status": "WAITING",
      "locationName": "스타벅스 강남점",
      "locationAddress": "서울특별시 강남구 테헤란로 123",
      "createdAt": "2024-01-10T10:00:00",
      "updatedAt": "2024-01-10T10:00:00",
      "currentParticipantCount": 2,
      "isHost": true
    }
  ],
  "pageInfo": {
    "currentPage": 0,
    "totalPages": 5,
    "totalElements": 100,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  },
  "searchSummary": {
    "keyword": "카페",
    "status": "WAITING",
    "locationName": "강남",
    "hostId": null,
    "appliedFilters": 4,
    "sortBy": "meetingTime",
    "sortOrder": "ASC"
  }
}
```

### GET /api/meetings/search/keyword?q={keyword}&page={page}&size={size}

키워드로 약속을 검색합니다.

#### 응답 (200 OK)
위와 동일한 응답 구조

### GET /api/meetings/search/status/{status}?page={page}&size={size}

특정 상태의 약속을 검색합니다.

### GET /api/meetings/search/location?location={locationName}&page={page}&size={size}

장소명으로 약속을 검색합니다.

### GET /api/meetings/search/popular?limit={limit}

인기 약속을 조회합니다 (참여자 수 기준).

#### 응답 (200 OK)
```json
[
  {
    "id": 1,
    "title": "카페에서 만나기",
    "currentParticipantCount": 8,
    "isHost": false
  }
]
```

### GET /api/meetings/search/recent?limit={limit}

최근 생성된 약속을 조회합니다.

### GET /api/meetings/search/time?start={startTime}&end={endTime}&page={page}&size={size}

시간 범위로 약속을 검색합니다.

## 검색 및 필터링 옵션

### 검색 조건
- **keyword**: 제목, 설명에서 검색
- **status**: 약속 상태 필터 (WAITING, CONFIRMED, COMPLETED, CANCELLED)
- **startTime/endTime**: 약속 시간 범위
- **locationName**: 장소명 포함 검색
- **hostId**: 방장 ID 필터
- **participantUserIds**: 참여자 ID 목록 필터
- **minParticipants/maxParticipants**: 참여자 수 범위

### 정렬 옵션
- **sortBy**: meetingTime, createdAt, title
- **sortOrder**: ASC, DESC

### 페이지네이션
- **page**: 페이지 번호 (0부터 시작)
- **size**: 페이지 크기 (기본값: 20)

## 테스트 시나리오

### 1. 약속방 생성 테스트
1. 유효한 데이터로 약속방 생성
2. 필수 필드 누락 시 에러 확인
3. 최대 참여자 수 제한 확인

### 2. 친구 초대 테스트
1. 기존 약속방에 추가 참여자 초대
2. 이미 초대된 사용자 재초대 시도
3. 최대 참여자 수 초과 시 초대 실패 확인

### 3. 초대 응답 테스트
1. 초대받은 사용자의 수락/거절 처리
2. 이미 응답한 초대에 대한 재응답 시도

### 4. 약속 상태 관리 테스트
1. 유효한 상태 변경 (WAITING → CONFIRMED)
2. 유효하지 않은 상태 변경 (WAITING → COMPLETED)
3. 상태 변경 제약 조건 검사 (참여자 수, 시간 등)
4. 상태 변경 히스토리 기록 확인

### 5. 권한 테스트
1. 방장이 아닌 사용자의 상태 변경 시도
2. 방장이 아닌 사용자의 삭제 시도
3. 방장이 아닌 사용자의 참여자 제거 시도

### 6. UserService 연동 테스트
1. 존재하지 않는 사용자 ID로 초대 시도
2. UserService 연결 실패 시 에러 처리
3. 친구 목록 조회 성공/실패 케이스

### 7. 데이터 무결성 테스트
1. 약속 삭제 시 관련 데이터 정리 확인
2. 히스토리 기록 확인
3. 참여자 제거 시 관련 데이터 정리 확인

### 8. 통계 및 분석 테스트
1. 상태별 약속 수 통계 조회
2. 특정 상태의 약속 목록 조회
3. 상태 변경 히스토리 조회

### 9. 검색 및 필터링 테스트
1. 키워드 검색 기능
2. 상태별 필터링
3. 장소별 검색
4. 시간 범위 검색
5. 페이지네이션 동작 확인
6. 정렬 기능 확인

### 10. 알림 서비스 테스트
1. 약속 생성 시 초대 알림 전송
2. 약속 상태 변경 시 알림 전송
3. 약속 취소 시 알림 전송
4. 알림 전송 실패 시 에러 처리
5. 테스트 알림 전송 기능
6. 알림 서비스 헬스체크

## 알림 서비스 API

### POST /api/notifications/test

테스트용 알림을 전송합니다.

#### 요청 헤더
```
X-User-ID: 123
Content-Type: application/json
```

#### 요청 본문
```json
{
  "recipientUserIds": [456, 789],
  "title": "테스트 알림",
  "content": "이것은 테스트 알림입니다.",
  "type": "TEST",
  "meetingId": 1,
  "priority": "HIGH"
}
```

#### 응답 (200 OK)
```json
{
  "successfullyNotified": [456, 789],
  "failedToNotify": [],
  "sentAt": "2024-01-10T12:00:00",
  "message": "성공적으로 전송된 알림: 2건",
  "totalRecipients": 2,
  "successCount": 2,
  "failureCount": 0
}
```

### GET /api/notifications/health

알림 서비스의 상태를 확인합니다.

#### 응답 (200 OK)
```
알림 서비스가 정상적으로 동작 중입니다
```

## 알림 자동 전송 시나리오

### 1. 약속 생성 시
- **발송 대상**: 초대된 사용자들 (방장 제외)
- **알림 내용**: 새로운 약속 초대, 약속 시간, 장소 정보
- **알림 타입**: MEETING_INVITATION

### 2. 약속 상태 변경 시
- **발송 대상**: 모든 참여자 (방장 포함)
- **알림 내용**: 상태 변경 정보, 변경 사유, 약속 상세 정보
- **알림 타입**: 상태별로 다름 (CONFIRMED, COMPLETED, CANCELLED 등)

### 3. 약속 취소 시
- **발송 대상**: 모든 참여자 (방장 포함)
- **알림 내용**: 약속 취소 정보, 취소 사유
- **알림 타입**: MEETING_CANCELLED

## 알림 서비스 통합

### 외부 알림 서비스 연동
- **기본 URL**: http://localhost:8083
- **전송 API**: POST /api/notifications/send
- **에러 처리**: 알림 전송 실패 시에도 약속 기능은 정상 동작
- **재시도**: 수동 재전송 API 제공

### 알림 우선순위
- **HIGH**: 약속 관련 모든 알림
- **MEDIUM**: 일반 정보성 알림
- **LOW**: 마케팅 또는 광고성 알림
