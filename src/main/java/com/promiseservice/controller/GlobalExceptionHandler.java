package com.promiseservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.Map;

/**
 * 글로벌 예외 처리 핸들러
 * 이유: 일관된 에러 응답 형태로 클라이언트에게 명확한 오류 정보를 제공하고 500 에러를 방지하기 위해
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 접근 권한 없음 예외 처리
     * 이유: 권한이 없는 사용자의 요청을 403 Forbidden으로 명확히 응답하기 위해
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleForbidden(AccessDeniedException e) {
        return Map.of(
            "code", "FORBIDDEN", 
            "message", e.getMessage(),
            "status", 403
        );
    }

    /**
     * 엔티티 찾을 수 없음 예외 처리
     * 이유: 존재하지 않는 리소스 요청을 404 Not Found로 명확히 응답하기 위해
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(EntityNotFoundException e) {
        return Map.of(
            "code", "NOT_FOUND", 
            "message", e.getMessage(),
            "status", 404
        );
    }

    /**
     * 일반적인 RuntimeException 처리
     * 이유: 비즈니스 로직 오류를 500 대신 적절한 상태 코드로 응답하기 위해
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        // 특정 메시지에 따라 다른 상태 코드 매핑
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("권한이 없습니다") || message.contains("권한 없음")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "code", "FORBIDDEN", 
                    "message", message,
                    "status", 403
                ));
            }
            if (message.contains("찾을 수 없습니다") || message.contains("존재하지 않습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "code", "NOT_FOUND", 
                    "message", message,
                    "status", 404
                ));
            }
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "code", "INTERNAL_SERVER_ERROR", 
            "message", message != null ? message : "서버 내부 오류가 발생했습니다",
            "status", 500
        ));
    }

    /**
     * Validation 예외 처리
     * 이유: 요청 데이터 검증 실패를 400 Bad Request로 명확히 응답하기 위해
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBadRequest(MethodArgumentNotValidException e) {
        return Map.of(
            "code", "BAD_REQUEST", 
            "message", "validation failed",
            "status", 400
        );
    }
}
