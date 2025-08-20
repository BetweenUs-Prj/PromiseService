package com.promiseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promiseservice.config.TestConfig;
import com.promiseservice.config.JpaH2TestConfig;
import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.repository.MeetingRepository;
import com.promiseservice.dto.MeetingCreateRequest;
import com.promiseservice.service.UserService;
import com.promiseservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MeetingController 통합 테스트
 * 이유: 약속 관리 API의 동작을 검증하여 사용자 요청에 대한 올바른 응답과 비즈니스 로직 실행을 보장하기 위해
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import({TestConfig.class, JpaH2TestConfig.class})
@Transactional
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeetingRepository meetingRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationService notificationService;

    private static final Long TEST_USER_ID = 123L;
    private static final String USER_ID_HEADER = "X-User-ID";

    @BeforeEach
    void setUp() {
        // 테스트 이유: 각 테스트마다 일관된 초기 상태를 보장하기 위해
        
        // UserService Mock 설정 - 사용자 존재 확인
        when(userService.existsUser(any(Long.class))).thenReturn(true);
        
        // NotificationService Mock 설정 - 알림 전송 성공
        doNothing().when(notificationService).sendMeetingCreatedNotification(any(Meeting.class));
    }

    @Test
    @DisplayName("약속 생성 성공 테스트")
    // 테스트 이유: 올바른 요청으로 약속이 정상적으로 생성되는지 검증
    void should_CreateMeeting_When_ValidRequest() throws Exception {
        // Given
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setTitle("테스트 약속");
        request.setDescription("테스트 설명");
        request.setMeetingTime(LocalDateTime.now().plusDays(1));
        request.setMaxParticipants(5);
        request.setLocationName("테스트 장소");
        request.setLocationAddress("서울시 강남구");
        request.setLocationCoordinates("{\"lat\": 37.123, \"lng\": 127.456}");
        request.setParticipantUserIds(Arrays.asList(456L, 789L));

        // When & Then
        mockMvc.perform(post("/api/meetings")
                .header(USER_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hostId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.title").value("테스트 약속"))
                .andExpect(jsonPath("$.description").value("테스트 설명"))
                .andExpect(jsonPath("$.locationName").value("테스트 장소"))
                .andExpect(jsonPath("$.status").value("WAITING"));

        // 알림 전송 확인
        verify(notificationService, times(1)).sendMeetingCreatedNotification(any(Meeting.class));
    }

    @Test
    @DisplayName("약속 생성 실패 - 필수 필드 누락")
    // 테스트 이유: 필수 필드가 누락된 요청에 대해 적절한 validation 에러가 발생하는지 검증
    void should_ReturnBadRequest_When_RequiredFieldMissing() throws Exception {
        // Given - 제목이 누락된 요청
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setDescription("테스트 설명");
        request.setMeetingTime(LocalDateTime.now().plusDays(1));

        // When & Then
        mockMvc.perform(post("/api/meetings")
                .header(USER_ID_HEADER, TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("약속 조회 성공 테스트")
    // 테스트 이유: 존재하는 약속을 정상적으로 조회할 수 있는지 검증
    void should_GetMeeting_When_MeetingExists() throws Exception {
        // Given - 테스트용 약속 생성
        Meeting meeting = new Meeting();
        meeting.setHostId(TEST_USER_ID);
        meeting.setTitle("조회 테스트 약속");
        meeting.setDescription("조회 테스트");
        meeting.setMeetingTime(LocalDateTime.now().plusDays(1));
        meeting.setLocationName("테스트 장소");
        meeting.setStatus(Meeting.MeetingStatus.WAITING);
        Meeting savedMeeting = meetingRepository.save(meeting);

        // When & Then
        mockMvc.perform(get("/api/meetings/{meetingId}", savedMeeting.getId())
                .header(USER_ID_HEADER, TEST_USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMeeting.getId()))
                .andExpect(jsonPath("$.title").value("조회 테스트 약속"))
                .andExpect(jsonPath("$.hostId").value(TEST_USER_ID));
    }

    @Test
    @DisplayName("약속 조회 실패 - 존재하지 않는 약속")
    // 테스트 이유: 존재하지 않는 약속 조회 시 적절한 에러 응답이 반환되는지 검증
    void should_ReturnNotFound_When_MeetingNotExists() throws Exception {
        // Given
        Long nonExistentMeetingId = 99999L;

        // When & Then
        mockMvc.perform(get("/api/meetings/{meetingId}", nonExistentMeetingId)
                .header(USER_ID_HEADER, TEST_USER_ID))
                .andDo(print())
                .andExpect(status().isNotFound()); // GlobalExceptionHandler로 404 처리
    }

    @Test
    @DisplayName("방장의 약속 목록 조회 테스트")
    // 테스트 이유: 특정 사용자가 방장으로 생성한 약속 목록이 올바르게 조회되는지 검증
    void should_GetMeetingsByHost_When_ValidHostId() throws Exception {
        // Given - 테스트용 약속들 생성
        Meeting meeting1 = createTestMeeting("약속1", TEST_USER_ID);
        Meeting meeting2 = createTestMeeting("약속2", TEST_USER_ID);
        meetingRepository.saveAll(Arrays.asList(meeting1, meeting2));

        // When & Then
        mockMvc.perform(get("/api/meetings/host/{hostId}", TEST_USER_ID)
                .header(USER_ID_HEADER, TEST_USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].hostId").value(TEST_USER_ID))
                .andExpect(jsonPath("$[1].hostId").value(TEST_USER_ID));
    }

    @Test
    @DisplayName("약속 삭제 성공 테스트")
    // 테스트 이유: 방장이 자신의 약속을 정상적으로 삭제할 수 있는지 검증
    void should_DeleteMeeting_When_HostRequestsDeletion() throws Exception {
        // Given
        Meeting meeting = createTestMeeting("삭제 테스트 약속", TEST_USER_ID);
        Meeting savedMeeting = meetingRepository.save(meeting);

        // When & Then
        mockMvc.perform(delete("/api/meetings/{meetingId}", savedMeeting.getId())
                .header(USER_ID_HEADER, TEST_USER_ID))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 삭제 확인
        Optional<Meeting> deletedMeeting = meetingRepository.findById(savedMeeting.getId());
        assert deletedMeeting.isEmpty();
    }

    @Test
    @DisplayName("약속 삭제 실패 - 권한 없음")
    // 테스트 이유: 방장이 아닌 사용자가 약속 삭제를 시도할 때 적절한 권한 에러가 발생하는지 검증
    void should_ReturnForbidden_When_NonHostTriesToDelete() throws Exception {
        // Given
        Meeting meeting = createTestMeeting("삭제 테스트 약속", TEST_USER_ID);
        Meeting savedMeeting = meetingRepository.save(meeting);
        Long otherUserId = 999L;

        // When & Then
        mockMvc.perform(delete("/api/meetings/{meetingId}", savedMeeting.getId())
                .header(USER_ID_HEADER, otherUserId))
                .andDo(print())
                .andExpect(status().isForbidden()); // GlobalExceptionHandler로 403 처리
    }

    /**
     * 테스트용 약속 생성 헬퍼 메서드
     * 이유: 테스트 코드의 중복을 줄이고 일관된 테스트 데이터를 생성하기 위해
     */
    private Meeting createTestMeeting(String title, Long hostId) {
        Meeting meeting = new Meeting();
        meeting.setHostId(hostId);
        meeting.setTitle(title);
        meeting.setDescription("테스트 설명");
        meeting.setMeetingTime(LocalDateTime.now().plusDays(1));
        meeting.setLocationName("테스트 장소");
        meeting.setStatus(Meeting.MeetingStatus.WAITING);
        return meeting;
    }
}
