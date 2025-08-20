package com.promiseservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 약속 검색 요청을 위한 DTO
 * 이유: 사용자가 다양한 조건으로 약속을 검색할 수 있도록 검색 파라미터를 체계적으로 관리하기 위해
 */
@Getter
@Setter
@NoArgsConstructor
public class MeetingSearchRequest {

    // 제목, 설명에서 검색할 키워드
    // 이유: 사용자가 원하는 약속을 빠르게 찾을 수 있도록 텍스트 기반 검색 제공
    @Size(max = 255, message = "검색어는 255자를 초과할 수 없습니다")
    private String keyword;

    // 약속 상태별 필터링
    // 이유: 특정 상태의 약속만 조회하여 사용자 경험 향상
    private String status;

    // 검색 시작 시간 범위
    // 이유: 특정 기간의 약속만 조회하여 시간 기반 필터링 제공
    private LocalDateTime startTime;

    // 검색 종료 시간 범위
    // 이유: 특정 기간의 약속만 조회하여 시간 기반 필터링 제공
    private LocalDateTime endTime;

    // 장소명 기반 필터링
    // 이유: 특정 지역의 약속을 찾을 수 있도록 위치 기반 검색 제공
    @Size(max = 500, message = "장소명은 500자를 초과할 수 없습니다")
    private String locationName;

    // 방장 ID 기반 필터링
    // 이유: 특정 사용자가 방장인 약속만 조회할 수 있도록 개인화된 검색 제공
    private Long hostId;

    // 참여자 ID 목록 기반 필터링
    // 이유: 특정 사용자들이 참여하는 약속을 찾을 수 있도록 관계 기반 검색 제공
    private List<Long> participantUserIds;

    // 최소 참여자 수 제한
    // 이유: 참여자 수가 적은 약속을 제외하여 활성도가 높은 약속만 조회
    private Integer minParticipants;

    // 최대 참여자 수 제한
    // 이유: 참여자 수가 많은 약속을 제외하여 소규모 약속만 조회
    private Integer maxParticipants;

    // 정렬 기준 필드
    // 이유: 사용자가 원하는 순서로 약속 목록을 정렬할 수 있도록 정렬 옵션 제공
    private String sortBy = "meetingTime";

    // 정렬 순서 (오름차순/내림차순)
    // 이유: 사용자가 원하는 방향으로 약속 목록을 정렬할 수 있도록 정렬 방향 제어
    private String sortOrder = "ASC";

    // 페이지 번호 (0부터 시작)
    // 이유: 대량의 검색 결과를 페이지 단위로 나누어 효율적인 데이터 로딩 제공
    private Integer page = 0;

    // 페이지 크기
    // 이유: 한 번에 로드할 데이터 양을 제어하여 성능 최적화 및 사용자 경험 향상
    private Integer size = 20;
}
