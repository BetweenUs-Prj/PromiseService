package com.promiseservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 헤더 스니핑 필터
 * 이유: 모든 요청에서 X-Kakao-Id 헤더가 실제로 전달되는지 최상단에서 확인하기 위해
 * 필터 체인에서 헤더가 지워지는 문제를 빠르게 파악할 수 있음
 */
@Slf4j
@Component
public class HeaderSniffFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 모든 요청에서 X-Kakao-Id 헤더 확인
        String kakaoId = request.getHeader("X-Kakao-Id");
        
        if (kakaoId != null) {
            log.info("🔍 SNIFF: X-Kakao-Id={}", kakaoId);
        }
        
        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}
