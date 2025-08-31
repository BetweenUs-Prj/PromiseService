package com.promiseservice.service;

import com.promiseservice.model.entity.Meeting;
import com.promiseservice.model.entity.Meeting.MeetingStatus;
import com.promiseservice.repository.MeetingRepository;
import com.promiseservice.repository.MeetingParticipantRepository;
import com.promiseservice.dto.MeetingSearchRequest;
import com.promiseservice.dto.MeetingSearchResponse;
import com.promiseservice.dto.MeetingSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 약속 검색 및 필터링을 담당하는 서비스
 * 이유: 사용자가 다양한 조건으로 약속을 검색할 수 있도록 고급 검색 기능을 제공하고, 
 * 검색 결과의 페이지네이션과 정렬을 체계적으로 관리하기 위해
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingSearchService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;

    /**
     * 고급 약속 검색을 수행하는 메서드
     * 이유: 다양한 검색 조건을 조합하여 사용자가 원하는 약속을 효율적으로 찾을 수 있도록 
     * 복합 검색 기능을 제공하기 위해
     * 
     * @param request 검색 요청 조건
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 검색 결과와 페이지네이션 정보를 포함한 응답
     */
    public MeetingSearchResponse searchMeetings(MeetingSearchRequest request, Long currentUserId) {
        log.info("약속 검색 시작 - 검색 조건: {}", request);

        // 정렬 설정 생성
        // 이유: 사용자가 요청한 정렬 기준과 순서에 따라 검색 결과를 정렬하기 위해
        Sort sort = createSort(request.getSortBy(), request.getSortOrder());
        
        // 페이지네이션 설정 생성
        // 이유: 대량의 검색 결과를 페이지 단위로 나누어 효율적인 데이터 로딩을 제공하기 위해
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 검색 조건에 따른 쿼리 실행
        // 이유: 다양한 검색 조건을 분석하여 적절한 검색 전략을 선택하고 실행하기 위해
        Page<Meeting> meetingPage = executeSearch(request, pageable);

        // 응답 데이터 변환
        // 이유: 엔티티 데이터를 DTO로 변환하여 API 응답에 적합한 형태로 가공하기 위해
        List<MeetingSummaryResponse> meetingSummaries = meetingPage.getContent().stream()
            .map(meeting -> {
                int participantCount = (int) participantRepository.countByMeetingId(meeting.getId());
                boolean isHost = meeting.getHostId().equals(currentUserId);
                return MeetingSummaryResponse.from(meeting, participantCount, isHost);
            })
            .collect(Collectors.toList());

        // 페이지 정보 생성
        // 이유: 프론트엔드에서 페이지네이션 UI를 구현할 수 있도록 페이지 관련 정보를 제공하기 위해
        MeetingSearchResponse.PageInfo pageInfo = createPageInfo(meetingPage);
        
        // 검색 요약 정보 생성
        // 이유: 사용자가 적용된 검색 조건과 정렬 옵션을 확인할 수 있도록 검색 요약 정보를 제공하기 위해
        MeetingSearchResponse.SearchSummary searchSummary = createSearchSummary(request);

        log.info("약속 검색 완료 - 결과 수: {}, 총 페이지: {}", 
                meetingPage.getTotalElements(), meetingPage.getTotalPages());

        return new MeetingSearchResponse(meetingSummaries, pageInfo, searchSummary);
    }

    /**
     * 검색 조건에 따른 적절한 검색 쿼리를 실행하는 메서드
     * 이유: 다양한 검색 조건을 분석하여 최적의 검색 전략을 선택하고 실행하여 검색 성능을 최적화하기 위해
     * 
     * @param request 검색 요청 조건
     * @param pageable 페이지네이션 정보
     * @return 검색 결과 페이지
     */
    private Page<Meeting> executeSearch(MeetingSearchRequest request, Pageable pageable) {
        // 기본 검색 조건 확인 (키워드, 상태, 시간 범위)
        // 이유: 가장 일반적인 검색 조건을 우선적으로 처리하여 검색 효율성 향상
        if (request.getKeyword() != null || request.getStatus() != null || 
            request.getStartTime() != null || request.getEndTime() != null) {
            return searchWithBasicFilters(request, pageable);
        }
        
        // 장소 기반 검색 조건 확인
        // 이유: 위치 기반 검색은 사용자가 자주 사용하는 검색 방식이므로 우선 처리
        if (request.getLocationName() != null) {
            return searchByLocation(request, pageable);
        }
        
        // 참여자 기반 검색 조건 확인
        // 이유: 특정 사용자와의 관계를 통한 검색을 지원하여 소셜 기능 강화
        if (request.getParticipantUserIds() != null && !request.getParticipantUserIds().isEmpty()) {
            return searchByParticipants(request, pageable);
        }
        
        // 방장 기반 검색은 제거됨 (hostId 필드 삭제)
        
        // 기본 검색 (모든 약속)
        // 이유: 검색 조건이 없는 경우 전체 약속 목록을 제공하여 기본적인 조회 기능 보장
        return meetingRepository.findAll(pageable);
    }

    /**
     * 기본 필터를 사용한 검색
     */
    private Page<Meeting> searchWithBasicFilters(MeetingSearchRequest request, Pageable pageable) {
        // 실제 구현에서는 Specification이나 QueryDSL을 사용하여 동적 쿼리 생성
        // 여기서는 간단한 예시로 구현
        
        if (request.getStatus() != null) {
            MeetingStatus status = MeetingStatus.valueOf(request.getStatus().toUpperCase());
            return meetingRepository.findByStatusOrderByMeetingTimeAsc(status, pageable);
        }
        
        if (request.getKeyword() != null) {
            return meetingRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                request.getKeyword(), pageable);
        }
        
        // 시간 범위 검색은 별도 메서드로 구현
        return searchByTimeRange(request, pageable);
    }

    /**
     * 시간 범위로 검색
     */
    private Page<Meeting> searchByTimeRange(MeetingSearchRequest request, Pageable pageable) {
        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now();
        LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : 
                               startTime.plusMonths(1); // 기본값: 1개월
        
        List<Meeting> meetings = meetingRepository.findMeetingsByTimeRange(startTime, endTime);
        
        // Page 객체로 변환 (실제로는 Repository에서 Page를 반환하도록 수정 필요)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), meetings.size());
        
        List<Meeting> pageContent = meetings.subList(start, end);
        
        // 임시 Page 객체 생성 (실제 구현에서는 Repository 수정 필요)
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, meetings.size());
    }

    /**
     * 장소로 검색
     */
    private Page<Meeting> searchByLocation(MeetingSearchRequest request, Pageable pageable) {
        // 장소명으로 검색 (실제로는 Repository에 메서드 추가 필요)
        List<Meeting> meetings = meetingRepository.findByLocationNameContainingIgnoreCase(
            request.getLocationName());
        
        return createPageFromList(meetings, pageable);
    }

    /**
     * 참여자로 검색
     */
    private Page<Meeting> searchByParticipants(MeetingSearchRequest request, Pageable pageable) {
        // 참여자 ID로 검색
        List<Meeting> meetings = request.getParticipantUserIds().stream()
            .flatMap(userId -> meetingRepository.findMeetingsByParticipantUserId(userId).stream())
            .distinct()
            .collect(Collectors.toList());
        
        return createPageFromList(meetings, pageable);
    }



    /**
     * List를 Page로 변환
     */
    private Page<Meeting> createPageFromList(List<Meeting> meetings, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), meetings.size());
        
        List<Meeting> pageContent = meetings.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, meetings.size());
    }

    /**
     * 정렬 설정 생성
     */
    private Sort createSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        switch (sortBy.toLowerCase()) {
            case "title":
                return Sort.by(direction, "title");
            case "createdat":
                return Sort.by(direction, "createdAt");
            case "meetingtime":
            default:
                return Sort.by(direction, "meetingTime");
        }
    }

    /**
     * 페이지 정보 생성
     */
    private MeetingSearchResponse.PageInfo createPageInfo(Page<Meeting> meetingPage) {
        MeetingSearchResponse.PageInfo pageInfo = new MeetingSearchResponse.PageInfo();
        pageInfo.setCurrentPage(meetingPage.getNumber());
        pageInfo.setTotalPages(meetingPage.getTotalPages());
        pageInfo.setTotalElements(meetingPage.getTotalElements());
        pageInfo.setPageSize(meetingPage.getSize());
        pageInfo.setHasNext(meetingPage.hasNext());
        pageInfo.setHasPrevious(meetingPage.hasPrevious());
        return pageInfo;
    }

    /**
     * 검색 요약 정보 생성
     */
    private MeetingSearchResponse.SearchSummary createSearchSummary(MeetingSearchRequest request) {
        MeetingSearchResponse.SearchSummary summary = new MeetingSearchResponse.SearchSummary();
        summary.setKeyword(request.getKeyword());
        summary.setStatus(request.getStatus());
        summary.setLocationName(request.getLocationName());
        summary.setSortBy(request.getSortBy());
        summary.setSortOrder(request.getSortOrder());
        
        // 적용된 필터 수 계산
        int appliedFilters = 0;
        if (request.getKeyword() != null) appliedFilters++;
        if (request.getStatus() != null) appliedFilters++;
        if (request.getStartTime() != null) appliedFilters++;
        if (request.getEndTime() != null) appliedFilters++;
        if (request.getLocationName() != null) appliedFilters++;
        if (request.getParticipantUserIds() != null && !request.getParticipantUserIds().isEmpty()) appliedFilters++;
        
        summary.setAppliedFilters(appliedFilters);
        return summary;
    }

    /**
     * 인기 약속 조회 (참여자 수 기준)
     */
    public List<MeetingSummaryResponse> getPopularMeetings(int limit, Long currentUserId) {
        log.info("인기 약속 조회 - 제한: {}", limit);
        
        // 참여자 수가 많은 순으로 정렬 (실제로는 Repository에 메서드 추가 필요)
        List<Meeting> popularMeetings = meetingRepository.findAll().stream()
            .sorted((m1, m2) -> {
                long count1 = participantRepository.countByMeetingId(m1.getId());
                long count2 = participantRepository.countByMeetingId(m2.getId());
                return Long.compare(count2, count1); // 내림차순
            })
            .limit(limit)
            .collect(Collectors.toList());
        
        return popularMeetings.stream()
            .map(meeting -> {
                int participantCount = (int) participantRepository.countByMeetingId(meeting.getId());
                boolean isHost = meeting.getHostId().equals(currentUserId);
                return MeetingSummaryResponse.from(meeting, participantCount, isHost);
            })
            .collect(Collectors.toList());
    }

    /**
     * 최근 약속 조회
     */
    public List<MeetingSummaryResponse> getRecentMeetings(int limit, Long currentUserId) {
        log.info("최근 약속 조회 - 제한: {}", limit);
        
        List<Meeting> recentMeetings = meetingRepository.findAll().stream()
            .sorted((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()))
            .limit(limit)
            .collect(Collectors.toList());
        
        return recentMeetings.stream()
            .map(meeting -> {
                int participantCount = (int) participantRepository.countByMeetingId(meeting.getId());
                boolean isHost = meeting.getHostId().equals(currentUserId);
                return MeetingSummaryResponse.from(meeting, participantCount, isHost);
            })
            .collect(Collectors.toList());
    }
}
