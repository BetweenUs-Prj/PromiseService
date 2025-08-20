package com.promiseservice.repository;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.entity.MeetingParticipant;
import com.promiseservice.domain.entity.User;
import com.promiseservice.domain.repository.MeetingRepository;
import com.promiseservice.domain.repository.MeetingParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * MeetingRepository H2 테스트
 * 이유: H2 인메모리 데이터베이스를 사용하여 빠르고 독립적인 Repository 계층 테스트 수행
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class MeetingRepositoryTestH2 {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingParticipantRepository participantRepository;

    private User testUser;
    private Meeting testMeeting;

    @BeforeEach
    void setUp() {
        // 테스트 이유: 각 테스트가 독립적으로 실행될 수 있도록 기본 데이터 준비
        
        // 테스트용 사용자 생성
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("테스트 사용자");
        testUser.setProvider("test");  // 필수값: OAuth 제공자
        testUser.setProviderId("test123");  // 필수값: OAuth 제공자 ID
        testUser.setRole("USER");  // 필수값: 사용자 역할
        testUser = entityManager.persistAndFlush(testUser);

        // 테스트용 약속 생성
        testMeeting = new Meeting();
        testMeeting.setHostId(testUser.getId());
        testMeeting.setTitle("테스트 약속");
        testMeeting.setDescription("Repository 테스트용 약속");
        testMeeting.setMeetingTime(LocalDateTime.of(2025, 8, 20, 14, 0));
        testMeeting.setMaxParticipants(5);
        testMeeting.setStatus(Meeting.MeetingStatus.WAITING);
        testMeeting.setLocationName("강남역");
        testMeeting.setLocationAddress("서울시 강남구");
        testMeeting.setLocationCoordinates("{\"lat\": 37.498095, \"lng\": 127.027621}");
        testMeeting = entityManager.persistAndFlush(testMeeting);

        // 캐시 클리어
        entityManager.clear();
    }

    @Test
    @DisplayName("약속 저장 및 조회 테스트")
    void should_SaveAndFindMeeting_When_ValidData() {
        // 테스트 이유: 기본적인 CRUD 동작이 정상적으로 작동하는지 검증

        // When
        Optional<Meeting> found = meetingRepository.findById(testMeeting.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("테스트 약속");
        assertThat(found.get().getHostId()).isEqualTo(testUser.getId());
        assertThat(found.get().getStatus()).isEqualTo(Meeting.MeetingStatus.WAITING);
    }

    @Test
    @DisplayName("방장 ID로 약속 목록 조회 테스트")
    void should_FindMeetingsByHost_When_ValidHostId() {
        // 테스트 이유: 특정 사용자가 생성한 약속들을 조회하는 기능 검증

        // When
        List<Meeting> meetings = meetingRepository.findByHostIdOrderByCreatedAtDesc(testUser.getId());

        // Then
        assertThat(meetings).hasSize(1);
        assertThat(meetings.get(0).getTitle()).isEqualTo("테스트 약속");
        assertThat(meetings.get(0).getHostId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("약속 상태별 조회 및 카운트 테스트")
    void should_FindAndCountByStatus_When_ValidStatus() {
        // 테스트 이유: 약속 상태별 필터링과 통계 기능 검증

        // When
        List<Meeting> waitingMeetings = meetingRepository.findByStatusOrderByMeetingTimeAsc(Meeting.MeetingStatus.WAITING, PageRequest.of(0, 10)).getContent();
        long waitingCount = meetingRepository.countByStatus(Meeting.MeetingStatus.WAITING);

        // Then
        assertThat(waitingMeetings).hasSize(1);
        assertThat(waitingCount).isEqualTo(1);
        assertThat(waitingMeetings.get(0).getStatus()).isEqualTo(Meeting.MeetingStatus.WAITING);
    }

    @Test
    @DisplayName("제목으로 약속 검색 테스트")
    void should_FindMeetingsByTitle_When_SearchKeyword() {
        // 테스트 이유: 제목 검색 기능이 대소문자 구분 없이 정상 동작하는지 검증

        // When
        List<Meeting> meetings = meetingRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("테스트", PageRequest.of(0, 10)).getContent();

        // Then
        assertThat(meetings).hasSize(1);
        assertThat(meetings.get(0).getTitle()).contains("테스트");
    }

    @Test
    @DisplayName("장소명으로 약속 검색 테스트")
    void should_FindMeetingsByLocation_When_SearchLocation() {
        // 테스트 이유: 장소 기반 검색 기능 검증

        // When
        List<Meeting> meetings = meetingRepository.findByLocationNameContainingIgnoreCase("강남");

        // Then
        assertThat(meetings).hasSize(1);
        assertThat(meetings.get(0).getLocationName()).contains("강남");
    }

    @Test
    @DisplayName("시간 범위로 약속 검색 테스트")
    void should_FindMeetingsInTimeRange_When_ValidRange() {
        // 테스트 이유: 날짜/시간 범위 검색 기능 검증

        // Given
        LocalDateTime startTime = LocalDateTime.of(2025, 8, 20, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 8, 20, 23, 59);

        // When
        List<Meeting> meetings = meetingRepository.findByMeetingTimeBetween(startTime, endTime);

        // Then
        assertThat(meetings).hasSize(1);
        assertThat(meetings.get(0).getMeetingTime()).isBetween(startTime, endTime);
    }

    @Test
    @DisplayName("참여자가 있는 약속 조회 테스트")
    void should_FindMeetingsWithParticipants_When_ParticipantsExist() {
        // 테스트 이유: 참여자 관계가 포함된 복합 쿼리 테스트

        // Given - 참여자 추가
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeeting(testMeeting);
        participant.setUserId(testUser.getId());
        participant.setResponse(MeetingParticipant.ResponseStatus.ACCEPTED);
        participant.setJoinedAt(LocalDateTime.now());
        entityManager.persistAndFlush(participant);

        // When
        long participantCount = participantRepository.countByMeetingId(testMeeting.getId());

        // Then
        assertThat(participantCount).isEqualTo(1);
    }

    @Test
    @DisplayName("약속 삭제 테스트")
    void should_DeleteMeeting_When_ValidId() {
        // 테스트 이유: 약속 삭제 기능과 CASCADE 동작 검증

        // When
        meetingRepository.deleteById(testMeeting.getId());
        entityManager.flush();

        // Then
        Optional<Meeting> deleted = meetingRepository.findById(testMeeting.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("JPA Auditing 기능 테스트")
    void should_SetAuditingFields_When_SaveEntity() {
        // 테스트 이유: @CreatedDate, @LastModifiedDate 자동 설정 기능 검증

        // Given
        Meeting newMeeting = new Meeting();
        newMeeting.setHostId(testUser.getId());
        newMeeting.setTitle("Auditing 테스트");
        newMeeting.setMeetingTime(LocalDateTime.of(2025, 8, 21, 15, 0));
        newMeeting.setMaxParticipants(3);
        newMeeting.setStatus(Meeting.MeetingStatus.WAITING);

        // When
        Meeting saved = meetingRepository.save(newMeeting);
        entityManager.flush();

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("페이지네이션 테스트")
    void should_ReturnPagedResults_When_PageableProvided() {
        // 테스트 이유: 페이지네이션 기능이 올바르게 동작하는지 검증

        // Given - 추가 데이터 생성
        for (int i = 2; i <= 5; i++) {
            Meeting meeting = new Meeting();
            meeting.setHostId(testUser.getId());
            meeting.setTitle("테스트 약속 " + i);
            meeting.setMeetingTime(LocalDateTime.of(2025, 8, 20 + i, 14, 0));
            meeting.setMaxParticipants(5);
            meeting.setStatus(Meeting.MeetingStatus.WAITING);
            entityManager.persistAndFlush(meeting);
        }

        // When
        Pageable pageable = PageRequest.of(0, 3);
        Page<Meeting> page = meetingRepository.findByStatusOrderByMeetingTimeAsc(Meeting.MeetingStatus.WAITING, pageable);

        // Then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }
}
