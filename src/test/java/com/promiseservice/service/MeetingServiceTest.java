package com.promiseservice.service;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.entity.MeetingHistory;
import com.promiseservice.domain.entity.MeetingParticipant;
import com.promiseservice.domain.repository.MeetingRepository;
import com.promiseservice.domain.repository.MeetingHistoryRepository;
import com.promiseservice.domain.repository.MeetingParticipantRepository;
import com.promiseservice.dto.MeetingCreateRequest;
import com.promiseservice.dto.MeetingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MeetingService 단위 테스트
 * 이유: MeetingService의 비즈니스 로직을 독립적으로 검증하여 서비스 계층의 안정성을 보장하기 위해
 */
@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingParticipantRepository participantRepository;

    @Mock
    private MeetingHistoryRepository historyRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MeetingService meetingService;

    private static final Long TEST_USER_ID = 123L;
    private Meeting testMeeting;
    private MeetingCreateRequest testRequest;

    @BeforeEach
    void setUp() {
        // 테스트 이유: 각 테스트마다 일관된 초기 상태를 보장하기 위해
        
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setHostId(TEST_USER_ID);
        testMeeting.setTitle("테스트 약속");
        testMeeting.setDescription("테스트 설명");
        testMeeting.setMeetingTime(LocalDateTime.now().plusDays(1));
        testMeeting.setLocationName("테스트 장소");
        testMeeting.setStatus(Meeting.MeetingStatus.WAITING);

        testRequest = new MeetingCreateRequest();
        testRequest.setTitle("테스트 약속");
        testRequest.setDescription("테스트 설명");
        testRequest.setMeetingTime(LocalDateTime.now().plusDays(1));
        testRequest.setMaxParticipants(5);
        testRequest.setLocationName("테스트 장소");
        testRequest.setLocationAddress("서울시 강남구");
        testRequest.setLocationCoordinates("{\"lat\": 37.123, \"lng\": 127.456}");
        testRequest.setParticipantUserIds(Arrays.asList(456L, 789L));
    }

    @Test
    @DisplayName("약속 생성 성공 테스트")
    // 테스트 이유: 올바른 요청으로 약속이 정상적으로 생성되는지 비즈니스 로직 검증
    void should_CreateMeeting_When_ValidRequest() {
        // Given
        // 참여자 유저들의 존재 여부 검증 (hostId는 검증하지 않음)
        when(userService.existsUser(456L)).thenReturn(true);
        when(userService.existsUser(789L)).thenReturn(true);
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);
        when(participantRepository.save(any(MeetingParticipant.class))).thenReturn(new MeetingParticipant());
        when(historyRepository.save(any(MeetingHistory.class))).thenReturn(new MeetingHistory());
        doNothing().when(notificationService).sendMeetingCreatedNotification(any(Meeting.class));

        // When
        MeetingResponse response = meetingService.createMeeting(testRequest, TEST_USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getHostId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getTitle()).isEqualTo("테스트 약속");
        assertThat(response.getStatus()).isEqualTo("WAITING");

        // 검증: 저장소 메서드들이 올바르게 호출되었는지 확인
        verify(meetingRepository, times(1)).save(any(Meeting.class));
        verify(participantRepository, times(3)).save(any(MeetingParticipant.class)); // 호스트 1명 + 초대자 2명 = 3명
        verify(historyRepository, times(1)).save(any(MeetingHistory.class));
        verify(notificationService, times(1)).sendMeetingCreatedNotification(any(Meeting.class));
    }

    @Test
    @DisplayName("약속 생성 실패 - 존재하지 않는 사용자 초대")
    // 테스트 이유: 존재하지 않는 사용자를 초대할 때 적절한 예외가 발생하는지 검증
    void should_ThrowException_When_InvitingNonExistentUser() {
        // Given
        // hostId는 검증하지 않음, 참여자만 검증
        when(userService.existsUser(456L)).thenReturn(true);
        when(userService.existsUser(789L)).thenReturn(false); // 존재하지 않는 사용자

        // When & Then
        assertThatThrownBy(() -> meetingService.createMeeting(testRequest, TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("존재하지 않는 사용자");

        // 검증: 예외 발생으로 인해 저장 관련 메서드들이 호출되지 않았는지 확인
        // 이유: 존재하지 않는 사용자 초대 시 트랜잭션이 롤백되어야 하므로
        verify(meetingRepository, atMost(1)).save(any(Meeting.class)); // Meeting은 저장될 수 있음
        verify(participantRepository, atMost(2)).save(any(MeetingParticipant.class)); // 호스트만 저장될 수 있음
        verify(historyRepository, never()).save(any(MeetingHistory.class)); // 히스토리는 마지막에 저장되므로 저장되지 않아야 함
        verify(notificationService, never()).sendMeetingCreatedNotification(any(Meeting.class));
    }

    @Test
    @DisplayName("약속 조회 성공 테스트")
    // 테스트 이유: 존재하는 약속을 정상적으로 조회할 수 있는지 검증
    void should_GetMeeting_When_MeetingExists() {
        // Given
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));

        // When
        MeetingResponse response = meetingService.getMeeting(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("테스트 약속");
        assertThat(response.getHostId()).isEqualTo(TEST_USER_ID);

        verify(meetingRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("약속 조회 실패 - 존재하지 않는 약속")
    // 테스트 이유: 존재하지 않는 약속 조회 시 적절한 예외가 발생하는지 검증
    void should_ThrowException_When_MeetingNotExists() {
        // Given
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> meetingService.getMeeting(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("약속을 찾을 수 없습니다");

        verify(meetingRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("방장의 약속 목록 조회 테스트")
    // 테스트 이유: 특정 사용자가 방장으로 생성한 약속 목록이 올바르게 조회되는지 검증
    void should_GetMeetingsByHost_When_ValidHostId() {
        // Given
        List<Meeting> meetings = Arrays.asList(testMeeting, createAnotherTestMeeting());
        when(meetingRepository.findByHostIdOrderByCreatedAtDesc(TEST_USER_ID)).thenReturn(meetings);

        // When
        List<MeetingResponse> responses = meetingService.getMeetingsByHost(TEST_USER_ID);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getHostId()).isEqualTo(TEST_USER_ID);
        assertThat(responses.get(1).getHostId()).isEqualTo(TEST_USER_ID);

        verify(meetingRepository, times(1)).findByHostIdOrderByCreatedAtDesc(TEST_USER_ID);
    }

    @Test
    @DisplayName("약속 상태 변경 성공 테스트")
    // 테스트 이유: 방장이 약속 상태를 정상적으로 변경할 수 있는지 검증
    void should_UpdateMeetingStatus_When_HostRequestsChange() {
        // Given
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(historyRepository.save(any(MeetingHistory.class))).thenReturn(new MeetingHistory());

        // When
        MeetingResponse response = meetingService.updateMeetingStatus(1L, Meeting.MeetingStatus.CONFIRMED, TEST_USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(testMeeting.getStatus()).isEqualTo(Meeting.MeetingStatus.CONFIRMED);

        verify(meetingRepository, times(1)).findById(1L);
        verify(historyRepository, times(1)).save(any(MeetingHistory.class));
    }

    @Test
    @DisplayName("약속 상태 변경 실패 - 권한 없음")
    // 테스트 이유: 방장이 아닌 사용자가 상태 변경을 시도할 때 적절한 예외가 발생하는지 검증
    void should_ThrowException_When_NonHostTriesToChangeStatus() {
        // Given
        Long otherUserId = 999L;
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));

        // When & Then
        assertThatThrownBy(() -> meetingService.updateMeetingStatus(1L, Meeting.MeetingStatus.CONFIRMED, otherUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("약속 상태 변경 권한이 없습니다");

        verify(meetingRepository, times(1)).findById(1L);
        verify(historyRepository, never()).save(any(MeetingHistory.class));
    }

    @Test
    @DisplayName("약속 삭제 성공 테스트")
    // 테스트 이유: 방장이 자신의 약속을 정상적으로 삭제할 수 있는지 검증
    void should_DeleteMeeting_When_HostRequestsDeletion() {
        // Given
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));
        when(historyRepository.save(any(MeetingHistory.class))).thenReturn(new MeetingHistory());
        doNothing().when(meetingRepository).delete(testMeeting);

        // When
        assertThatCode(() -> meetingService.deleteMeeting(1L, TEST_USER_ID))
                .doesNotThrowAnyException();

        // Then
        verify(meetingRepository, times(1)).findById(1L);
        verify(historyRepository, times(1)).save(any(MeetingHistory.class));
        verify(meetingRepository, times(1)).delete(testMeeting);
    }

    @Test
    @DisplayName("약속 삭제 실패 - 권한 없음")
    // 테스트 이유: 방장이 아닌 사용자가 삭제를 시도할 때 적절한 예외가 발생하는지 검증
    void should_ThrowException_When_NonHostTriesToDelete() {
        // Given
        Long otherUserId = 999L;
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));

        // When & Then
        assertThatThrownBy(() -> meetingService.deleteMeeting(1L, otherUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("약속 삭제 권한이 없습니다");

        verify(meetingRepository, times(1)).findById(1L);
        verify(meetingRepository, never()).delete(any(Meeting.class));
    }

    /**
     * 추가 테스트용 약속 생성 헬퍼 메서드
     * 이유: 테스트 코드의 중복을 줄이고 일관된 테스트 데이터를 생성하기 위해
     */
    private Meeting createAnotherTestMeeting() {
        Meeting meeting = new Meeting();
        meeting.setId(2L);
        meeting.setHostId(TEST_USER_ID);
        meeting.setTitle("또 다른 테스트 약속");
        meeting.setDescription("또 다른 테스트 설명");
        meeting.setMeetingTime(LocalDateTime.now().plusDays(2));
        meeting.setLocationName("또 다른 테스트 장소");
        meeting.setStatus(Meeting.MeetingStatus.WAITING);
        return meeting;
    }
}
