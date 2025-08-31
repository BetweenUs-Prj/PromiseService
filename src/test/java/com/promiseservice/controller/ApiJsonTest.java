package com.promiseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promiseservice.config.TestConfig;
import com.promiseservice.config.JpaH2TestConfig;
import com.promiseservice.dto.MeetingCreateRequest;
import com.promiseservice.service.MeetingService;
import com.promiseservice.service.NotificationService;
import com.promiseservice.service.UserService;
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

import com.promiseservice.dto.MeetingResponse;
import com.promiseservice.dto.ParticipantResponse;
import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.service.ParticipantService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API JSON 테스트 클래스
 * 이유: MockMvc를 활용하여 실제 HTTP JSON 요청/응답을 테스트하고, API 스펙을 검증하기 위해
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import({TestConfig.class, JpaH2TestConfig.class})
@Transactional
class ApiJsonTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private MeetingService meetingService;

    @MockBean
    private ParticipantService participantService;

    private static final Long TEST_USER_ID = 123L;

    @BeforeEach
    void setUp() {
        // 테스트 이유: 외부 의존성 Mocking으로 독립적인 API 테스트 환경 구축
        
        // UserService Mock 설정
        when(userService.existsUser(any(Long.class))).thenReturn(true);
        
        // NotificationService Mock 설정
        doNothing().when(notificationService).sendMeetingCreatedNotification(any());
        
        // MeetingService Mock 설정
        MeetingResponse mockResponse = createMockMeetingResponse();
        when(meetingService.createMeeting(any(), any())).thenReturn(mockResponse);
        when(meetingService.getMeetingsByHost(any())).thenReturn(Arrays.asList(mockResponse));
    }
    
    private MeetingResponse createMockMeetingResponse() {
        MeetingResponse response = new MeetingResponse();
        response.setId(1L);
        response.setHostId(TEST_USER_ID);
        response.setTitle("JSON 테스트 약속");
        response.setDescription("MockMvc로 테스트하는 약속");
        response.setStatus("WAITING");
        response.setLocationName("강남역");
        response.setLocationAddress("서울시 강남구 강남대로");
        response.setLocationCoordinates("{\"lat\": 37.498095, \"lng\": 127.027621}");
        response.setMaxParticipants(5);
        // 테스트용 참여자 데이터 생성
        // 이유: 복잡한 JSON 구조 검증 테스트에서 participants 배열의 구조를 검증하기 위해
        List<ParticipantResponse> participants = Arrays.asList(
            createMockParticipant(123L, "ACCEPTED"),  // 호스트
            createMockParticipant(456L, "INVITED"),   // 참여자 1
            createMockParticipant(789L, "INVITED"),   // 참여자 2  
            createMockParticipant(101L, "INVITED")    // 추가 참여자
        );
        response.setParticipants(participants);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    /**
     * 테스트용 Mock 참여자 객체 생성
     * 이유: JSON 응답 테스트에서 participants 배열의 구조를 검증하기 위해 필요한 Mock 데이터 생성
     */
    private ParticipantResponse createMockParticipant(Long userId, String response) {
        ParticipantResponse participant = new ParticipantResponse();
        participant.setId(1L);
        participant.setUserId(userId);
        participant.setResponse(response);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setInvitedAt(LocalDateTime.now());
        participant.setRespondedAt(LocalDateTime.now());
        return participant;
    }

    @Test
    @DisplayName("약속 생성 API JSON 테스트")
    void should_CreateMeeting_When_ValidJsonRequest() throws Exception {
        // 테스트 이유: JSON 요청 형태의 약속 생성 API가 올바른 JSON 응답을 반환하는지 검증

        // Given - JSON 요청 데이터
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setTitle("JSON 테스트 약속");
        request.setDescription("MockMvc로 테스트하는 약속");
        request.setMeetingTime(LocalDateTime.of(2025, 8, 20, 14, 0));
        request.setMaxParticipants(5);
        request.setLocationName("강남역");
        request.setLocationAddress("서울시 강남구 강남대로");
        request.setLocationCoordinates("{\"lat\": 37.498095, \"lng\": 127.027621}");
        request.setParticipantUserIds(Arrays.asList(456L, 789L));

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then - API 호출 및 JSON 응답 검증
        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-ID", TEST_USER_ID)
                        .content(requestJson))
                .andDo(print()) // 요청/응답 출력
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("JSON 테스트 약속"))
                .andExpect(jsonPath("$.description").value("MockMvc로 테스트하는 약속"))
                .andExpect(jsonPath("$.hostId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.locationName").value("강남역"))
                .andExpect(jsonPath("$.locationAddress").value("서울시 강남구 강남대로"))
                .andExpect(jsonPath("$.locationCoordinates").value("{\"lat\": 37.498095, \"lng\": 127.027621}"))
                .andExpect(jsonPath("$.maxParticipants").value(5))
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants.length()").value(4)) // Mock에서 4개 participants 반환
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("약속 목록 조회 API JSON 테스트")
    void should_ReturnMeetingList_When_ValidJsonRequest() throws Exception {
        // 테스트 이유: 약속 목록 조회 API가 올바른 JSON 배열 형태로 응답하는지 검증

        // When & Then
        mockMvc.perform(get("/api/meetings/host/{hostId}", TEST_USER_ID)
                        .header("X-User-ID", TEST_USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("잘못된 JSON 요청 시 400 에러 반환 테스트")
    void should_ReturnBadRequest_When_InvalidJsonRequest() throws Exception {
        // 테스트 이유: 잘못된 JSON 형태의 요청에 대해 적절한 에러 응답을 반환하는지 검증

        // Given - 잘못된 JSON (필수 필드 누락)
        String invalidJson = "{\"title\": \"\"}"; // title이 비어있음

        // When & Then
        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-ID", TEST_USER_ID)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("헤더 누락 시 400 에러 반환 테스트")
    void should_ReturnBadRequest_When_MissingUserIdHeader() throws Exception {
        // 테스트 이유: 필수 헤더(X-User-ID) 누락 시 적절한 에러 응답을 반환하는지 검증

        // Given
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setTitle("헤더 누락 테스트");
        request.setMeetingTime(LocalDateTime.of(2025, 8, 20, 14, 0));
        request.setMaxParticipants(5);

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then - X-User-ID 헤더 없이 요청
        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("복잡한 JSON 응답 구조 검증 테스트")
    void should_ReturnComplexJsonStructure_When_ValidRequest() throws Exception {
        // 테스트 이유: 중첩된 JSON 구조(참여자 정보 등)가 올바르게 직렬화되는지 검증

        // Given
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setTitle("복잡한 JSON 테스트");
        request.setDescription("중첩 구조 검증");
        request.setMeetingTime(LocalDateTime.of(2025, 8, 20, 15, 30));
        request.setMaxParticipants(10);
        request.setLocationName("홍대입구역");
        request.setLocationAddress("서울시 마포구 양화로");
        request.setLocationCoordinates("{\"lat\": 37.557527, \"lng\": 126.925320}");
        request.setParticipantUserIds(Arrays.asList(456L, 789L, 101L));

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then - 복잡한 JSON 구조 검증
        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-ID", TEST_USER_ID)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants[0].userId").exists())
                .andExpect(jsonPath("$.participants[0].response").exists())
                .andExpect(jsonPath("$.participants[0].joinedAt").exists())
                .andExpect(jsonPath("$.participants[1].userId").value(456))
                .andExpect(jsonPath("$.participants[1].response").value("INVITED"))
                .andExpect(jsonPath("$.participants[2].userId").value(789))
                .andExpect(jsonPath("$.participants[2].response").value("INVITED"))
                .andExpect(jsonPath("$.participants[3].userId").value(101))
                .andExpect(jsonPath("$.participants[3].response").value("INVITED"));
    }
}
