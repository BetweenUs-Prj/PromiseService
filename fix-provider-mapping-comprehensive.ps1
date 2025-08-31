# 🚀 Provider ID 매핑 문제 종합 진단 및 해결 스크립트
# 이유: "Provider ID 매핑 실패" 에러의 모든 원인을 체계적으로 진단하고 해결하기 위해

Write-Host "=== 🚀 Provider ID 매핑 문제 종합 진단 및 해결 ===" -ForegroundColor Green
Write-Host ""

# 1. 환경변수 설정
$env:KAKAO_TEST_ACCESS_TOKEN="zAKnaPxezZrySUoDqgUfJ-lprgUfJ-lprgUeJW2FAAAAAQoNDF4AAAGY-lgt2-Q1KlcE_6bt"
Write-Host "✅ 카카오 액세스 토큰 설정 완료" -ForegroundColor Green

# 2. 데이터베이스 연결 정보 확인
Write-Host ""
Write-Host "=== 🔍 단계 1: 데이터베이스 연결 정보 확인 ===" -ForegroundColor Yellow
Write-Host "목적: 실행 중인 앱이 실제로 어떤 DB에 연결되어 있는지 확인" -ForegroundColor Cyan

try {
    $dsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/ds" -Method GET
    
    Write-Host "✅ 데이터소스 정보 조회 성공!" -ForegroundColor Green
    Write-Host "응답: $($dsResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 핵심 정보 추출
    $dbProduct = $dsResponse.databaseProductName
    $dbUrl = $dsResponse.url
    $hikariUrl = $dsResponse.hikariJdbcUrl
    
    Write-Host ""
    Write-Host "=== 🔍 데이터베이스 연결 정보 ===" -ForegroundColor Yellow
    Write-Host "데이터베이스: $dbProduct" -ForegroundColor Cyan
    Write-Host "연결 URL: $dbUrl" -ForegroundColor Cyan
    if ($hikariUrl) {
        Write-Host "HikariCP URL: $hikariUrl" -ForegroundColor Cyan
    }
    
} catch {
    Write-Host "❌ 데이터소스 정보 조회 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ 서버가 실행 중인지 확인해주세요." -ForegroundColor Yellow
    exit 1
}

# 3. 카카오 API 진단 (진짜 User ID 확인)
Write-Host ""
Write-Host "=== 🔍 단계 2: 카카오 API 진단 ===" -ForegroundColor Yellow
Write-Host "목적: 현재 토큰의 진짜 Kakao User ID와 앱 ID 확인" -ForegroundColor Cyan

try {
    # 3-1. 카카오 사용자 정보 조회
    Write-Host "📤 카카오 사용자 정보 조회 중..." -ForegroundColor Cyan
    $userInfoResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/kakao/user/me" -Method GET
    
    Write-Host "✅ 카카오 사용자 정보 조회 성공!" -ForegroundColor Green
    $actualKakaoId = $userInfoResponse.userInfo.id
    Write-Host "🔍 진짜 카카오 사용자 ID: $actualKakaoId" -ForegroundColor Yellow
    
    # 3-2. 카카오 액세스 토큰 정보 조회
    Write-Host "📤 카카오 액세스 토큰 정보 조회 중..." -ForegroundColor Cyan
    $tokenInfoResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/debug/kakao/user/access-token-info" -Method GET
    
    Write-Host "✅ 카카오 액세스 토큰 정보 조회 성공!" -ForegroundColor Green
    $appId = $tokenInfoResponse.tokenInfo.app_id
    Write-Host "🔍 카카오 앱 ID: $appId" -ForegroundColor Yellow
    
    # 3-3. ID 일치 여부 확인
    $expectedId = "4399968638"
    if ($actualKakaoId -eq $expectedId) {
        Write-Host "✅ 카카오 User ID 일치: $actualKakaoId" -ForegroundColor Green
    } else {
        Write-Host "❌ 카카오 User ID 불일치!" -ForegroundColor Red
        Write-Host "  - 헤더로 보내는 ID: $expectedId" -ForegroundColor Red
        Write-Host "  - 실제 토큰의 ID: $actualKakaoId" -ForegroundColor Red
        Write-Host "⚠️ 올바른 ID로 헤더를 보내거나 새로운 토큰을 발급받아주세요." -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ 카카오 API 진단 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ 토큰이 유효하지 않거나 네트워크 문제일 수 있습니다." -ForegroundColor Yellow
    exit 1
}

# 4. Provider ID 링크 (매핑 데이터 생성)
Write-Host ""
Write-Host "=== 🔗 단계 3: Provider ID 링크 ===" -ForegroundColor Yellow
Write-Host "목적: 진짜 카카오 사용자 ID와 내부 사용자 ID를 매핑" -ForegroundColor Cyan

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

# 5. 약속 생성 테스트 (문제 해결 확인)
Write-Host ""
Write-Host "=== 🧪 단계 4: 약속 생성 테스트 ===" -ForegroundColor Yellow
Write-Host "목적: Provider ID 매핑이 정상 작동하는지 확인" -ForegroundColor Cyan

$meetingRequest = @{
    title = "종합 진단 해결 테스트"
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
    
    Write-Host "🎉 성공! Provider ID 매핑 문제가 완전히 해결되었습니다!" -ForegroundColor Green
    Write-Host "응답: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
    
    # 약속 ID 추출 및 알림 결과 확인
    $meetingId = $response.id
    if ($meetingId) {
        Write-Host "📋 생성된 약속 ID: $meetingId" -ForegroundColor Green
        CheckNotificationResult $meetingId "종합 진단 해결"
    }
    
} catch {
    Write-Host "❌ 약속 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ 여전히 문제가 있습니다. 서버 로그를 확인해주세요." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== 🎯 종합 진단 및 해결 완료 ===" -ForegroundColor Green
Write-Host "서버 콘솔에서 다음 정보를 확인하세요:" -ForegroundColor Cyan
Write-Host "1. 🔍 데이터소스: 실행 중인 앱의 DB 연결 정보" -ForegroundColor Yellow
Write-Host "2. 🔍 카카오 API: 진짜 User ID와 앱 ID 확인" -ForegroundColor Yellow
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
