# 카카오톡 약속 알림 서비스 테스트 스크립트 (PowerShell)
# 이유: Windows 환경에서 카카오톡 알림 기능이 정상적으로 작동하는지 다양한 시나리오로 테스트하기 위해

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$JwtToken = "",
    [string]$UserId = "1",
    [string]$MeetingId = "1",
    [switch]$Auto,
    [switch]$Help
)

# 색상 정의
$Colors = @{
    Info = "Cyan"
    Success = "Green" 
    Warning = "Yellow"
    Error = "Red"
    Section = "Magenta"
}

# 로그 함수들
function Write-InfoLog($Message) {
    Write-Host "[INFO] $Message" -ForegroundColor $Colors.Info
}

function Write-SuccessLog($Message) {
    Write-Host "[SUCCESS] $Message" -ForegroundColor $Colors.Success
}

function Write-WarningLog($Message) {
    Write-Host "[WARNING] $Message" -ForegroundColor $Colors.Warning
}

function Write-ErrorLog($Message) {
    Write-Host "[ERROR] $Message" -ForegroundColor $Colors.Error
}

function Write-SectionLog($Message) {
    Write-Host ""
    Write-Host "=== $Message ===" -ForegroundColor $Colors.Section
    Write-Host ""
}

# HTTP 요청 함수
function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    try {
        $Uri = "$BaseUrl$Endpoint"
        
        # 기본 헤더 설정
        if ($JwtToken) {
            $Headers["Authorization"] = "Bearer $JwtToken"
        }
        if ($UserId) {
            $Headers["X-User-ID"] = $UserId
        }
        
        $RequestParams = @{
            Uri = $Uri
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $RequestParams["Body"] = $Body
        }
        
        $Response = Invoke-RestMethod @RequestParams
        return @{
            Success = $true
            Data = $Response
            StatusCode = 200
        }
    }
    catch {
        $StatusCode = if ($_.Exception.Response) { 
            [int]$_.Exception.Response.StatusCode 
        } else { 
            0 
        }
        
        $ErrorMessage = if ($_.Exception.Response) {
            try {
                $ErrorStream = $_.Exception.Response.GetResponseStream()
                $Reader = New-Object System.IO.StreamReader($ErrorStream)
                $ErrorContent = $Reader.ReadToEnd()
                $Reader.Close()
                $ErrorContent
            }
            catch {
                $_.Exception.Message
            }
        } else {
            $_.Exception.Message
        }
        
        return @{
            Success = $false
            Error = $ErrorMessage
            StatusCode = $StatusCode
        }
    }
}

# JSON 값 추출 함수
function Get-JsonValue {
    param(
        [object]$JsonObject,
        [string]$Key
    )
    
    if ($JsonObject -and $JsonObject.PSObject.Properties[$Key]) {
        return $JsonObject.$Key
    }
    return $null
}

# 서버 상태 확인
function Test-ServerHealth {
    Write-SectionLog "서버 상태 확인"
    
    $Result = Invoke-ApiRequest -Method "GET" -Endpoint "/api/health"
    
    if ($Result.Success) {
        Write-SuccessLog "서버가 정상적으로 실행중입니다"
        return $true
    }
    else {
        Write-ErrorLog "서버에 연결할 수 없습니다. 서버가 실행중인지 확인해주세요."
        Write-ErrorLog "오류: $($Result.Error)"
        return $false
    }
}

# 사용자 입력 받기
function Get-UserInput {
    Write-SectionLog "사용자 정보 입력"
    
    if (-not $JwtToken) {
        $Script:JwtToken = Read-Host "JWT 토큰을 입력하세요 (엔터 시 토큰 없이 진행)"
    }
    
    if (-not $UserId) {
        $InputUserId = Read-Host "사용자 ID를 입력하세요 (기본값: 1)"
        $Script:UserId = if ($InputUserId) { $InputUserId } else { "1" }
    }
    
    if (-not $MeetingId) {
        $InputMeetingId = Read-Host "테스트할 약속(Meeting) ID를 입력하세요 (기본값: 1)"
        $Script:MeetingId = if ($InputMeetingId) { $InputMeetingId } else { "1" }
    }
    
    Write-InfoLog "설정완료 - 사용자 ID: $UserId, 약속 ID: $MeetingId"
}

# 카카오 알림 사용 가능 여부 확인
function Test-KakaoAvailability {
    Write-SectionLog "카카오 알림 사용 가능 여부 확인"
    
    $Result = Invoke-ApiRequest -Method "GET" -Endpoint "/api/notifications/kakao/availability"
    
    if ($Result.Success) {
        Write-InfoLog "응답: $($Result.Data | ConvertTo-Json -Compress)"
        
        $Available = Get-JsonValue -JsonObject $Result.Data -Key "available"
        $HasConsent = Get-JsonValue -JsonObject $Result.Data -Key "hasConsent"
        $HasKakaoInfo = Get-JsonValue -JsonObject $Result.Data -Key "hasKakaoInfo"
        
        if ($Available -eq $true) {
            Write-SuccessLog "카카오 알림 전송이 가능합니다"
        }
        else {
            Write-WarningLog "카카오 알림 전송 조건이 충족되지 않았습니다"
            Write-InfoLog "동의 상태: $HasConsent, 카카오 정보: $HasKakaoInfo"
        }
    }
    else {
        Write-ErrorLog "가용성 확인 요청이 실패했습니다"
        Write-ErrorLog "오류: $($Result.Error)"
    }
}

# 카카오 알림 전송 테스트 (기본)
function Test-KakaoNotificationBasic {
    Write-SectionLog "카카오 알림 전송 테스트 (기본)"
    
    $RequestBody = @{
        meetingId = [int]$MeetingId
        receiverIds = @(2, 3, 4)
    } | ConvertTo-Json
    
    Write-InfoLog "전송 요청 데이터: $RequestBody"
    
    $Result = Invoke-ApiRequest -Method "POST" -Endpoint "/api/notifications/kakao" -Body $RequestBody
    
    if ($Result.Success) {
        Write-InfoLog "응답: $($Result.Data | ConvertTo-Json -Compress)"
        
        $Success = Get-JsonValue -JsonObject $Result.Data -Key "success"
        $SentCount = Get-JsonValue -JsonObject $Result.Data -Key "sentCount"
        $TotalCount = Get-JsonValue -JsonObject $Result.Data -Key "totalCount"
        
        if ($Success -eq $true) {
            Write-SuccessLog "카카오 알림 전송 성공 - 전송: $SentCount/$TotalCount"
        }
        else {
            Write-WarningLog "카카오 알림 전송 실패 또는 부분 성공"
        }
    }
    else {
        Write-ErrorLog "카카오 알림 전송 요청이 실패했습니다"
        Write-ErrorLog "상태 코드: $($Result.StatusCode), 오류: $($Result.Error)"
    }
}

# 카카오 알림 전송 테스트 (전체 참여자)
function Test-KakaoNotificationAllParticipants {
    Write-SectionLog "카카오 알림 전송 테스트 (전체 참여자)"
    
    $RequestBody = @{
        meetingId = [int]$MeetingId
    } | ConvertTo-Json
    
    Write-InfoLog "전송 요청 데이터: $RequestBody"
    
    $Result = Invoke-ApiRequest -Method "POST" -Endpoint "/api/notifications/kakao" -Body $RequestBody
    
    if ($Result.Success) {
        Write-InfoLog "응답: $($Result.Data | ConvertTo-Json -Compress)"
        
        $Success = Get-JsonValue -JsonObject $Result.Data -Key "success"
        $SentCount = Get-JsonValue -JsonObject $Result.Data -Key "sentCount"
        $TotalCount = Get-JsonValue -JsonObject $Result.Data -Key "totalCount"
        
        if ($Success -eq $true) {
            Write-SuccessLog "전체 참여자 알림 전송 성공 - 전송: $SentCount/$TotalCount"
        }
        else {
            Write-WarningLog "전체 참여자 알림 전송 실패 또는 부분 성공"
        }
    }
    else {
        Write-ErrorLog "전체 참여자 알림 전송 요청이 실패했습니다"
        Write-ErrorLog "상태 코드: $($Result.StatusCode), 오류: $($Result.Error)"
    }
}

# 잘못된 약속 ID로 테스트
function Test-InvalidMeetingId {
    Write-SectionLog "잘못된 약속 ID 테스트"
    
    $RequestBody = @{
        meetingId = 99999
        receiverIds = @(2, 3)
    } | ConvertTo-Json
    
    Write-InfoLog "잘못된 약속 ID로 테스트: $RequestBody"
    
    $Result = Invoke-ApiRequest -Method "POST" -Endpoint "/api/notifications/kakao" -Body $RequestBody
    
    if ($Result.StatusCode -eq 400 -or ($Result.Success -and (Get-JsonValue -JsonObject $Result.Data -Key "success") -eq $false)) {
        Write-SuccessLog "잘못된 약속 ID에 대한 적절한 오류 응답"
    }
    elseif ($Result.Success) {
        Write-InfoLog "응답: $($Result.Data | ConvertTo-Json -Compress)"
        Write-WarningLog "예상과 다른 응답이 왔습니다"
    }
    else {
        Write-ErrorLog "잘못된 약속 ID 테스트 요청이 실패했습니다"
        Write-ErrorLog "오류: $($Result.Error)"
    }
}

# 빈 수신자 목록 테스트
function Test-EmptyReceivers {
    Write-SectionLog "빈 수신자 목록 테스트"
    
    $RequestBody = @{
        meetingId = [int]$MeetingId
        receiverIds = @()
    } | ConvertTo-Json
    
    Write-InfoLog "빈 수신자 목록 테스트: $RequestBody"
    
    $Result = Invoke-ApiRequest -Method "POST" -Endpoint "/api/notifications/kakao" -Body $RequestBody
    
    if ($Result.Success) {
        Write-InfoLog "응답: $($Result.Data | ConvertTo-Json -Compress)"
        Write-SuccessLog "빈 수신자 목록에 대한 적절한 처리"
    }
    else {
        Write-ErrorLog "빈 수신자 목록 테스트 요청이 실패했습니다"
        Write-ErrorLog "오류: $($Result.Error)"
    }
}

# 카카오 간단 테스트 엔드포인트
function Test-KakaoSimpleTest {
    Write-SectionLog "카카오 간단 테스트 엔드포인트"
    
    $Endpoint = "/api/notifications/kakao/test?meetingId=$MeetingId&receiverIds=2,3"
    Write-InfoLog "간단 테스트 요청: $Endpoint"
    
    $Result = Invoke-ApiRequest -Method "POST" -Endpoint $Endpoint
    
    if ($Result.Success) {
        Write-InfoLog "응답: $($Result.Data | ConvertTo-Json -Compress)"
        Write-SuccessLog "간단 테스트 엔드포인트 응답 확인"
    }
    else {
        Write-ErrorLog "간단 테스트 요청이 실패했습니다"
        Write-ErrorLog "오류: $($Result.Error)"
    }
}

# 알림 서비스 상태 확인
function Test-NotificationServiceHealth {
    Write-SectionLog "알림 서비스 상태 확인"
    
    $Result = Invoke-ApiRequest -Method "GET" -Endpoint "/api/notifications/health"
    
    if ($Result.Success) {
        Write-InfoLog "알림 서비스 상태: $($Result.Data | ConvertTo-Json -Compress)"
        Write-SuccessLog "알림 서비스 상태 확인 완료"
    }
    else {
        Write-ErrorLog "알림 서비스 상태 확인이 실패했습니다"
        Write-ErrorLog "오류: $($Result.Error)"
    }
    
    # 모든 알림 채널 상태 확인
    $ChannelsResult = Invoke-ApiRequest -Method "GET" -Endpoint "/api/notifications/channels/health"
    
    if ($ChannelsResult.Success) {
        Write-InfoLog "모든 알림 채널 상태: $($ChannelsResult.Data | ConvertTo-Json -Compress)"
        Write-SuccessLog "모든 알림 채널 상태 확인 완료"
    }
    else {
        Write-ErrorLog "알림 채널 상태 확인이 실패했습니다"
        Write-ErrorLog "오류: $($ChannelsResult.Error)"
    }
}

# 인증 없이 테스트 (401 오류 확인)
function Test-UnauthorizedAccess {
    Write-SectionLog "인증 없이 접근 테스트 (401 오류 확인)"
    
    $OriginalToken = $Script:JwtToken
    $OriginalUserId = $Script:UserId
    
    # 토큰과 사용자 ID 임시 제거
    $Script:JwtToken = ""
    $Script:UserId = ""
    
    $RequestBody = @{
        meetingId = [int]$MeetingId
        receiverIds = @(2, 3)
    } | ConvertTo-Json
    
    $Result = Invoke-ApiRequest -Method "POST" -Endpoint "/api/notifications/kakao" -Body $RequestBody
    
    if ($Result.StatusCode -eq 401 -or ($Result.Error -match "로그인")) {
        Write-SuccessLog "인증 없는 접근에 대한 적절한 401 오류 응답"
    }
    elseif ($Result.Success) {
        Write-InfoLog "응답: $($Result.Data | ConvertTo-Json -Compress)"
        Write-WarningLog "예상과 다른 응답이 왔습니다"
    }
    else {
        Write-ErrorLog "인증 없는 접근 테스트 요청이 실패했습니다"
        Write-ErrorLog "오류: $($Result.Error)"
    }
    
    # 원래 토큰과 사용자 ID 복원
    $Script:JwtToken = $OriginalToken
    $Script:UserId = $OriginalUserId
}

# 스트레스 테스트 (연속 요청)
function Test-StressRequests {
    Write-SectionLog "스트레스 테스트 (연속 5회 요청)"
    
    $RequestBody = @{
        meetingId = [int]$MeetingId
        receiverIds = @(2)
    } | ConvertTo-Json
    
    for ($i = 1; $i -le 5; $i++) {
        Write-InfoLog "스트레스 테스트 $i/5 진행중..."
        
        $Result = Invoke-ApiRequest -Method "POST" -Endpoint "/api/notifications/kakao" -Body $RequestBody
        
        if ($Result.Success) {
            $Success = Get-JsonValue -JsonObject $Result.Data -Key "success"
            if ($Success -eq $true) {
                Write-SuccessLog "스트레스 테스트 $i 성공"
            }
            else {
                Write-WarningLog "스트레스 테스트 $i 실패 또는 부분 성공"
            }
        }
        else {
            Write-ErrorLog "스트레스 테스트 $i 요청 실패"
            Write-ErrorLog "오류: $($Result.Error)"
        }
        
        # 요청 간격 (1초)
        Start-Sleep -Seconds 1
    }
}

# 모든 테스트 실행
function Start-AllTests {
    Write-SectionLog "카카오톡 약속 알림 서비스 테스트 시작"
    
    # 1. 서버 상태 확인
    if (-not (Test-ServerHealth)) {
        return
    }
    
    # 2. 사용자 입력 (자동 모드가 아닌 경우)
    if (-not $Auto) {
        Get-UserInput
    }
    
    # 3. 카카오 알림 사용 가능 여부 확인
    Test-KakaoAvailability
    
    # 4. 기본 카카오 알림 전송 테스트
    Test-KakaoNotificationBasic
    
    # 5. 전체 참여자 알림 전송 테스트
    Test-KakaoNotificationAllParticipants
    
    # 6. 간단 테스트 엔드포인트
    Test-KakaoSimpleTest
    
    # 7. 오류 상황 테스트
    Test-InvalidMeetingId
    Test-EmptyReceivers
    Test-UnauthorizedAccess
    
    # 8. 알림 서비스 상태 확인
    Test-NotificationServiceHealth
    
    # 9. 스트레스 테스트
    Test-StressRequests
    
    Write-SectionLog "모든 테스트 완료"
    Write-SuccessLog "카카오톡 알림 서비스 테스트가 완료되었습니다!"
}

# 도움말 출력
function Show-Help {
    Write-Host "카카오톡 약속 알림 서비스 테스트 스크립트 (PowerShell)" -ForegroundColor $Colors.Section
    Write-Host ""
    Write-Host "사용법:"
    Write-Host "  .\kakao-notification-test-script.ps1 [옵션]"
    Write-Host ""
    Write-Host "옵션:"
    Write-Host "  -Help                이 도움말을 표시합니다"
    Write-Host "  -BaseUrl URL         서버 URL을 지정합니다 (기본값: http://localhost:8080)"
    Write-Host "  -JwtToken TOKEN      JWT 토큰을 지정합니다"
    Write-Host "  -UserId ID           사용자 ID를 지정합니다 (기본값: 1)"
    Write-Host "  -MeetingId ID        약속 ID를 지정합니다 (기본값: 1)"
    Write-Host "  -Auto                자동 모드 (사용자 입력 없이 기본값으로 실행)"
    Write-Host ""
    Write-Host "예시:"
    Write-Host "  .\kakao-notification-test-script.ps1 -Auto -UserId 2 -MeetingId 5"
    Write-Host "  .\kakao-notification-test-script.ps1 -BaseUrl 'http://localhost:8080' -JwtToken 'eyJhbGci...'"
}

# 메인 실행부
if ($Help) {
    Show-Help
    return
}

if ($Auto) {
    Write-InfoLog "자동 모드 실행 - 사용자 ID: $UserId, 약속 ID: $MeetingId"
}

# 스크립트 실행
Start-AllTests


