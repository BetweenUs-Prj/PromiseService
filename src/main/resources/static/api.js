/**
 * PromiseService API 호출 유틸리티
 * 이유: JWT 토큰을 자동으로 Authorization 헤더에 첨부하여
 * 모든 API 호출에서 인증 처리를 자동화하고 코드 중복을 방지하기 위해
 */

/**
 * JWT 토큰이 첨부된 API 호출 함수
 * 이유: localStorage에 저장된 JWT를 자동으로 헤더에 포함시켜
 * 개발자가 매번 인증 헤더를 수동으로 설정하는 번거로움을 제거하기 위해
 * 
 * @param {string} url API 엔드포인트 URL
 * @param {object} options fetch 옵션 (method, body, headers 등)
 * @returns {Promise<any>} 파싱된 JSON 응답
 * @throws {Error} HTTP 오류 또는 네트워크 오류 시
 */
async function apiFetch(url, options = {}) {
    try {
        // localStorage에서 JWT 토큰 조회
        // 이유: 로그인 시 저장된 인증 토큰을 가져와 API 호출에 사용
        const token = localStorage.getItem('jwt');
        
        // 기존 헤더와 Authorization 헤더 병합
        // 이유: 기존에 설정된 헤더를 유지하면서 인증 헤더를 추가하기 위해
        const headers = Object.assign({}, options.headers || {});
        
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        
        // Content-Type 기본값 설정 (POST/PUT 요청에서 JSON 전송 시)
        // 이유: API 서버가 요청 데이터 형식을 올바르게 인식할 수 있도록 지원
        if (options.method && ['POST', 'PUT', 'PATCH'].includes(options.method.toUpperCase())) {
            if (!headers['Content-Type']) {
                headers['Content-Type'] = 'application/json';
            }
        }
        
        // fetch 옵션 병합
        const fetchOptions = Object.assign({}, options, { headers });
        
        console.log(`🔗 API 호출: ${options.method || 'GET'} ${url}`, {
            headers: { ...headers, Authorization: token ? 'Bearer ***' : undefined },
            body: options.body
        });
        
        // 실제 API 호출
        const response = await fetch(url, fetchOptions);
        
        // HTTP 상태 확인
        // 이유: 2xx 범위가 아닌 응답에 대해 명확한 오류 처리를 수행하기 위해
        if (!response.ok) {
            const errorText = await response.text();
            let errorMessage = `HTTP ${response.status}`;
            
            // JSON 오류 응답 파싱 시도
            try {
                const errorJson = JSON.parse(errorText);
                if (errorJson.message) {
                    errorMessage += `: ${errorJson.message}`;
                }
                if (errorJson.error) {
                    errorMessage += ` (${errorJson.error})`;
                }
            } catch (e) {
                // JSON 파싱 실패 시 원본 텍스트 사용
                if (errorText) {
                    errorMessage += `: ${errorText.substring(0, 100)}`;
                }
            }
            
            console.error('❌ API 오류:', {
                status: response.status,
                statusText: response.statusText,
                error: errorText
            });
            
            throw new Error(errorMessage);
        }
        
        // 응답 JSON 파싱
        const result = await response.json();
        
        console.log('✅ API 성공:', result);
        
        return result;
        
    } catch (error) {
        // 네트워크 오류나 기타 예외 처리
        console.error('❌ API 호출 실패:', error);
        
        // 인증 오류인 경우 로그인 페이지로 리다이렉트 제안
        if (error.message.includes('401') || error.message.includes('Unauthorized')) {
            if (confirm('인증이 만료되었습니다. 다시 로그인하시겠습니까?')) {
                localStorage.removeItem('jwt');
                location.href = '/login.html';
                return;
            }
        }
        
        throw error;
    }
}

/**
 * 약속 확정 API 호출 헬퍼
 * 이유: 자주 사용되는 약속 확정 API를 간편하게 호출할 수 있도록 래핑
 * 
 * @param {number} appointmentId 확정할 약속 ID
 * @returns {Promise<any>} API 응답
 */
async function confirmAppointment(appointmentId) {
    return await apiFetch(`/api/appointments/${appointmentId}/confirm`, {
        method: 'POST'
    });
}

/**
 * 참여자 현황 조회 API 호출 헬퍼
 * 이유: 약속의 참여자 현황을 조회하는 API를 간편하게 호출하기 위해
 * 
 * @param {number} appointmentId 조회할 약속 ID
 * @returns {Promise<any>} 참여자 현황 데이터
 */
async function getParticipants(appointmentId) {
    return await apiFetch(`/api/appointments/${appointmentId}/participants`, {
        method: 'GET'
    });
}

/**
 * 알림 재발송 API 호출 헬퍼
 * 이유: 실패한 알림을 재발송하는 API를 간편하게 호출하기 위해
 * 
 * @param {number} appointmentId 약속 ID
 * @param {number[]} participantIds 재발송 대상 참여자 ID 목록
 * @returns {Promise<any>} 재발송 결과
 */
async function resendNotifications(appointmentId, participantIds) {
    return await apiFetch(`/api/appointments/${appointmentId}/notify/resend`, {
        method: 'POST',
        body: JSON.stringify(participantIds)
    });
}

/**
 * 약속 취소 API 호출 헬퍼
 * 이유: 약속을 취소하는 API를 간편하게 호출하기 위해
 * 
 * @param {number} appointmentId 취소할 약속 ID
 * @param {string} cancelReason 취소 사유 (선택사항)
 * @returns {Promise<any>} 취소 결과
 */
async function cancelAppointment(appointmentId, cancelReason = '') {
    const url = `/api/appointments/${appointmentId}/cancel${cancelReason ? '?cancelReason=' + encodeURIComponent(cancelReason) : ''}`;
    return await apiFetch(url, {
        method: 'POST'
    });
}

/**
 * 현재 로그인 상태 확인
 * 이유: JWT 토큰 존재 여부를 확인하여 로그인 상태를 판단하기 위해
 * 
 * @returns {boolean} 로그인 여부
 */
function isLoggedIn() {
    const token = localStorage.getItem('jwt');
    return !!token;
}

/**
 * 로그아웃 처리
 * 이유: 저장된 JWT 토큰을 제거하고 로그인 페이지로 이동하기 위해
 */
function logout() {
    localStorage.removeItem('jwt');
    location.href = '/login.html';
}

/**
 * 저장된 JWT 토큰 조회
 * 이유: 디버깅이나 토큰 정보 확인이 필요한 경우 사용
 * 
 * @returns {string|null} JWT 토큰 또는 null
 */
function getToken() {
    return localStorage.getItem('jwt');
}

// 전역 사용을 위해 window 객체에 등록
// 이유: 다른 HTML 파일에서도 이 함수들을 사용할 수 있도록 전역 스코프에 노출
if (typeof window !== 'undefined') {
    window.apiFetch = apiFetch;
    window.confirmAppointment = confirmAppointment;
    window.getParticipants = getParticipants;
    window.resendNotifications = resendNotifications;
    window.cancelAppointment = cancelAppointment;
    window.isLoggedIn = isLoggedIn;
    window.logout = logout;
    window.getToken = getToken;
}

// 페이지 로드 시 로그인 상태 확인
// 이유: 페이지 접근 시 자동으로 로그인 상태를 확인하여 필요한 경우 로그인 페이지로 리다이렉트
document.addEventListener('DOMContentLoaded', function() {
    // 로그인 관련 페이지가 아닌 경우에만 로그인 상태 확인
    const currentPath = location.pathname;
    const publicPaths = ['/login.html', '/login-done.html', '/'];
    
    if (!publicPaths.includes(currentPath) && !isLoggedIn()) {
        console.log('로그인이 필요한 페이지입니다. 로그인 페이지로 이동합니다.');
        location.href = '/login.html';
    }
});







