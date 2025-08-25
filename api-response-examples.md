# API JSON 응답 예시

> **이유**: API 테스트 시 예상되는 JSON 응답 형태를 미리 확인하여 클라이언트 개발과 테스트 케이스 작성에 활용

## 1. 약속 생성 성공 응답

```json
{
  "id": 1,
  "hostId": 123,
  "title": "JSON 테스트 약속",
  "description": "curl로 테스트하는 약속",
  "meetingTime": "2025-08-20T14:00:00",
  "maxParticipants": 5,
  "status": "WAITING",
  "locationName": "강남역",
  "locationAddress": "서울시 강남구 강남대로",
  "locationCoordinates": "{\"lat\": 37.498095, \"lng\": 127.027621}",
  "participants": [
    {
      "id": 1,
      "userId": 123,
      "response": "ACCEPTED",
      "joinedAt": "2025-08-19T14:31:00"
    },
    {
      "id": 2,
      "userId": 456,
      "response": "INVITED",
      "joinedAt": null
    },
    {
      "id": 3,
      "userId": 789,
      "response": "INVITED",
      "joinedAt": null
    }
  ],
  "createdAt": "2025-08-19T14:31:00",
  "updatedAt": "2025-08-19T14:31:00"
}
```

## 2. 약속 목록 조회 응답

```json
[
  {
    "id": 1,
    "hostId": 123,
    "title": "JSON 테스트 약속",
    "description": "curl로 테스트하는 약속",
    "meetingTime": "2025-08-20T14:00:00",
    "status": "WAITING",
    "locationName": "강남역",
    "participantCount": 3,
    "maxParticipants": 5,
    "createdAt": "2025-08-19T14:31:00"
  },
  {
    "id": 2,
    "hostId": 123,
    "title": "두 번째 약속",
    "description": "또 다른 테스트 약속",
    "meetingTime": "2025-08-21T15:00:00",
    "status": "CONFIRMED",
    "locationName": "홍대입구역",
    "participantCount": 4,
    "maxParticipants": 6,
    "createdAt": "2025-08-19T15:00:00"
  }
]
```

## 3. 참여자 초대 성공 응답

```json
{
  "success": true,
  "message": "참여자 초대가 완료되었습니다",
  "invitedUsers": [
    {
      "userId": 999,
      "status": "INVITED"
    },
    {
      "userId": 888,
      "status": "INVITED"
    }
  ],
  "totalParticipants": 5,
  "maxParticipants": 5
}
```

## 4. 약속 상태 변경 성공 응답

```json
{
  "meetingId": 1,
  "previousStatus": "WAITING",
  "newStatus": "CONFIRMED",
  "message": "약속이 확정되었습니다!",
  "statusHistory": [
    {
      "status": "WAITING",
      "changedAt": "2025-08-19T14:31:00",
      "changedBy": 123,
      "message": "약속이 생성되었습니다"
    },
    {
      "status": "CONFIRMED",
      "changedAt": "2025-08-19T15:30:00",
      "changedBy": 123,
      "message": "약속이 확정되었습니다!"
    }
  ],
  "statistics": {
    "totalMeetings": 10,
    "confirmedMeetings": 6,
    "waitingMeetings": 3,
    "cancelledMeetings": 1
  }
}
```

## 5. 약속 검색 결과 응답

```json
{
  "content": [
    {
      "id": 1,
      "title": "강남역 모임",
      "locationName": "강남역",
      "meetingTime": "2025-08-20T14:00:00",
      "status": "WAITING",
      "participantCount": 3,
      "maxParticipants": 5
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "by": "MEETING_TIME",
      "direction": "ASC"
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1
}
```

## 6. 에러 응답 예시

### Validation 오류 (400 Bad Request)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2025-08-19T14:31:00",
  "path": "/api/meetings",
  "details": [
    {
      "field": "title",
      "rejectedValue": null,
      "message": "제목은 필수입니다"
    },
    {
      "field": "meetingTime",
      "rejectedValue": "2025-08-10T14:00:00",
      "message": "미래 시간이어야 합니다"
    }
  ]
}
```

### 권한 오류 (403 Forbidden)
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "약속 수정 권한이 없습니다",
  "timestamp": "2025-08-19T14:31:00",
  "path": "/api/meetings/1"
}
```

### 리소스 없음 (404 Not Found)
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "약속을 찾을 수 없습니다: 999",
  "timestamp": "2025-08-19T14:31:00",
  "path": "/api/meetings/999"
}
```

## 7. 알림 전송 응답

```json
{
  "notificationId": "notif_12345",
  "success": true,
  "message": "알림이 성공적으로 전송되었습니다",
  "sentTo": [456, 789],
  "failedRecipients": [],
  "timestamp": "2025-08-19T14:31:00"
}
```






