# 자동 카카오톡 알림 테스트 스크립트 (개선된 MeetingController 지원)
# 이유: 약속 생성 시 자동으로 카카오톡이 발송되는지 테스트하기 위해
# X-User-Id와 X-Provider-Id 두 가지 인증 방식 모두 테스트

Write-Host "=== 🚀 자동 카카오톡 알림 시스템 테스트 (개선된 컨트롤러) ===" -ForegroundColor Green
Write-Host ""

# 1. 환경변수 설정
$env:KAKAO_TEST_ACCESS_TOKEN="zAKnaPxezZrySUoDqgUfJ-lprgUeJW2FAAAAAQoNDF4AAAGY-lgt2-Q1KlcE_6bt"
Write-Host "✅ 카카오 액세스 토큰 설정 완료" -ForegroundColor Green

# 2. 약속 생성 테스트 (X-User-Id 방식)
Write-Host ""
Write-Host "=== 📅 약속 생성 테스트 1: X-User-Id 방식 ===" -ForegroundColor Yellow

$meetingRequest1 = @{
    title = "E2E 자동 알림 테스트 (X-User-Id)"
    meetingTime = "2025-08-30T21:00:00"
    locationName = "온라인"
    participants = @(1, 2, 3)
    sendNotification = $true
} | ConvertTo-Json

Write-Host "📤 X-User-Id 방식으로 약속 생성 요청 전송 중..." -ForegroundColor Cyan
Write-Host "요청 데이터: $meetingRequest1" -ForegroundColor Gray

try {
    $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/meetings" -Method POST -Headers @{
        "Content-Type" = "application/json"
        "X-User-Id" = "1"
    } -Body $meetingRequest1
    
    Write-Host "✅ X-User-Id 방식 약속 생성 성공!" -ForegroundColor Green
    Write-Host "응답: $($response1 | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 약속 ID 추출 및 알림 결과 확인
    $meetingId1 = $response1.id
    if ($meetingId1) {
        Write-Host "📋 생성된 약속 ID: $meetingId1" -ForegroundColor Green
        CheckNotificationResult $meetingId1 "X-User-Id 방식"
    }
    
} catch {
    Write-Host "❌ X-User-Id 방식 약속 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 약속 생성 테스트 (X-Provider-Id 방식 - 다양한 헤더 이름)
Write-Host ""
Write-Host "=== 📅 약속 생성 테스트 2: X-Provider-Id 방식 (다양한 헤더 이름) ===" -ForegroundColor Yellow

$meetingRequest2 = @{
    title = "E2E 자동 알림 테스트 (X-Provider-Id)"
    meetingTime = "2025-08-30T22:00:00"
    locationName = "오프라인"
    participants = @(2, 3, 4)
    sendNotification = $true
} | ConvertTo-Json

# 다양한 헤더 이름으로 테스트
$providerHeaders = @(
    @{ "X-Provider-Id" = "2" },      # 표준 형태
    @{ "X-Provider-ID" = "3" },      # 대문자 ID
    @{ "X-ProviderId" = "4" },       # 카멜케이스
    @{ "x-provider-id" = "5" }       # 소문자
)

foreach ($header in $providerHeaders) {
    $headerName = $header.Keys | Select-Object -First 1
    $headerValue = $header.Values | Select-Object -First 1
    
    Write-Host "📤 $headerName 방식으로 약속 생성 요청 전송 중... (값: $headerValue)" -ForegroundColor Cyan
    
    try {
        $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/meetings" -Method POST -Headers @{
            "Content-Type" = "application/json"
        } -Body $meetingRequest2 -Headers $header
        
        Write-Host "✅ $headerName 방식 약속 생성 성공!" -ForegroundColor Green
        Write-Host "응답: $($response2 | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
        
        # 약속 ID 추출 및 알림 결과 확인
        $meetingId2 = $response2.id
        if ($meetingId2) {
            Write-Host "📋 생성된 약속 ID: $meetingId2" -ForegroundColor Green
            CheckNotificationResult $meetingId2 "$headerName 방식"
            break  # 첫 번째 성공한 경우만 진행
        }
        
    } catch {
        Write-Host "❌ $headerName 방식 약속 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4. 인증 정보 없는 경우 테스트
Write-Host ""
Write-Host "=== 📅 약속 생성 테스트 3: 인증 정보 없음 (에러 케이스) ===" -ForegroundColor Yellow

$meetingRequest3 = @{
    title = "인증 정보 없는 테스트"
    meetingTime = "2025-08-30T23:00:00"
    locationName = "테스트"
    participants = @(1, 2)
    sendNotification = $true
} | ConvertTo-Json

Write-Host "📤 인증 정보 없이 약속 생성 요청 전송 중..." -ForegroundColor Cyan

try {
    $response3 = Invoke-RestMethod -Uri "http://localhost:8080/api/meetings" -Method POST -Headers @{
        "Content-Type" = "application/json"
    } -Body $meetingRequest3
    
    Write-Host "⚠️ 예상과 다름: 인증 정보 없이도 성공" -ForegroundColor Yellow
    
} catch {
    Write-Host "✅ 예상대로 실패: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== 🎯 테스트 완료 ===" -ForegroundColor Green
Write-Host "이제 카카오톡 > 나와의 채팅에서 약속 초대 메시지를 확인하세요!" -ForegroundColor Cyan
Write-Host "메시지가 도착했다면 자동 카카오톡 알림 시스템이 정상 작동하는 것입니다! 🎉" -ForegroundColor Green

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
