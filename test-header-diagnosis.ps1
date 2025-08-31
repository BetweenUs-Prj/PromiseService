# 🔍 헤더 진단 테스트 스크립트
# 이유: X-Provider-Id 헤더가 실제로 서버에 전달되는지 정확히 확인하기 위해

Write-Host "=== 🔍 헤더 진단 테스트 시작 ===" -ForegroundColor Green
Write-Host ""

# 1. 환경변수 설정
$env:KAKAO_TEST_ACCESS_TOKEN="zAKnaPxezZrySUoDqgUfJ-lprgUfJ-lprgUeJW2FAAAAAQoNDF4AAAGY-lgt2-Q1KlcE_6bt"
Write-Host "✅ 카카오 액세스 토큰 설정 완료" -ForegroundColor Green

# 2. 헤더 에코 테스트 (1분 증명용)
Write-Host ""
Write-Host "=== 🔍 테스트 1: 헤더 에코 엔드포인트 ===" -ForegroundColor Yellow
Write-Host "목적: 서버가 실제로 X-Provider-Id 헤더를 받는지 확인" -ForegroundColor Cyan

try {
    $echoResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/echo-headers" -Method GET -Headers @{
        "X-Provider-Id" = "kakao_4399986838"
    }
    
    Write-Host "✅ 헤더 에코 응답 성공!" -ForegroundColor Green
    Write-Host "응답: $($echoResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 핵심 진단 정보 확인
    $seenProviderId = $echoResponse.seenProviderId
    $springHasXProviderId = $echoResponse.springHasXProviderId
    
    Write-Host ""
    Write-Host "=== 🔍 진단 결과 ===" -ForegroundColor Yellow
    Write-Host "서블릿 레벨 X-Provider-Id: $seenProviderId" -ForegroundColor Cyan
    Write-Host "Spring HttpHeaders X-Provider-Id: $springHasXProviderId" -ForegroundColor Cyan
    
    if ($seenProviderId) {
        Write-Host "✅ 헤더는 들어옴 → 컨트롤러에서 읽는 로직만 손보면 됨" -ForegroundColor Green
    } else {
        Write-Host "❌ 헤더가 안 들어옴 → 도구/프록시/필터 문제" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ 헤더 에코 테스트 실패: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 실제 약속 생성 테스트 (X-Provider-Id만)
Write-Host ""
Write-Host "=== 🔍 테스트 2: X-Provider-Id만으로 약속 생성 ===" -ForegroundColor Yellow
Write-Host "목적: 개선된 컨트롤러가 Provider ID를 제대로 읽는지 확인" -ForegroundColor Cyan

$meetingRequest = @{
    title = "헤더 진단 테스트"
    meetingTime = "2025-08-30T21:00:00"
    locationName = "온라인"
    participants = @(1, 2, 3)
    sendNotification = $true
} | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/meetings" -Method POST -Headers @{
            "Content-Type" = "application/json"
            "X-Provider-Id" = "kakao_4399986838"
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

# 4. 다양한 헤더 이름으로 테스트
Write-Host ""
Write-Host "=== 🔍 테스트 3: 다양한 헤더 이름으로 테스트 ===" -ForegroundColor Yellow
Write-Host "목적: 별칭 허용 코드가 작동하는지 확인" -ForegroundColor Cyan

$providerHeaders = @(
    @{ "X-Provider-ID" = "kakao_4399986838" },      # 대문자 ID
    @{ "X-ProviderId" = "kakao_4399986838" }        # 카멜케이스
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

Write-Host ""
Write-Host "=== 🎯 진단 완료 ===" -ForegroundColor Green
Write-Host "서버 콘솔에서 다음 정보를 확인하세요:" -ForegroundColor Cyan
Write-Host "1. 🔍 SNIFF: 필터에서 헤더 감지 여부" -ForegroundColor Yellow
Write-Host "2. 🔍 헤더 에코: 서블릿/Spring 레벨 헤더 확인" -ForegroundColor Yellow
Write-Host "3. 🔍 약속 생성: Provider ID → User ID 매핑 성공 여부" -ForegroundColor Yellow

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
