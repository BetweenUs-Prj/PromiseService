package com.promiseservice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 인증 필터 (간단한 서블릿 필터 버전)
 * 이유: Spring Security 없이도 JWT 토큰을 검증하고 사용자 정보를 HTTP 요청에 설정하여
 * 컨트롤러에서 사용자 ID를 조회할 수 있도록 지원하기 위해
 * OPTIONS 요청과 토큰이 없는 요청은 건너뛰어 CORS와 공개 리소스 접근을 보장
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements Filter {

    // 개발 모드 설정
    // 이유: 개발 환경에서만 X-User-Id 헤더를 통한 사용자 강제 지정을 허용하기 위해
    @Value("${app.dev.skip-host-check:false}")
    private boolean devMode;

    /**
     * JWT 토큰 검증 및 인증 처리 (서블릿 필터 버전)
     * 이유: 각 요청마다 JWT 토큰의 유효성을 검사하고 사용자 정보를 요청 속성에 설정하기 위해
     * Spring Security 없이도 인증 정보를 컨트롤러에서 사용할 수 있도록 지원
     * 
     * @param request 서블릿 요청
     * @param response 서블릿 응답
     * @param chain 필터 체인
     * @throws IOException I/O 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // ★ OPTIONS 요청은 무조건 패스
        // 이유: CORS 프리플라이트 요청은 인증 검사 없이 통과시켜야 브라우저가 정상적으로 실제 요청을 보낼 수 있음
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            log.debug("OPTIONS 요청 감지 - 인증 검사 건너뜀: {}", httpRequest.getRequestURI());
            chain.doFilter(request, response);
            return;
        }
        
        // ★ 개발용 API는 인증 검사 건너뜀 (단, X-User-Id 헤더 처리는 수행)
        // 이유: /api/dev/** 경로는 개발 및 테스트 용도로 인증 없이 접근 가능하도록 허용
        if (httpRequest.getRequestURI().startsWith("/api/dev/")) {
            log.debug("개발용 API 요청 감지: {}", httpRequest.getRequestURI());
            
            // 개발 모드에서 X-User-Id 헤더 체크
            // 이유: 개발 환경에서 특정 사용자 ID로 강제 지정하여 테스트 편의성 제공
            if (devMode) {
                String xUserId = httpRequest.getHeader("X-User-Id");
                if (xUserId != null && !xUserId.trim().isEmpty()) {
                    try {
                        Long forcedUserId = Long.valueOf(xUserId.trim());
                        httpRequest.setAttribute("userId", forcedUserId);
                        httpRequest.setAttribute("userRole", "USER");
                        httpRequest.setAttribute("authenticated", true);
                        log.debug("개발 모드: X-User-Id 헤더로 사용자 강제 지정 - userId: {}", forcedUserId);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 X-User-Id 헤더 형식: {}", xUserId);
                    }
                }
            }
            
            chain.doFilter(request, response);
            return;
        }
        
        // ★ 카카오 테스트 API는 인증 검사 건너뜀
        // 이유: /api/kakao/test/** 경로는 카카오톡 연동 테스트 용도로 인증 없이 접근 가능하도록 허용
        if (httpRequest.getRequestURI().startsWith("/api/kakao/test/")) {
            log.debug("카카오 테스트 API 요청 감지 - 인증 검사 건너뜀: {}", httpRequest.getRequestURI());
            chain.doFilter(request, response);
            return;
        }
        
        // ★ 알림 조회 API는 인증 검사 건너뜀
        // 이유: /api/notifications/** 경로는 운영 모니터링 용도로 인증 없이 접근 가능하도록 허용
        if (httpRequest.getRequestURI().startsWith("/api/notifications/")) {
            log.debug("알림 조회 API 요청 감지 - 인증 검사 건너뜀: {}", httpRequest.getRequestURI());
            chain.doFilter(request, response);
            return;
        }
        
        // Authorization 헤더 추출
        String authorizationHeader = httpRequest.getHeader("Authorization");
        
        // ★ 헤더가 없거나 Bearer로 시작하지 않으면 패스
        // 이유: 공개 리소스나 정적 파일 접근 시 인증 토큰이 없어도 정상 처리되도록 지원
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.debug("Authorization 헤더 없음 또는 Bearer 형식 아님 - 인증 검사 건너뜀: {}", httpRequest.getRequestURI());
            chain.doFilter(request, response);
            return;
        }
        
        try {
            // JWT 토큰 추출
            String token = authorizationHeader.substring(7); // "Bearer " 제거
            
            log.debug("JWT 토큰 추출됨 - 길이: {}, URI: {}", token.length(), httpRequest.getRequestURI());
            
            // 토큰 검증 및 사용자 정보 추출
            // 이유: JWT 토큰에서 사용자 ID와 권한 정보를 추출하여 요청 속성에 설정하기 위해
            if (isValidToken(token)) {
                Long userId = extractUserIdFromToken(token);
                String userRole = extractUserRoleFromToken(token);
                
                // 요청 속성에 사용자 정보 설정
                // 이유: Spring Security 없이도 컨트롤러에서 사용자 정보를 조회할 수 있도록 지원
                httpRequest.setAttribute("userId", userId);
                httpRequest.setAttribute("userRole", userRole);
                httpRequest.setAttribute("authenticated", true);
                
                log.debug("JWT 인증 성공 - 사용자 ID: {}, 역할: {}", userId, userRole);
                
            } else {
                log.warn("유효하지 않은 JWT 토큰: {}", token.substring(0, Math.min(20, token.length())) + "...");
                // 토큰이 유효하지 않아도 필터 체인은 계속 진행
                // 이유: 인증 실패 처리를 컨트롤러나 다른 계층에서 처리하도록 위임
            }
            
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
            // 예외 발생 시에도 필터 체인 계속 진행
            // 이유: 인증 실패가 전체 요청을 차단하지 않도록 하여 시스템 안정성 확보
        }
        
        // 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }
    
    /**
     * JWT 토큰 유효성 검증 (간단한 구현)
     * 이유: UserService에서 발급한 토큰의 형식과 유효성을 간단히 검증하기 위해
     * 실제 환경에서는 서명 검증, 만료 시간 확인 등의 정교한 검증이 필요
     * 
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    private boolean isValidToken(String token) {
        try {
            // 간단한 토큰 형식 검증
            // 이유: JWT는 일반적으로 3개 부분(헤더.페이로드.서명)으로 구성되므로 기본 형식 확인
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            
            // JWT 형식 확인 (점으로 구분된 3개 부분)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.debug("JWT 토큰 형식 오류 - 구성 요소 개수: {}", parts.length);
                return false;
            }
            
            // TODO: 실제 환경에서는 여기서 서명 검증, 만료 시간 확인 등을 수행
            // 현재는 형식만 확인하는 임시 구현
            log.debug("JWT 토큰 기본 형식 검증 통과");
            return true;
            
        } catch (Exception e) {
            log.error("JWT 토큰 유효성 검증 중 오류: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * JWT 토큰에서 사용자 ID 추출 (임시 구현)
     * 이유: JWT 페이로드에서 사용자 식별자를 추출하여 인증 객체에 설정하기 위해
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    private Long extractUserIdFromToken(String token) {
        try {
            // TODO: 실제 환경에서는 JWT 라이브러리를 사용하여 페이로드 파싱
            // 현재는 토큰 길이를 기반으로 임시 사용자 ID 생성 (테스트용)
            
            // 간단한 해시를 통한 임시 사용자 ID 생성
            // 이유: 실제 JWT 파싱 라이브러리 도입 전까지 테스트 가능한 임시 구현
            int hashCode = token.hashCode();
            Long userId = Math.abs((long) hashCode) % 1000 + 1; // 1~1000 범위의 사용자 ID
            
            log.debug("토큰에서 추출된 임시 사용자 ID: {}", userId);
            return userId;
            
        } catch (Exception e) {
            log.error("사용자 ID 추출 중 오류: {}", e.getMessage());
            return 1L; // 기본값 반환
        }
    }
    
    /**
     * JWT 토큰에서 사용자 역할 추출 (임시 구현)
     * 이유: 사용자의 권한 정보를 추출하여 Spring Security 권한 체계에 적용하기 위해
     * 
     * @param token JWT 토큰
     * @return 사용자 역할
     */
    private String extractUserRoleFromToken(String token) {
        try {
            // TODO: 실제 환경에서는 JWT 페이로드에서 role 클레임 추출
            // 현재는 모든 사용자를 USER 역할로 설정 (임시)
            
            // 토큰 길이에 따른 임시 역할 할당
            // 이유: 테스트 환경에서 다양한 권한 시나리오를 확인할 수 있도록 지원
            if (token.length() > 200) {
                return "ADMIN";
            } else {
                return "USER";
            }
            
        } catch (Exception e) {
            log.error("사용자 역할 추출 중 오류: {}", e.getMessage());
            return "USER"; // 기본값 반환
        }
    }
}
