package com.promiseservice.controller;

import com.promiseservice.dto.MeetingSearchRequest;
import com.promiseservice.dto.MeetingSearchResponse;
import com.promiseservice.dto.MeetingSummaryResponse;
import com.promiseservice.service.MeetingSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 약속 검색 및 필터링을 위한 REST API 컨트롤러
 * 이유: 사용자가 다양한 조건으로 약속을 검색할 수 있도록 검색 관련 엔드포인트를 제공하고,
 * 검색 결과의 페이지네이션과 정렬을 체계적으로 관리하기 위해
 */
@Slf4j
@RestController
@RequestMapping("/api/meetings/search")
@RequiredArgsConstructor
public class MeetingSearchController {

    private final MeetingSearchService meetingSearchService;

    /**
     * 고급 약속 검색을 수행하는 엔드포인트
     * 이유: 다양한 검색 조건을 조합하여 사용자가 원하는 약속을 효율적으로 찾을 수 있도록 
     * 복합 검색 기능을 제공하기 위해
     * 
     * POST /api/meetings/search
     */
    @PostMapping
    public ResponseEntity<MeetingSearchResponse> searchMeetings(
            @Valid @RequestBody MeetingSearchRequest request,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("고급 약속 검색 요청 - 사용자: {}, 검색 조건: {}", currentUserId, request);
        
        try {
            MeetingSearchResponse response = meetingSearchService.searchMeetings(request, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약속 검색 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 키워드로 약속을 검색하는 엔드포인트
     * 이유: 사용자가 제목이나 설명에서 특정 키워드를 포함한 약속을 빠르게 찾을 수 있도록 
     * 간편한 검색 기능을 제공하기 위해
     * 
     * GET /api/meetings/search/keyword?q={keyword}&page={page}&size={size}
     */
    @GetMapping("/keyword")
    public ResponseEntity<MeetingSearchResponse> searchByKeyword(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("키워드 검색 요청 - 사용자: {}, 키워드: {}, 페이지: {}", currentUserId, q, page);
        
        try {
            MeetingSearchRequest request = new MeetingSearchRequest();
            request.setKeyword(q);
            request.setPage(page);
            request.setSize(size);
            
            MeetingSearchResponse response = meetingSearchService.searchMeetings(request, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("키워드 검색 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 상태의 약속을 검색하는 엔드포인트
     * 이유: 사용자가 원하는 상태의 약속만 조회할 수 있도록 상태별 필터링 기능을 제공하기 위해
     * 
     * GET /api/meetings/search/status/{status}?page={page}&size={size}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<MeetingSearchResponse> searchByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("상태별 검색 요청 - 사용자: {}, 상태: {}, 페이지: {}", currentUserId, status, page);
        
        try {
            MeetingSearchRequest request = new MeetingSearchRequest();
            request.setStatus(status);
            request.setPage(page);
            request.setSize(size);
            
            MeetingSearchResponse response = meetingSearchService.searchMeetings(request, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상태별 검색 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 장소명으로 약속을 검색하는 엔드포인트
     * 이유: 사용자가 특정 지역의 약속을 찾을 수 있도록 위치 기반 검색 기능을 제공하기 위해
     * 
     * GET /api/meetings/search/location?location={locationName}&page={page}&size={size}
     */
    @GetMapping("/location")
    public ResponseEntity<MeetingSearchResponse> searchByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("장소별 검색 요청 - 사용자: {}, 장소: {}, 페이지: {}", currentUserId, location, page);
        
        try {
            MeetingSearchRequest request = new MeetingSearchRequest();
            request.setLocationName(location);
            request.setPage(page);
            request.setSize(size);
            
            MeetingSearchResponse response = meetingSearchService.searchMeetings(request, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("장소별 검색 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 인기 약속을 조회하는 엔드포인트
     * 이유: 참여자 수가 많은 인기 약속을 우선적으로 보여주어 사용자가 활성도 높은 약속을 
     * 쉽게 발견할 수 있도록 하기 위해
     * 
     * GET /api/meetings/search/popular?limit={limit}
     */
    @GetMapping("/popular")
    public ResponseEntity<List<MeetingSummaryResponse>> getPopularMeetings(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("인기 약속 조회 요청 - 사용자: {}, 제한: {}", currentUserId, limit);
        
        try {
            List<MeetingSummaryResponse> popularMeetings = 
                meetingSearchService.getPopularMeetings(limit, currentUserId);
            return ResponseEntity.ok(popularMeetings);
        } catch (Exception e) {
            log.error("인기 약속 조회 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 최근 생성된 약속을 조회하는 엔드포인트
     * 이유: 새로 생성된 약속을 우선적으로 보여주어 사용자가 최신 약속을 빠르게 확인할 수 있도록 하기 위해
     * 
     * GET /api/meetings/search/recent?limit={limit}
     */
    @GetMapping("/recent")
    public ResponseEntity<List<MeetingSummaryResponse>> getRecentMeetings(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("최근 약속 조회 요청 - 사용자: {}, 제한: {}", currentUserId, limit);
        
        try {
            List<MeetingSummaryResponse> recentMeetings = 
                meetingSearchService.getRecentMeetings(limit, currentUserId);
            return ResponseEntity.ok(recentMeetings);
        } catch (Exception e) {
            log.error("최근 약속 조회 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 시간 범위로 약속을 검색하는 엔드포인트
     * 이유: 사용자가 특정 기간의 약속만 조회할 수 있도록 시간 기반 필터링 기능을 제공하기 위해
     * 
     * GET /api/meetings/search/time?start={startTime}&end={endTime}&page={page}&size={size}
     */
    @GetMapping("/time")
    public ResponseEntity<MeetingSearchResponse> searchByTimeRange(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestHeader("X-User-ID") Long currentUserId) {
        
        log.info("시간 범위 검색 요청 - 사용자: {}, 시작: {}, 종료: {}, 페이지: {}", 
                currentUserId, start, end, page);
        
        try {
            MeetingSearchRequest request = new MeetingSearchRequest();
            if (start != null) {
                request.setStartTime(java.time.LocalDateTime.parse(start));
            }
            if (end != null) {
                request.setEndTime(java.time.LocalDateTime.parse(end));
            }
            request.setPage(page);
            request.setSize(size);
            
            MeetingSearchResponse response = meetingSearchService.searchMeetings(request, currentUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("시간 범위 검색 실패 - 에러: {}", e.getMessage());
            throw e;
        }
    }
}
