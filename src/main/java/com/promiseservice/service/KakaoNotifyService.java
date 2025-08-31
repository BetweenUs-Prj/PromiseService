package com.promiseservice.service;

import com.promiseservice.client.KakaoClient;
import com.promiseservice.domain.entity.*;
import com.promiseservice.domain.repository.*;
import com.promiseservice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 카카오톡 알림 전송 서비스
 * 이유: 약속 확정 시 카카오톡으로 알림을 전송하고, 친구 관계와 동의 상태를 확인하여
 * 적절한 사용자들에게만 메시지를 전송하기 위해
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoNotifyService {

    private final KakaoClient kakaoClient;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserService userService;
    private final NotificationLogService notificationLogService;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${kakao.notification.batch-size:20}")
    private int batchSize;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM월 dd일(E) HH:mm");

    /**
     * 카카오톡 알림 전송 메인 메서드
     * 이유: 약속 확정 시 참여자들에게 카카오톡 알림을 전송하기 위해
     * 
     * @param inviterId 초대자(발송자) ID
     * @param meetingId 약속 ID
     * @param receiverIds 수신자 ID 목록 (null이면 모든 참여자)
     * @return 전송 결과
     */
    @Transactional
    public KakaoNotifyResponse sendKakaoNotification(Long inviterId, Long meetingId, List<Long> receiverIds) {
        log.info("카카오톡 알림 전송 시작 - 발송자: {}, 약속: {}, 수신자: {}명", 
                inviterId, meetingId, receiverIds != null ? receiverIds.size() : "전체");

        try {
            // 1. 기본 검증
            validateBasicRequirements(inviterId, meetingId);

            // 2. 약속 정보 조회
            Meeting meeting = meetingRepository.findById(meetingId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약속입니다: " + meetingId));

            // 3. 템플릿 페이로드 생성
            TemplatePayload templatePayload = buildTemplatePayload(meeting, inviterId);

            // 4. 수신자 목록 결정
            List<Long> targetReceivers = resolveReceivers(inviterId, meetingId, receiverIds);

            // 5. 실제 카카오톡 전송
            return sendRealKakaoNotification(inviterId, targetReceivers, templatePayload);

        } catch (Exception e) {
            log.error("카카오톡 알림 전송 중 오류 발생 - 발송자: {}, 약속: {}", inviterId, meetingId, e);
            KakaoNotifyResponse response = new KakaoNotifyResponse(0, 
                    receiverIds != null ? receiverIds.size() : 0);
            response.addFailure(inviterId, "전송 중 오류 발생: " + e.getMessage());
            return response;
        }
    }

    /**
     * 템플릿 페이로드를 생성하는 메서드
     * 이유: 약속 정보를 바탕으로 카카오톡 메시지 템플릿을 구성하기 위해
     * 
     * @param meeting 약속 정보
     * @param inviterId 초대자 ID
     * @return 템플릿 페이로드
     */
    public TemplatePayload buildTemplatePayload(Meeting meeting, Long inviterId) {
        try {
            // 초대자 정보 조회
            UserDto inviterUser = userService.getUserById(inviterId);
            String inviterName = inviterUser != null ? inviterUser.getName() : "익명";

            // 날짜 포맷팅
            String formattedDate = meeting.getMeetingTime().format(DATE_FORMATTER);

            // 장소 정보 구성
            String place = meeting.getLocationName();
            if (place == null || place.trim().isEmpty()) {
                place = "장소 미정";
            }

            // 약속 상세 URL 생성
            String meetingUrl = appBaseUrl + "/meetings/" + meeting.getId();

            return new TemplatePayload(
                    inviterName,
                    formattedDate,
                    place,
                    meetingUrl,
                    meeting.getTitle(),
                    meeting.getDescription()
            );
        } catch (Exception e) {
            log.error("템플릿 페이로드 생성 중 오류", e);
            throw new RuntimeException("메시지 템플릿 생성에 실패했습니다", e);
        }
    }

    /**
     * 수신자 목록을 결정하는 메서드
     * 이유: 수신자가 지정되지 않으면 약속 참여자를 기본으로 하고, 
     * 발송자는 수신자에서 제외하기 위해
     * 
     * @param inviterId 발송자 ID
     * @param meetingId 약속 ID
     * @param receiverIds 지정된 수신자 ID 목록
     * @return 최종 수신자 목록
     */
    public List<Long> resolveReceivers(Long inviterId, Long meetingId, List<Long> receiverIds) {
        List<Long> targets;

        if (receiverIds == null || receiverIds.isEmpty()) {
            // 약속 참여자 전체를 대상으로 함
            List<MeetingParticipant> participants = participantRepository.findByMeetingId(meetingId);
            targets = participants.stream()
                    .map(MeetingParticipant::getUserId)
                    .collect(Collectors.toList());
        } else {
            targets = new ArrayList<>(receiverIds);
        }

        // 발송자는 수신자에서 제외
        targets.removeIf(id -> id.equals(inviterId));

        log.info("수신자 목록 결정 완료 - 총 {}명 (발송자 제외)", targets.size());
        return targets;
    }

    /**
     * 실제 카카오톡 알림 전송
     * 이유: 실제 카카오 API를 통해 사용자들에게 카카오톡 메시지를 전송하기 위해
     * 
     * @param inviterId 발송자 ID
     * @param targetReceivers 수신자 ID 목록
     * @param templatePayload 메시지 템플릿
     * @return 전송 결과
     */
    private KakaoNotifyResponse sendRealKakaoNotification(Long inviterId, List<Long> targetReceivers, TemplatePayload templatePayload) {
        log.info("실제 카카오톡 알림 전송 시작 - 발송자: {}, 대상: {}명", inviterId, targetReceivers.size());
        
        if (targetReceivers.isEmpty()) {
            return new KakaoNotifyResponse(0, 0);
        }
        
        try {
            // 1. 발송자의 카카오 액세스 토큰 가져오기
            String accessToken = getKakaoAccessToken(inviterId);
            if (accessToken == null) {
                KakaoNotifyResponse response = new KakaoNotifyResponse(0, targetReceivers.size());
                response.addFailure(inviterId, "카카오 액세스 토큰이 없습니다. 카카오 로그인이 필요합니다.");
                return response;
            }
            
            // 2. 수신자들의 카카오 액세스 토큰 수집
            Map<Long, String> participantTokens = getKakaoTokensForUsers(targetReceivers);
            if (participantTokens.isEmpty()) {
                KakaoNotifyResponse response = new KakaoNotifyResponse(0, targetReceivers.size());
                response.addFailure(null, "카카오 로그인된 참여자가 없습니다.");
                return response;
            }
            
            // 3. 실제 카카오 "나와의 채팅" API 호출
            CompletableFuture<KakaoClient.KakaoSendResult> future = 
                kakaoClient.sendToMemo(participantTokens, templatePayload);
            
            KakaoClient.KakaoSendResult result = future.join();
            
            // 4. 결과 처리
            KakaoNotifyResponse response = new KakaoNotifyResponse(result.getSentCount(), targetReceivers.size());
            
            if (!result.isSuccess()) {
                response.addFailure(null, result.getMessage());
            }
            
            log.info("실제 카카오톡 알림 전송 완료 - 성공: {}/{}", result.getSentCount(), targetReceivers.size());
            return response;
            
        } catch (Exception e) {
            log.error("카카오톡 알림 전송 중 오류", e);
            KakaoNotifyResponse response = new KakaoNotifyResponse(0, targetReceivers.size());
            response.addFailure(inviterId, "전송 중 오류 발생: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 사용자의 카카오 액세스 토큰을 가져오는 메서드
     * 이유: 카카오 API 호출을 위해 사용자의 유효한 액세스 토큰이 필요하기 때문에
     * 
     * @param userId 사용자 ID
     * @return 카카오 액세스 토큰 (없으면 null)
     */
    private String getKakaoAccessToken(Long userId) {
        // TODO: 실제 구현에서는 UserKakaoInfo 테이블에서 토큰을 조회해야 함
        // 현재는 테스트용으로 환경변수나 설정에서 가져오기
        
        // 개발/테스트용 고정 토큰 (실제로는 DB에서 조회)
        String testToken = System.getenv("KAKAO_TEST_ACCESS_TOKEN");
        if (testToken != null && !testToken.trim().isEmpty()) {
            log.info("테스트용 카카오 액세스 토큰 사용 - 사용자 ID: {}", userId);
            return testToken;
        }
        
        log.warn("카카오 액세스 토큰을 찾을 수 없음 - 사용자 ID: {}", userId);
        return null;
    }
    
    /**
     * 사용자 ID 목록을 카카오 액세스 토큰 맵으로 변환하는 메서드
     * 이유: "나와의 채팅" 방식은 각 사용자의 개별 액세스 토큰이 필요하기 때문에
     * 
     * @param userIds 사용자 ID 목록
     * @return 사용자 ID -> 액세스 토큰 맵
     */
    private Map<Long, String> getKakaoTokensForUsers(List<Long> userIds) {
        // TODO: 실제 구현에서는 UserKakaoInfo 테이블에서 각 사용자의 토큰을 조회해야 함
        // 현재는 테스트용으로 환경변수의 토큰을 모든 사용자에게 적용
        
        Map<Long, String> tokenMap = new HashMap<>();
        String testToken = System.getenv("KAKAO_TEST_ACCESS_TOKEN");
        
        if (testToken != null && !testToken.trim().isEmpty()) {
            for (Long userId : userIds) {
                tokenMap.put(userId, testToken);
                log.debug("사용자 ID {} -> 테스트 토큰 적용", userId);
            }
        } else {
            log.warn("KAKAO_TEST_ACCESS_TOKEN 환경변수가 설정되지 않았습니다.");
        }
        
        return tokenMap;
    }

    /**
     * 사용자의 카카오 기능 동의 상태를 확인하는 메서드
     * 이유: 컨트롤러에서 사용자의 동의 상태를 빠르게 확인하기 위해
     * 
     * @param userId 사용자 ID
     * @return 동의 여부
     */
    public boolean checkUserConsent(Long userId) {
        return userConsentRepository.findByUserId(userId)
                .map(UserConsent::canUseKakaoFeatures)
                .orElse(false);
    }

    /**
     * 사용자의 카카오 정보 등록 상태를 확인하는 메서드 (Mock)
     * 이유: 컨트롤러에서 사용자의 카카오 정보 등록 여부를 빠르게 확인하기 위해
     * 
     * @param userId 사용자 ID
     * @return 카카오 정보 등록 여부 (항상 true로 Mock 처리)
     */
    public boolean checkKakaoInfo(Long userId) {
        // Mock: 항상 true 반환 (테스트 목적)
        return true;
    }

    /**
     * 기본 요구사항을 검증하는 메서드
     * 이유: 필수 파라미터와 발송자의 기본 조건을 사전에 확인하기 위해
     */
    private void validateBasicRequirements(Long inviterId, Long meetingId) {
        if (inviterId == null) {
            throw new IllegalArgumentException("발송자 ID는 필수입니다");
        }
        if (meetingId == null) {
            throw new IllegalArgumentException("약속 ID는 필수입니다");
        }

        // 발송자의 동의 상태 확인 (간소화)
        boolean hasConsent = userConsentRepository.findByUserId(inviterId)
                .map(UserConsent::canUseKakaoFeatures)
                .orElse(false);

        if (!hasConsent) {
            throw new IllegalStateException("발송자가 카카오 기능 사용에 동의하지 않았습니다");
        }
    }
}
