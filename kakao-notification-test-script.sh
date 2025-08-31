#!/bin/bash

# 카카오톡 약속 알림 서비스 테스트 스크립트
# 이유: 카카오톡 알림 기능이 정상적으로 작동하는지 다양한 시나리오로 테스트하기 위해

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 기본 설정
BASE_URL="http://localhost:8080"
JWT_TOKEN=""
USER_ID=""
MEETING_ID=""

# 로그 함수들
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_section() {
    echo
    echo -e "${YELLOW}=== $1 ===${NC}"
    echo
}

# HTTP 요청 함수
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local headers=""
    
    if [ ! -z "$JWT_TOKEN" ]; then
        headers="-H 'Authorization: Bearer $JWT_TOKEN'"
    fi
    
    if [ ! -z "$USER_ID" ]; then
        headers="$headers -H 'X-User-ID: $USER_ID'"
    fi
    
    if [ "$method" = "POST" ] && [ ! -z "$data" ]; then
        headers="$headers -H 'Content-Type: application/json'"
        eval "curl -s -X $method $headers -d '$data' '$BASE_URL$endpoint'"
    else
        eval "curl -s -X $method $headers '$BASE_URL$endpoint'"
    fi
}

# JSON 파싱 함수
extract_json_value() {
    local json=$1
    local key=$2
    echo "$json" | grep -o "\"$key\":[^,}]*" | cut -d':' -f2 | tr -d '"' | tr -d ' '
}

# 서버 상태 확인
check_server_health() {
    log_section "서버 상태 확인"
    
    local response=$(curl -s "$BASE_URL/api/health" || echo "")
    
    if [[ $response == *"UP"* ]] || [[ $response == *"status"* ]]; then
        log_success "서버가 정상적으로 실행중입니다"
        return 0
    else
        log_error "서버에 연결할 수 없습니다. 서버가 실행중인지 확인해주세요."
        return 1
    fi
}

# 사용자 입력 받기
get_user_input() {
    log_section "사용자 정보 입력"
    
    echo -n "JWT 토큰을 입력하세요 (엔터 시 토큰 없이 진행): "
    read JWT_TOKEN
    
    echo -n "사용자 ID를 입력하세요 (기본값: 1): "
    read input_user_id
    USER_ID=${input_user_id:-1}
    
    echo -n "테스트할 약속(Meeting) ID를 입력하세요 (기본값: 1): "
    read input_meeting_id
    MEETING_ID=${input_meeting_id:-1}
    
    log_info "설정완료 - 사용자 ID: $USER_ID, 약속 ID: $MEETING_ID"
}

# 카카오 알림 사용 가능 여부 확인
check_kakao_availability() {
    log_section "카카오 알림 사용 가능 여부 확인"
    
    local response=$(make_request "GET" "/api/notifications/kakao/availability")
    
    if [ $? -eq 0 ]; then
        log_info "응답: $response"
        
        local available=$(extract_json_value "$response" "available")
        local hasConsent=$(extract_json_value "$response" "hasConsent")
        local hasKakaoInfo=$(extract_json_value "$response" "hasKakaoInfo")
        
        if [[ "$available" == "true" ]]; then
            log_success "카카오 알림 전송이 가능합니다"
        else
            log_warning "카카오 알림 전송 조건이 충족되지 않았습니다"
            log_info "동의 상태: $hasConsent, 카카오 정보: $hasKakaoInfo"
        fi
    else
        log_error "가용성 확인 요청이 실패했습니다"
    fi
}

# 카카오 알림 전송 테스트 (기본)
test_kakao_notification_basic() {
    log_section "카카오 알림 전송 테스트 (기본)"
    
    local data="{
        \"meetingId\": $MEETING_ID,
        \"receiverIds\": [2, 3, 4]
    }"
    
    log_info "전송 요청 데이터: $data"
    
    local response=$(make_request "POST" "/api/notifications/kakao" "$data")
    
    if [ $? -eq 0 ]; then
        log_info "응답: $response"
        
        local success=$(extract_json_value "$response" "success")
        local sentCount=$(extract_json_value "$response" "sentCount")
        local totalCount=$(extract_json_value "$response" "totalCount")
        
        if [[ "$success" == "true" ]]; then
            log_success "카카오 알림 전송 성공 - 전송: $sentCount/$totalCount"
        else
            log_warning "카카오 알림 전송 실패 또는 부분 성공"
        fi
    else
        log_error "카카오 알림 전송 요청이 실패했습니다"
    fi
}

# 카카오 알림 전송 테스트 (전체 참여자)
test_kakao_notification_all_participants() {
    log_section "카카오 알림 전송 테스트 (전체 참여자)"
    
    local data="{
        \"meetingId\": $MEETING_ID
    }"
    
    log_info "전송 요청 데이터: $data"
    
    local response=$(make_request "POST" "/api/notifications/kakao" "$data")
    
    if [ $? -eq 0 ]; then
        log_info "응답: $response"
        
        local success=$(extract_json_value "$response" "success")
        local sentCount=$(extract_json_value "$response" "sentCount")
        local totalCount=$(extract_json_value "$response" "totalCount")
        
        if [[ "$success" == "true" ]]; then
            log_success "전체 참여자 알림 전송 성공 - 전송: $sentCount/$totalCount"
        else
            log_warning "전체 참여자 알림 전송 실패 또는 부분 성공"
        fi
    else
        log_error "전체 참여자 알림 전송 요청이 실패했습니다"
    fi
}

# 잘못된 약속 ID로 테스트
test_invalid_meeting_id() {
    log_section "잘못된 약속 ID 테스트"
    
    local data="{
        \"meetingId\": 99999,
        \"receiverIds\": [2, 3]
    }"
    
    log_info "잘못된 약속 ID로 테스트: $data"
    
    local response=$(make_request "POST" "/api/notifications/kakao" "$data")
    
    if [ $? -eq 0 ]; then
        log_info "응답: $response"
        
        if [[ $response == *"400"* ]] || [[ $response == *"존재하지 않는"* ]]; then
            log_success "잘못된 약속 ID에 대한 적절한 오류 응답"
        else
            log_warning "예상과 다른 응답이 왔습니다"
        fi
    else
        log_error "잘못된 약속 ID 테스트 요청이 실패했습니다"
    fi
}

# 빈 수신자 목록 테스트
test_empty_receivers() {
    log_section "빈 수신자 목록 테스트"
    
    local data="{
        \"meetingId\": $MEETING_ID,
        \"receiverIds\": []
    }"
    
    log_info "빈 수신자 목록 테스트: $data"
    
    local response=$(make_request "POST" "/api/notifications/kakao" "$data")
    
    if [ $? -eq 0 ]; then
        log_info "응답: $response"
        
        # 빈 수신자 목록이어도 약속 참여자 전체를 대상으로 하므로 정상 처리될 수 있음
        local success=$(extract_json_value "$response" "success")
        if [[ "$success" == "true" ]] || [[ $response == *"참여자"* ]]; then
            log_success "빈 수신자 목록에 대한 적절한 처리"
        else
            log_warning "예상과 다른 응답이 왔습니다"
        fi
    else
        log_error "빈 수신자 목록 테스트 요청이 실패했습니다"
    fi
}

# 카카오 알림 테스트 엔드포인트 (간단 테스트)
test_kakao_simple_test() {
    log_section "카카오 간단 테스트 엔드포인트"
    
    local endpoint="/api/notifications/kakao/test?meetingId=$MEETING_ID&receiverIds=2,3"
    
    log_info "간단 테스트 요청: $endpoint"
    
    local response=$(make_request "POST" "$endpoint")
    
    if [ $? -eq 0 ]; then
        log_info "응답: $response"
        log_success "간단 테스트 엔드포인트 응답 확인"
    else
        log_error "간단 테스트 요청이 실패했습니다"
    fi
}

# 알림 서비스 상태 확인
check_notification_service_health() {
    log_section "알림 서비스 상태 확인"
    
    local response=$(make_request "GET" "/api/notifications/health")
    
    if [ $? -eq 0 ]; then
        log_info "알림 서비스 상태: $response"
        log_success "알림 서비스 상태 확인 완료"
    else
        log_error "알림 서비스 상태 확인이 실패했습니다"
    fi
    
    # 모든 알림 채널 상태 확인
    local channels_response=$(make_request "GET" "/api/notifications/channels/health")
    
    if [ $? -eq 0 ]; then
        log_info "모든 알림 채널 상태: $channels_response"
        log_success "모든 알림 채널 상태 확인 완료"
    else
        log_error "알림 채널 상태 확인이 실패했습니다"
    fi
}

# 인증 없이 테스트 (401 오류 확인)
test_unauthorized_access() {
    log_section "인증 없이 접근 테스트 (401 오류 확인)"
    
    local original_token="$JWT_TOKEN"
    local original_user_id="$USER_ID"
    
    # 토큰과 사용자 ID 임시 제거
    JWT_TOKEN=""
    USER_ID=""
    
    local data="{
        \"meetingId\": $MEETING_ID,
        \"receiverIds\": [2, 3]
    }"
    
    local response=$(make_request "POST" "/api/notifications/kakao" "$data")
    
    if [ $? -eq 0 ]; then
        log_info "응답: $response"
        
        if [[ $response == *"401"* ]] || [[ $response == *"로그인"* ]]; then
            log_success "인증 없는 접근에 대한 적절한 401 오류 응답"
        else
            log_warning "예상과 다른 응답이 왔습니다"
        fi
    else
        log_error "인증 없는 접근 테스트 요청이 실패했습니다"
    fi
    
    # 원래 토큰과 사용자 ID 복원
    JWT_TOKEN="$original_token"
    USER_ID="$original_user_id"
}

# 스트레스 테스트 (연속 요청)
test_stress_requests() {
    log_section "스트레스 테스트 (연속 5회 요청)"
    
    local data="{
        \"meetingId\": $MEETING_ID,
        \"receiverIds\": [2]
    }"
    
    for i in {1..5}; do
        log_info "스트레스 테스트 $i/5 진행중..."
        
        local response=$(make_request "POST" "/api/notifications/kakao" "$data")
        
        if [ $? -eq 0 ]; then
            local success=$(extract_json_value "$response" "success")
            if [[ "$success" == "true" ]]; then
                log_success "스트레스 테스트 $i 성공"
            else
                log_warning "스트레스 테스트 $i 실패 또는 부분 성공"
            fi
        else
            log_error "스트레스 테스트 $i 요청 실패"
        fi
        
        # 요청 간격 (1초)
        sleep 1
    done
}

# 메인 테스트 실행 함수
run_all_tests() {
    log_section "카카오톡 약속 알림 서비스 테스트 시작"
    
    # 1. 서버 상태 확인
    if ! check_server_health; then
        exit 1
    fi
    
    # 2. 사용자 입력
    get_user_input
    
    # 3. 카카오 알림 사용 가능 여부 확인
    check_kakao_availability
    
    # 4. 기본 카카오 알림 전송 테스트
    test_kakao_notification_basic
    
    # 5. 전체 참여자 알림 전송 테스트
    test_kakao_notification_all_participants
    
    # 6. 간단 테스트 엔드포인트
    test_kakao_simple_test
    
    # 7. 오류 상황 테스트
    test_invalid_meeting_id
    test_empty_receivers
    test_unauthorized_access
    
    # 8. 알림 서비스 상태 확인
    check_notification_service_health
    
    # 9. 스트레스 테스트
    test_stress_requests
    
    log_section "모든 테스트 완료"
    log_success "카카오톡 알림 서비스 테스트가 완료되었습니다!"
}

# 도움말 출력
show_help() {
    echo "카카오톡 약속 알림 서비스 테스트 스크립트"
    echo
    echo "사용법:"
    echo "  $0 [옵션]"
    echo
    echo "옵션:"
    echo "  -h, --help           이 도움말을 표시합니다"
    echo "  -u, --url URL        서버 URL을 지정합니다 (기본값: http://localhost:8080)"
    echo "  -t, --token TOKEN    JWT 토큰을 지정합니다"
    echo "  -i, --user-id ID     사용자 ID를 지정합니다 (기본값: 1)"
    echo "  -m, --meeting-id ID  약속 ID를 지정합니다 (기본값: 1)"
    echo "  -a, --auto           자동 모드 (사용자 입력 없이 기본값으로 실행)"
    echo
    echo "예시:"
    echo "  $0 --auto --user-id 2 --meeting-id 5"
    echo "  $0 --url http://localhost:8080 --token eyJhbGci..."
}

# 파라미터 파싱
AUTO_MODE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -u|--url)
            BASE_URL="$2"
            shift 2
            ;;
        -t|--token)
            JWT_TOKEN="$2"
            shift 2
            ;;
        -i|--user-id)
            USER_ID="$2"
            shift 2
            ;;
        -m|--meeting-id)
            MEETING_ID="$2"
            shift 2
            ;;
        -a|--auto)
            AUTO_MODE=true
            shift
            ;;
        *)
            log_error "알 수 없는 옵션: $1"
            show_help
            exit 1
            ;;
    esac
done

# 메인 실행부
main() {
    # 자동 모드가 아니면 사용자 입력 받기
    if [ "$AUTO_MODE" = true ]; then
        USER_ID=${USER_ID:-1}
        MEETING_ID=${MEETING_ID:-1}
        log_info "자동 모드 실행 - 사용자 ID: $USER_ID, 약속 ID: $MEETING_ID"
        
        # 서버 상태만 확인하고 바로 테스트 실행
        if check_server_health; then
            # 사용자 입력 없이 바로 테스트들 실행
            check_kakao_availability
            test_kakao_notification_basic
            test_kakao_notification_all_participants
            test_kakao_simple_test
            test_invalid_meeting_id
            test_empty_receivers
            test_unauthorized_access
            check_notification_service_health
            test_stress_requests
            
            log_section "자동 테스트 완료"
            log_success "카카오톡 알림 서비스 자동 테스트가 완료되었습니다!"
        fi
    else
        run_all_tests
    fi
}

# 스크립트 실행
main


