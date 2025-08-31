# X-Provider-Id 헤더 디버깅 테스트
# 이유: 서버가 실제로 어떤 헤더를 받는지 확인하고 X-Provider-Id 인증이 작동하는지 테스트하기 위해

Write-Host "=== 🔍 X-Provider-Id 헤더 디버깅 테스트 ===" -ForegroundColor Green
Write-Host ""

# 1. 환경변수 설정
$env:KAKAO_TEST_ACCESS_TOKEN="zAKnaPxezZrySUoDqgUfJ-lprgUeJW2FAAAAAQoNDF4AAAGY-lgt2-Q1KlcE_6bt"
Write-Host "✅ 카카오 액세스 토큰 설정 완료" -ForegroundColor Green

# 2. X-Provider-Id만으로 약속 생성 테스트
Write-Host ""
Write-Host "=== 📅 테스트 1: X-Provider-Id만으로 약속 생성 ===" -ForegroundColor Yellow

$meetingRequest = @{
    title = "X-Provider-Id 디버깅 테스트"
    meetingTime = "2025-08-30T21:00:00"
    locationName = "온라인"
    participants = @(1, 2, 3)
    sendNotification = $true
} | ConvertTo-Json

Write-Host "📤 X-Provider-Id만으로 약속 생성 요청 전송 중..." -ForegroundColor Cyan
Write-Host "요청 데이터: $meetingRequest" -ForegroundColor Gray

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/meetings" -Method POST -Headers @{
        "Content-Type" = "application/json"
        "X-Provider-Id" = "4399986838"
    } -Body $meetingRequest
    
    Write-Host "✅ X-Provider-Id만으로 약속 생성 성공!" -ForegroundColor Green
    Write-Host "응답: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 약속 ID 추출 및 알림 결과 확인
    $meetingId = $response.id
    if ($meetingId) {
        Write-Host "📋 생성된 약속 ID: $meetingId" -ForegroundColor Green
        CheckNotificationResult $meetingId "X-Provider-Id만"
    }
    
} catch {
    Write-Host "❌ X-Provider-Id만으로 약속 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 다양한 헤더 이름으로 테스트
Write-Host ""
Write-Host "=== 📅 테스트 2: 다양한 헤더 이름으로 테스트 ===" -ForegroundColor Yellow

$providerHeaders = @(
    @{ "X-Provider-ID" = "4399986838" },      # 대문자 ID
    @{ "X-ProviderId" = "4399986838" },       # 카멜케이스
    @{ "x-provider-id" = "4399986838" }       # 소문자
)

foreach ($header in $providerHeaders) {
    $headerName = $header.Keys | Select-Object -First 1
    $headerValue = $header.Values | Select-Object -First 1
    
    Write-Host "📤 $headerName 방식으로 약속 생성 요청 전송 중... (값: $headerValue)" -ForegroundColor Cyan
    
    try {
        $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/meetings" -Method POST -Headers @{
            "Content-Type" = "application/json"
        } -Body $meetingRequest -Headers $header
        
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

# 4. X-User-Id와 X-Provider-Id 둘 다 있는 경우 테스트
Write-Host ""
Write-Host "=== 📅 테스트 3: X-User-Id와 X-Provider-Id 둘 다 있는 경우 ===" -ForegroundColor Yellow

try {
    $response3 = Invoke-RestMethod -Uri "http://localhost:8080/api/meetings" -Method POST -Headers @{
        "Content-Type" = "application/json"
        "X-User-Id" = "1"
        "X-Provider-Id" = "4399986838"
    } -Body $meetingRequest
    
    Write-Host "✅ X-User-Id와 X-Provider-Id 둘 다 있는 경우 약속 생성 성공!" -ForegroundColor Green
    Write-Host "응답: $($response3 | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 약속 ID 추출 및 알림 결과 확인
    $meetingId3 = $response3.id
    if ($meetingId3) {
        Write-Host "📋 생성된 약속 ID: $meetingId3" -ForegroundColor Green
        CheckNotificationResult $meetingId3 "X-User-Id와 X-Provider-Id 둘 다"
    }
    
} catch {
    Write-Host "❌ X-User-Id와 X-Provider-Id 둘 다 있는 경우 약속 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 🎯 테스트 완료 ===" -ForegroundColor Green
Write-Host "서버 콘솔에서 헤더 디버그 정보를 확인하세요!" -ForegroundColor Cyan
Write-Host "모든 테스트가 성공하면 X-Provider-Id 인증이 정상 작동하는 것입니다! 🎉" -ForegroundColor Green

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
