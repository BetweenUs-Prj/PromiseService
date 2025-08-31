# 🚀 Provider ID 매핑 문제 즉시 해결 스크립트
# 이유: "Provider ID 매핑 실패: 4399968638" 에러를 즉시 해결하기 위해

Write-Host "=== 🚀 Provider ID 매핑 문제 즉시 해결 ===" -ForegroundColor Green
Write-Host ""

# 1. 환경변수 설정
$env:KAKAO_TEST_ACCESS_TOKEN="zAKnaPxezZrySUoDqgUfJ-lprgUeJW2FAAAAAQoNDF4AAAGY-lgt2-Q1KlcE_6bt"
Write-Host "✅ 카카오 액세스 토큰 설정 완료" -ForegroundColor Green

# 2. 카카오 사용자 정보 조회 (실제 ID 확인)
Write-Host ""
Write-Host "=== 🔍 단계 1: 실제 카카오 사용자 ID 확인 ===" -ForegroundColor Yellow

try {
    $userInfoResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/kakao/user/me" -Method GET
    
    Write-Host "✅ 카카오 사용자 정보 조회 성공!" -ForegroundColor Green
    $actualKakaoId = $userInfoResponse.userInfo.id
    Write-Host "🔍 실제 카카오 사용자 ID: $actualKakaoId" -ForegroundColor Yellow
    
} catch {
    Write-Host "❌ 카카오 사용자 정보 조회 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ 토큰이 유효하지 않습니다. 새로운 토큰을 발급받아주세요." -ForegroundColor Yellow
    exit 1
}

# 3. Provider ID 링크 (매핑 데이터 생성)
Write-Host ""
Write-Host "=== 🔗 단계 2: Provider ID 링크 ===" -ForegroundColor Yellow
Write-Host "목적: 카카오 사용자 ID와 내부 사용자 ID 1을 매핑" -ForegroundColor Cyan

try {
    $linkResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/link-provider" -Method POST -Headers @{
        "X-User-Id" = "1"
        "X-Provider-Id" = "kakao_$actualKakaoId"
    }
    
    Write-Host "✅ Provider ID 링크 성공!" -ForegroundColor Green
    Write-Host "응답: $($linkResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    Write-Host "🔗 매핑 완료: 내부 사용자 ID 1 ↔ 카카오 사용자 ID $actualKakaoId" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Provider ID 링크 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ 서버를 재시작했는지 확인해주세요." -ForegroundColor Yellow
    exit 1
}

# 4. 약속 생성 테스트 (문제 해결 확인)
Write-Host ""
Write-Host "=== 🧪 단계 3: 약속 생성 테스트 ===" -ForegroundColor Yellow
Write-Host "목적: Provider ID 매핑이 정상 작동하는지 확인" -ForegroundColor Cyan

$meetingRequest = @{
    title = "매핑 해결 테스트"
    meetingTime = "2025-08-30T21:00:00"
    locationName = "온라인"
    participants = @(1, 2, 3)
    sendNotification = $true
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/meetings" -Method POST -Headers @{
        "Content-Type" = "application/json"
        "X-Provider-Id" = "kakao_$actualKakaoId"
    } -Body $meetingRequest
    
    Write-Host "🎉 성공! Provider ID 매핑 문제가 해결되었습니다!" -ForegroundColor Green
    Write-Host "응답: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 약속 ID 추출 및 알림 결과 확인
    $meetingId = $response.id
    if ($meetingId) {
        Write-Host "📋 생성된 약속 ID: $meetingId" -ForegroundColor Green
        CheckNotificationResult $meetingId "매핑 해결 테스트"
    }
    
} catch {
    Write-Host "❌ 약속 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ 여전히 문제가 있습니다. 서버 로그를 확인해주세요." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== 🎯 해결 완료 ===" -ForegroundColor Green
Write-Host "이제 X-Provider-Id: kakao_$actualKakaoId 로 약속을 생성할 수 있습니다!" -ForegroundColor Cyan

# 알림 결과 확인 함수
function CheckNotificationResult {
    param($meetingId, $testType)
    
    Write-Host ""
    Write-Host "=== 📊 알림 전송 결과 확인 ($testType) ===" -ForegroundColor Yellow
    
    Start-Sleep -Seconds 3  # 카카오톡 전송 완료 대기
    
    try {
        $notificationResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/notifications/meeting/$meetingId" -Method GET
        Write-Host "✅ 알림 전송 결과 조회 성공!" -ForegroundColor Green
        Write-Host "알림 통계: $($notificationResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    } catch {
        Write-Host "⚠️ 알림 전송 결과 조회 실패: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}
