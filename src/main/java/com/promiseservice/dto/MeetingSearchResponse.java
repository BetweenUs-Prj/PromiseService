package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 약속 검색 결과를 위한 응답 DTO
 * 이유: 검색 결과와 함께 페이지네이션 정보와 검색 요약 정보를 체계적으로 제공하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class MeetingSearchResponse {

    private List<MeetingSummaryResponse> meetings;
    private PageInfo pageInfo;
    private SearchSummary searchSummary;

    /**
     * 검색 결과 응답 생성자
     * 이유: 검색 결과, 페이지 정보, 검색 요약을 한 번에 설정하여 응답 객체 초기화
     */
    public MeetingSearchResponse(List<MeetingSummaryResponse> meetings, PageInfo pageInfo, SearchSummary searchSummary) {
        this.meetings = meetings;
        this.pageInfo = pageInfo;
        this.searchSummary = searchSummary;
    }

        /**
         * 페이지네이션 정보를 담는 내부 클래스
         * 이유: 검색 결과의 페이지 정보를 체계적으로 관리하여 프론트엔드에서 페이지네이션 UI 구현 지원
         */
        @Getter
        @Setter
        @NoArgsConstructor
        public static class PageInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private int pageSize;
        private boolean hasNext;
        private boolean hasPrevious;
    }

        /**
         * 검색 요약 정보를 담는 내부 클래스
         * 이유: 적용된 검색 조건과 정렬 옵션을 요약하여 사용자가 검색 결과를 이해하기 쉽게 제공
         */
        @Getter
        @Setter
        @NoArgsConstructor
        public static class SearchSummary {
        private String keyword;
        private String status;
        private String locationName;
        private Long hostId;
        private int appliedFilters;
        private String sortBy;
        private String sortOrder;
    }
}
