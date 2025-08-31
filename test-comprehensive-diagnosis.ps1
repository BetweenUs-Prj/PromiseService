# 🔍 종합 진단 및 해결 스크립트
# 이유: X-Provider-Id 인증 문제를 단계별로 진단하고 해결하기 위해

Write-Host "=== 🔍 종합 진단 및 해결 시작 ===" -ForegroundColor Green
Write-Host ""

# 1. 환경변수 설정
$env:KAKAO_TEST_ACCESS_TOKEN="zAKnaPxezZrySUoDqgUfJ-lprgUfJ-lprgUeJW2FAAAAAQoNDF4AAAGY-lgt2-Q1KlcE_6bt"
Write-Host "✅ 카카오 액세스 토큰 설정 완료" -ForegroundColor Green

# 2. 카카오 API 진단
Write-Host ""
Write-Host "=== 🔍 단계 1: 카카오 API 진단 ===" -ForegroundColor Yellow
Write-Host "목적: 현재 토큰의 유효성과 실제 사용자 ID 확인" -ForegroundColor Cyan

try {
    # 2-1. 카카오 사용자 정보 조회
    Write-Host "📤 카카오 사용자 정보 조회 중..." -ForegroundColor Cyan
    $userInfoResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/kakao/user/me" -Method GET
    
    Write-Host "✅ 카카오 사용자 정보 조회 성공!" -ForegroundColor Green
    Write-Host "응답: $($userInfoResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 실제 카카오 사용자 ID 추출
    $actualKakaoId = $userInfoResponse.userInfo.id
    Write-Host "🔍 실제 카카오 사용자 ID: $actualKakaoId" -ForegroundColor Yellow
    
    # 2-2. 카카오 액세스 토큰 정보 조회
    Write-Host "📤 카카오 액세스 토큰 정보 조회 중..." -ForegroundColor Cyan
    $tokenInfoResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/kakao/user/access-token-info" -Method GET
    
    Write-Host "✅ 카카오 액세스 토큰 정보 조회 성공!" -ForegroundColor Green
    Write-Host "응답: $($tokenInfoResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 앱 ID 확인
    $appId = $tokenInfoResponse.tokenInfo.app_id
    Write-Host "🔍 카카오 앱 ID: $appId" -ForegroundColor Yellow
    
} catch {
    Write-Host "❌ 카카오 API 진단 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ 토큰이 유효하지 않거나 네트워크 문제일 수 있습니다" -ForegroundColor Yellow
}

# 3. 헤더 전달 진단
Write-Host ""
Write-Host "=== 🔍 단계 2: 헤더 전달 진단 ===" -ForegroundColor Yellow
Write-Host "목적: X-Provider-Id 헤더가 서버까지 전달되는지 확인" -ForegroundColor Cyan

try {
    $echoResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/echo-headers" -Method GET -Headers @{
        "X-Provider-Id" = "kakao_$actualKakaoId"
    }
    
    Write-Host "✅ 헤더 에코 응답 성공!" -ForegroundColor Green
    Write-Host "응답: $($echoResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 핵심 진단 정보 확인
    $seenProviderId = $echoResponse.seenProviderId
    $springHasXProviderId = $echoResponse.springHasXProviderId
    
    Write-Host ""
    Write-Host "=== 🔍 헤더 전달 진단 결과 ===" -ForegroundColor Yellow
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

# 4. Provider ID 링크 (빠른 우회)
Write-Host ""
Write-Host "=== 🔍 단계 3: Provider ID 링크 ===" -ForegroundColor Yellow
Write-Host "목적: 실제 카카오 사용자 ID와 내부 사용자 ID를 매핑" -ForegroundColor Cyan

try {
    $linkResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/link-provider" -Method POST -Headers @{
        "X-User-Id" = "1"
        "X-Provider-Id" = "kakao_$actualKakaoId"
    }
    
    Write-Host "✅ Provider ID 링크 성공!" -ForegroundColor Green
    Write-Host "응답: $($linkResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
} catch {
    Write-Host "❌ Provider ID 링크 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ 이 단계는 선택사항입니다. DB에 직접 매핑 데이터를 입력할 수도 있습니다" -ForegroundColor Yellow
}

# 5. 실제 약속 생성 테스트
Write-Host ""
Write-Host "=== 🔍 단계 4: 실제 약속 생성 테스트 ===" -ForegroundColor Yellow
Write-Host "목적: Provider ID 인증이 정상 작동하는지 확인" -ForegroundColor Cyan

$meetingRequest = @{
    title = "종합 진단 테스트"
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
    
    Write-Host "✅ X-Provider-Id만으로 약속 생성 성공!" -ForegroundColor Green
    Write-Host "응답: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 약속 ID 추출 및 알림 결과 확인
    $meetingId = $response.id
    if ($meetingId) {
        Write-Host "📋 생성된 약속 ID: $meetingId" -ForegroundColor Green
        CheckNotificationResult $meetingId "종합 진단"
    }
    
} catch {
    Write-Host "❌ 약속 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ Provider ID 매핑이나 다른 문제가 있을 수 있습니다" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== 🎯 종합 진단 완료 ===" -ForegroundColor Green
Write-Host "서버 콘솔에서 다음 정보를 확인하세요:" -ForegroundColor Cyan
Write-Host "1. 🔍 카카오 API: 실제 사용자 ID와 앱 ID 확인" -ForegroundColor Yellow
Write-Host "2. 🔍 헤더 전달: X-Provider-Id 헤더 감지 여부" -ForegroundColor Yellow
Write-Host "3. 🔍 Provider ID 링크: 매핑 데이터 생성 여부" -ForegroundColor Yellow
Write-Host "4. 🔍 약속 생성: Provider ID → User ID 매핑 성공 여부" -ForegroundColor Yellow

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
