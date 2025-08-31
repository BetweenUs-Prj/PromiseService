#!/bin/bash

# API JSON 테스트 스크립트
# 이유: REST API의 JSON 요청/응답을 직접 테스트하여 실제 API 동작을 검증하기 위해

BASE_URL="http://localhost:8080"
USER_ID="123"

echo "🚀 Promise Service API JSON 테스트 시작"

# 1. 약속 생성 API 테스트
echo ""
echo "📝 1. 약속 생성 API 테스트"
curl -X POST "$BASE_URL/api/meetings" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: $USER_ID" \
  -d '{
    "title": "JSON 테스트 약속",
    "description": "curl로 테스트하는 약속",
    "meetingTime": "2025-08-20T14:00:00",
    "maxParticipants": 5,
    "locationName": "강남역",
    "locationAddress": "서울시 강남구 강남대로",
    "locationCoordinates": "{\"lat\": 37.498095, \"lng\": 127.027621}",
    "participantUserIds": [456, 789]
  }' \
  | jq '.'

# 2. 약속 목록 조회 API 테스트
echo ""
echo "📋 2. 약속 목록 조회 API 테스트"
curl -X GET "$BASE_URL/api/meetings/host" \
  -H "X-User-ID: $USER_ID" \
  | jq '.'

# 3. 약속 상세 조회 API 테스트
echo ""
echo "🔍 3. 약속 상세 조회 API 테스트"
curl -X GET "$BASE_URL/api/meetings/1" \
  -H "X-User-ID: $USER_ID" \
  | jq '.'

# 4. 참여자 초대 API 테스트
echo ""
echo "👥 4. 참여자 초대 API 테스트"
curl -X POST "$BASE_URL/api/meetings/1/participants/invite" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: $USER_ID" \
  -d '{
    "userIds": [999, 888]
  }' \
  | jq '.'

# 5. 약속 상태 변경 API 테스트
echo ""
echo "⚡ 5. 약속 상태 변경 API 테스트"
curl -X PUT "$BASE_URL/api/meetings/1/status" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: $USER_ID" \
  -d '{
    "status": "CONFIRMED",
    "message": "모든 인원이 확정되었습니다!"
  }' \
  | jq '.'

echo ""
echo "✅ API JSON 테스트 완료!"

























