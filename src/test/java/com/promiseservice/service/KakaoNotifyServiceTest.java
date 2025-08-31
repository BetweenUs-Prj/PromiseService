package com.promiseservice.service;

import com.promiseservice.client.KakaoClient;
import com.promiseservice.domain.entity.*;
import com.promiseservice.domain.repository.*;
import com.promiseservice.dto.KakaoNotifyResponse;
import com.promiseservice.dto.TemplatePayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * KakaoNotifyService 테스트 클래스
 * 이유: 카카오톡 알림 전송 로직의 정확성과 예외 처리를 검증하기 위해
 */
@ExtendWith(MockitoExtension.class)
class KakaoNotifyServiceTest {

    @Mock
    private KakaoClient kakaoClient;
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private MeetingParticipantRepository participantRepository;
    @Mock
    private UserKakaoInfoRepository userKakaoInfoRepository;
    @Mock
    private UserConsentRepository userConsentRepository;
    @Mock
    private FriendRepository friendRepository;
    @Mock
    private KakaoFriendMapRepository kakaoFriendMapRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private KakaoNotifyService kakaoNotifyService;

    private Meeting testMeeting;
    private UserKakaoInfo testUserKakaoInfo;
    private UserConsent testUserConsent;

    @BeforeEach
    void setUp() {
        // 테스트용 약속 데이터 생성
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setTitle("테스트 약속");
        testMeeting.setDescription("테스트용 약속입니다");
        testMeeting.setMeetingTime(LocalDateTime.now().plusDays(1));
        testMeeting.setLocationName("강남역");
        testMeeting.setStatus(Meeting.MeetingStatus.CONFIRMED);

        // 테스트용 카카오 정보 생성
        testUserKakaoInfo = new UserKakaoInfo();
        testUserKakaoInfo.setUserId(1L);
        testUserKakaoInfo.setKakaoUuid("test-uuid-123");
        testUserKakaoInfo.setKakaoAccessToken("test-access-token");
        testUserKakaoInfo.setKakaoScopesJson("{\"scopes\":[\"talk_message\",\"friends\"]}");
        testUserKakaoInfo.setTokenExpiresAt(LocalDateTime.now().plusHours(1));

        // 테스트용 동의 정보 생성
        testUserConsent = new UserConsent();
        testUserConsent.setUserId(1L);
        testUserConsent.setTalkMessageConsent(true);
        testUserConsent.setFriendsConsent(true);
    }

    @Test
    void buildTemplatePayload_정상적인_약속정보로_템플릿생성_성공() {
        // Given
        Long inviterId = 1L;
        when(userService.getUserById(inviterId)).thenReturn(createTestUserDto("테스트사용자"));

        // When
        TemplatePayload result = kakaoNotifyService.buildTemplatePayload(testMeeting, inviterId);

        // Then
        assertNotNull(result);
        assertEquals("테스트사용자", result.getInviter());
        assertNotNull(result.getDate());
        assertEquals("강남역", result.getPlace());
        assertEquals("테스트 약속", result.getTitle());
        assertTrue(result.isValid());
    }

    @Test
    void resolveReceivers_수신자목록이_null일때_약속참여자_반환() {
        // Given
        Long inviterId = 1L;
        Long meetingId = 1L;
        List<MeetingParticipant> participants = List.of(
                createTestParticipant(1L, meetingId),
                createTestParticipant(2L, meetingId),
                createTestParticipant(3L, meetingId)
        );
        when(participantRepository.findByMeetingId(meetingId)).thenReturn(participants);

        // When
        List<Long> result = kakaoNotifyService.resolveReceivers(inviterId, meetingId, null);

        // Then
        assertEquals(2, result.size()); // 발송자 제외
        assertFalse(result.contains(inviterId));
        assertTrue(result.contains(2L));
        assertTrue(result.contains(3L));
    }

    @Test
    void resolveReceivers_수신자목록이_지정된경우_발송자제외하고_반환() {
        // Given
        Long inviterId = 1L;
        Long meetingId = 1L;
        List<Long> receiverIds = List.of(1L, 2L, 3L);

        // When
        List<Long> result = kakaoNotifyService.resolveReceivers(inviterId, meetingId, receiverIds);

        // Then
        assertEquals(2, result.size()); // 발송자 제외
        assertFalse(result.contains(inviterId));
        assertTrue(result.contains(2L));
        assertTrue(result.contains(3L));
    }

    @Test
    void filterKakaoDeliverable_모든조건만족시_전송가능사용자_반환() {
        // Given
        Long inviterId = 1L;
        List<Long> receiverIds = List.of(2L, 3L);

        // 발송자 카카오 정보 설정
        when(userKakaoInfoRepository.findByUserId(inviterId))
                .thenReturn(Optional.of(testUserKakaoInfo));

        // 동의한 사용자들
        when(userConsentRepository.findUserIdsWithTalkMessageConsentFromList(receiverIds))
                .thenReturn(List.of(2L, 3L));

        // 친구 관계
        when(friendRepository.findFriendUserIdsFromCandidates(inviterId, List.of(2L, 3L)))
                .thenReturn(List.of(2L, 3L));

        // 카카오 정보가 있는 사용자들
        List<UserKakaoInfo> kakaoUsers = List.of(
                createTestKakaoInfo(2L, "uuid-2"),
                createTestKakaoInfo(3L, "uuid-3")
        );
        when(userKakaoInfoRepository.findByUserIdIn(List.of(2L, 3L)))
                .thenReturn(kakaoUsers);

        // When
        List<UserKakaoInfo> result = kakaoNotifyService.filterKakaoDeliverable(inviterId, receiverIds);

        // Then
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getUserId());
        assertEquals(3L, result.get(1).getUserId());
    }

    @Test
    void send_성공적인_전송시_응답생성() {
        // Given
        Long inviterId = 1L;
        List<UserKakaoInfo> deliverableUsers = List.of(
                createTestKakaoInfo(2L, "uuid-2"),
                createTestKakaoInfo(3L, "uuid-3")
        );
        TemplatePayload templatePayload = new TemplatePayload("테스트사용자", "내일 오후 2시", "강남역", "http://test.com");
        int totalTargetCount = 2;

        // 발송자 카카오 정보
        when(userKakaoInfoRepository.findByUserId(inviterId))
                .thenReturn(Optional.of(testUserKakaoInfo));

        // 카카오 UUID 수집
        when(kakaoFriendMapRepository.findKakaoUuidsByUserAndFriends(eq(inviterId), anyList()))
                .thenReturn(List.of("uuid-2", "uuid-3"));

        // 카카오 API 호출 성공
        KakaoClient.KakaoSendResult successResult = KakaoClient.KakaoSendResult.success(2, "전송 성공");
        when(kakaoClient.sendToFriends(anyString(), anyList(), any(TemplatePayload.class)))
                .thenReturn(CompletableFuture.completedFuture(successResult));

        // When
        KakaoNotifyResponse result = kakaoNotifyService.send(inviterId, deliverableUsers, templatePayload, totalTargetCount);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSentCount());
        assertEquals(2, result.getTotalCount());
        assertEquals(0, result.getFailedCount());
    }

    @Test
    void checkUserConsent_동의정보가_있을때_true_반환() {
        // Given
        Long userId = 1L;
        when(userConsentRepository.findByUserId(userId))
                .thenReturn(Optional.of(testUserConsent));

        // When
        boolean result = kakaoNotifyService.checkUserConsent(userId);

        // Then
        assertTrue(result);
    }

    @Test
    void checkKakaoInfo_카카오정보가_유효할때_true_반환() {
        // Given
        Long userId = 1L;
        when(userKakaoInfoRepository.findByUserId(userId))
                .thenReturn(Optional.of(testUserKakaoInfo));

        // When
        boolean result = kakaoNotifyService.checkKakaoInfo(userId);

        // Then
        assertTrue(result);
    }

    @Test
    void sendKakaoNotification_발송자_동의없음시_예외발생() {
        // Given
        Long inviterId = 1L;
        Long meetingId = 1L;
        List<Long> receiverIds = List.of(2L);

        // 발송자 동의 정보 없음
        when(userConsentRepository.findByUserId(inviterId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
                kakaoNotifyService.sendKakaoNotification(inviterId, meetingId, receiverIds));
    }

    // 헬퍼 메서드들

    private com.promiseservice.dto.UserDto createTestUserDto(String name) {
        com.promiseservice.dto.UserDto user = new com.promiseservice.dto.UserDto();
        user.setId(1L);
        user.setName(name);
        return user;
    }

    private MeetingParticipant createTestParticipant(Long userId, Long meetingId) {
        MeetingParticipant participant = new MeetingParticipant();
        participant.setUserId(userId);
        participant.setMeetingId(meetingId);
        participant.setResponse(MeetingParticipant.ResponseStatus.ACCEPTED);
        return participant;
    }

    private UserKakaoInfo createTestKakaoInfo(Long userId, String uuid) {
        UserKakaoInfo kakaoInfo = new UserKakaoInfo();
        kakaoInfo.setUserId(userId);
        kakaoInfo.setKakaoUuid(uuid);
        kakaoInfo.setKakaoAccessToken("test-token");
        kakaoInfo.setKakaoScopesJson("{\"scopes\":[\"talk_message\"]}");
        kakaoInfo.setTokenExpiresAt(LocalDateTime.now().plusHours(1));
        return kakaoInfo;
    }
}
