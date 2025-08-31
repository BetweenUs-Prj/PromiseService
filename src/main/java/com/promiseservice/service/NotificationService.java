package com.promiseservice.service;

import com.promiseservice.model.entity.Meeting;
import com.promiseservice.model.entity.Meeting.MeetingStatus;
import com.promiseservice.model.entity.MeetingParticipant;
import com.promiseservice.repository.MeetingParticipantRepository;
import com.promiseservice.repository.UserIdentityRepository;
import com.promiseservice.dto.NotificationRequest;
import com.promiseservice.dto.NotificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 약속 관련 알림을 처리하는 서비스
 * 이유: 약속 상태 변경 시 사용자들에게 적절한 알림을 전송하여 약속 정보를 실시간으로 공유하고 
 * 사용자 참여도를 높이기 위해. 푸시 알림 등을 통해 중요한 알림을 놓치지 않도록 함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${notifications.kakao.direct.enabled:false}")
    private boolean kakaoDirect;

    private final MeetingParticipantRepository participantRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final RestTemplate restTemplate;


    // 알림 서비스 기본 URL
    // 이유: 외부 알림 서비스와의 통신을 위한 엔드포인트 설정
    @Value("${notificationservice.base-url:http://localhost:8083}")
    private String notificationServiceBaseUrl;

    // 알림 전송 API 경로
    // 이유: 알림 서비스의 알림 전송 엔드포인트 경로 설정
    @Value("${notificationservice.api.send:/api/notifications/send}")
    private String notificationSendApiPath;



    /**
     * 약속 상태 변경에 따른 알림을 전송하는 메서드
     * 이유: 약속 상태가 변경될 때마다 관련 사용자들에게 적절한 알림을 자동으로 전송하여 
     * 약속 정보의 실시간 공유를 보장하기 위해
     * 
     * @param meeting 변경된 약속 정보
     * @param previousStatus 이전 약속 상태
     * @param reason 상태 변경 사유
     * @param updatedBy 상태를 변경한 사용자 ID
     */
    public void sendMeetingStatusChangeNotification(Meeting meeting, MeetingStatus previousStatus, 
                                                   MeetingStatus newStatus, String reason, Long updatedBy) {
        log.info("약속 상태 변경 알림 전송 시작 - 약속 ID: {}, 상태 변경: {} → {}", 
                meeting.getId(), previousStatus, newStatus);

        try {
            // 알림을 받을 사용자 목록 조회
            // 이유: 약속과 관련된 모든 사용자에게 상태 변경 알림을 전송하기 위해
            List<Long> recipientUserIds = getNotificationRecipients(meeting);

            // 알림 내용 생성
            // 이유: 상태 변경에 따른 적절한 알림 메시지를 생성하여 사용자 이해도 향상
            String title = createNotificationTitle(newStatus);
            String content = createNotificationContent(meeting, previousStatus, newStatus, reason);

            // 알림 요청 객체 생성
            // 이유: 알림 서비스에 전송할 알림 정보를 체계적으로 구성하기 위해
            NotificationRequest notificationRequest = createNotificationRequest(
                recipientUserIds, title, content, newStatus.name(), meeting.getId());

            // 푸시 알림 전송
            // 이유: 실제 알림 서비스를 통해 사용자들에게 푸시 알림을 전송하기 위해
            NotificationResponse response = sendNotification(notificationRequest);



            log.info("약속 상태 변경 알림 전송 완료 - 약속 ID: {}, 성공: {}, 실패: {}", 
                    meeting.getId(), response.getSuccessCount(), response.getFailureCount());

        } catch (Exception e) {
            log.error("약속 상태 변경 알림 전송 실패 - 약속 ID: {}, 에러: {}", meeting.getId(), e.getMessage());
            // 알림 전송 실패 시에도 약속 상태 변경은 계속 진행
        }
    }



    /**
     * 약속 취소 알림을 전송하는 메서드
     * 이유: 약속이 취소되었을 때 모든 참여자들에게 즉시 알림을 전송하여 
     * 불필요한 이동이나 준비를 방지하기 위해
     * 
     * @param meeting 취소된 약속 정보
     * @param reason 취소 사유
     * @param cancelledBy 취소한 사용자 ID
     */
    public void sendMeetingCancelledNotification(Meeting meeting, String reason, Long cancelledBy) {
        log.info("약속 취소 알림 전송 시작 - 약속 ID: {}", meeting.getId());

        try {
            // 모든 참여자 목록 조회
            List<Long> recipientUserIds = getNotificationRecipients(meeting);

            // 알림 내용 생성
            String title = "약속이 취소되었습니다";
            String content = String.format("'%s' 약속이 취소되었습니다. 사유: %s", 
                meeting.getTitle(), reason != null ? reason : "사유 없음");

            // 알림 요청 객체 생성
            NotificationRequest notificationRequest = createNotificationRequest(
                recipientUserIds, title, content, "MEETING_CANCELLED", meeting.getId());

            // 푸시 알림 전송
            NotificationResponse response = sendNotification(notificationRequest);



            log.info("약속 취소 알림 전송 완료 - 약속 ID: {}, 성공: {}, 실패: {}", 
                    meeting.getId(), response.getSuccessCount(), response.getFailureCount());

        } catch (Exception e) {
            log.error("약속 취소 알림 전송 실패 - 약속 ID: {}, 에러: {}", meeting.getId(), e.getMessage());
        }
    }

    /**
     * 알림을 받을 사용자 목록을 조회하는 메서드
     * 이유: 약속과 관련된 모든 사용자(방장 포함)를 조회하여 상태 변경 알림을 전송하기 위해
     * 
     * @param meeting 약속 정보
     * @return 알림을 받을 사용자 ID 목록
     */
    private List<Long> getNotificationRecipients(Meeting meeting) {
        // 방장 ID 추가
        List<Long> recipientUserIds = new java.util.ArrayList<>();
        recipientUserIds.add(meeting.getHostId());

        // 참여자 ID 목록 조회
        List<Long> participantUserIds = participantRepository.findByMeetingId(meeting.getId())
            .stream()
            .map(participant -> participant.getUserId())
            .collect(Collectors.toList());

        // 중복 제거 후 추가
        recipientUserIds.addAll(participantUserIds.stream()
            .filter(userId -> !userId.equals(meeting.getHostId()))
            .collect(Collectors.toList()));

        return recipientUserIds;
    }

    /**
     * 초대된 사용자 ID 목록을 조회하는 메서드
     * 이유: 방장을 제외한 초대된 사용자들에게만 약속 참여 요청 알림을 전송하기 위해
     * 
     * @param meeting 약속 정보
     * @return 초대된 사용자 ID 목록 (방장 제외)
     */
    private List<Long> getInvitedUserIds(Meeting meeting) {
        return participantRepository.findByMeetingId(meeting.getId())
            .stream()
            .map(participant -> participant.getUserId())
            .filter(userId -> !userId.equals(meeting.getHostId()))
            .collect(Collectors.toList());
    }

    /**
     * 알림 제목을 생성하는 메서드
     * 이유: 약속 상태에 따라 적절한 알림 제목을 생성하여 사용자가 알림의 핵심을 빠르게 파악할 수 있도록 하기 위해
     * 
     * @param newStatus 새로운 약속 상태
     * @return 알림 제목
     */
    private String createNotificationTitle(MeetingStatus newStatus) {
        switch (newStatus) {
            case CONFIRMED:
                return "약속이 확정되었습니다! 🎉";
            case COMPLETED:
                return "약속이 완료되었습니다";
            case CANCELLED:
                return "약속이 취소되었습니다";
            default:
                return "약속 상태가 변경되었습니다";
        }
    }

    /**
     * 알림 내용을 생성하는 메서드
     * 이유: 약속 정보와 상태 변경 사유를 포함한 상세한 알림 내용을 생성하여 
     * 사용자가 필요한 모든 정보를 한 번에 파악할 수 있도록 하기 위해
     * 
     * @param meeting 약속 정보
     * @param previousStatus 이전 상태
     * @param newStatus 새로운 상태
     * @param reason 변경 사유
     * @return 알림 내용
     */
    private String createNotificationContent(Meeting meeting, MeetingStatus previousStatus, 
                                           MeetingStatus newStatus, String reason) {
        StringBuilder content = new StringBuilder();
        content.append("'").append(meeting.getTitle()).append("' 약속의 상태가 변경되었습니다.\n");
        content.append("변경: ").append(previousStatus.getDisplayName())
               .append(" → ").append(newStatus.getDisplayName()).append("\n");
        content.append("시간: ").append(meeting.getMeetingTime()
               .format(java.time.format.DateTimeFormatter.ofPattern("MM월 dd일 HH:mm"))).append("\n");
        content.append("장소: ").append(meeting.getLocationName()).append("\n");
        
        if (reason != null && !reason.trim().isEmpty()) {
            content.append("사유: ").append(reason);
        }

        return content.toString();
    }

    /**
     * 알림 요청 객체를 생성하는 메서드
     * 이유: 알림 서비스에 전송할 알림 정보를 체계적으로 구성하여 알림 전송의 일관성과 품질을 보장하기 위해
     * 
     * @param recipientUserIds 수신자 사용자 ID 목록
     * @param title 알림 제목
     * @param content 알림 내용
     * @param type 알림 타입
     * @param meetingId 약속 ID
     * @return 알림 요청 객체
     */
    private NotificationRequest createNotificationRequest(List<Long> recipientUserIds, String title, 
                                                        String content, String type, Long meetingId) {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientUserIds(recipientUserIds);
        request.setTitle(title);
        request.setContent(content);
        request.setType(type);
        request.setMeetingId(meetingId);
        request.setPriority("HIGH"); // 약속 관련 알림은 높은 우선순위
        return request;
    }

    /**
     * 실제 알림을 전송하는 메서드
     * 이유: 외부 알림 서비스를 통해 사용자들에게 실제 알림을 전송하여 
     * 약속 정보의 실시간 공유를 실현하기 위해
     * 
     * @param notificationRequest 알림 요청 정보
     * @return 알림 전송 결과
     */
    public NotificationResponse sendNotification(NotificationRequest notificationRequest) {
        try {
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP 요청 엔티티 생성
            HttpEntity<NotificationRequest> requestEntity = new HttpEntity<>(notificationRequest, headers);

            // 알림 서비스에 POST 요청 전송
            String url = notificationServiceBaseUrl + notificationSendApiPath;
            NotificationResponse response = restTemplate.postForObject(url, requestEntity, NotificationResponse.class);

            if (response == null) {
                // 응답이 null인 경우 기본 응답 생성
                response = new NotificationResponse(
                    notificationRequest.getRecipientUserIds(), 
                    new java.util.ArrayList<>()
                );
            }

            return response;

        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생: {}", e.getMessage());
            
            // 오류 발생 시 모든 수신자를 실패로 처리
            return new NotificationResponse(
                new java.util.ArrayList<>(), 
                notificationRequest.getRecipientUserIds()
            );
        }
    }

    /**
     * 약속 생성 알림 전송 (기존 호환성)
     * 이유: 기존 코드와의 호환성을 위해 유지
     * 
     * @param meeting 생성된 약속 정보
     */
    public void sendMeetingCreatedNotification(Meeting meeting, List<Long> recipientUserIds) {
        log.info("약속 생성 알림 전송 시작 - 약속 ID: {}", meeting.getId());

        try {
            if (recipientUserIds.isEmpty()) {
                log.info("알림을 받을 초대된 사용자가 없음 - 약속 ID: {}", meeting.getId());
                return;
            }

            // 알림 내용 생성
            String title = "새로운 약속 초대";
            String content = String.format("'%s' 약속에 초대되었습니다. %s에 %s에서 만나요!", 
                meeting.getTitle(), 
                meeting.getMeetingTime().format(java.time.format.DateTimeFormatter.ofPattern("MM월 dd일 HH:mm")),
                meeting.getLocationName());

            // 알림 요청 객체 생성
            NotificationRequest notificationRequest = createNotificationRequest(
                recipientUserIds, title, content, "MEETING_INVITATION", meeting.getId());

            // 알림 전송
            NotificationResponse response = sendNotification(notificationRequest);

            log.info("약속 생성 알림 전송 완료 - 약속 ID: {}, 성공: {}, 실패: {}", 
                    meeting.getId(), response.getSuccessCount(), response.getFailureCount());

        } catch (Exception e) {
            log.error("약속 생성 알림 전송 실패 - 약속 ID: {}, 에러: {}", meeting.getId(), e.getMessage());
        }
    }

    /**
     * 사용자 ID로 kakaoId를 조회하는 메서드
     * 이유: 내부 사용자 ID를 kakaoId로 변환하여 카카오톡 발송에 사용하기 위해
     * 
     * @param userId 내부 사용자 ID
     * @return kakaoId (Optional)
     */
    private Optional<String> findKakaoIdByUserId(Long userId) {
        try {
            return userIdentityRepository.findByUserId(userId)
                .stream()
                .filter(identity -> "KAKAO".equals(identity.getProvider().name()))
                .map(identity -> identity.getProviderUserId())
                .findFirst();
        } catch (Exception e) {
            log.error("사용자 ID로 kakaoId 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * kakaoId로 직접 카카오톡 메시지 전송
     * 이유: kakaoId를 직접 사용하여 카카오톡 발송을 단순화하고 정확성 향상
     * 
     * @param kakaoId 카카오 사용자 ID
     * @param messageText 전송할 메시지 내용
     * @param meetingId 약속 ID
     */
    private void sendKakaoMessageByKakaoId(String kakaoId, String messageText, Long meetingId) {
        // 테스트용 카카오 액세스 토큰 (환경변수에서 가져오기)
        String accessToken = System.getenv("KAKAO_TEST_ACCESS_TOKEN");
        if (accessToken == null || accessToken.isEmpty()) {
            log.warn("카카오 테스트 액세스 토큰이 설정되지 않음 - kakaoId: {}", kakaoId);
            return;
        }

        try {
            // 카카오톡 API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 메시지 템플릿 생성
            String templateObject = String.format(
                "{\"object_type\":\"text\",\"text\":\"%s\",\"link\":{\"web_url\":\"http://localhost:8080/meetings/%d\"}}",
                messageText.replace("\"", "\\\""), // JSON 이스케이프
                meetingId
            );

            // Form 데이터 생성
            org.springframework.util.LinkedMultiValueMap<String, String> form = new org.springframework.util.LinkedMultiValueMap<>();
            form.add("template_object", templateObject);

            // 카카오톡 "나와의 채팅" API 호출
            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, String>> request = 
                new HttpEntity<>(form, headers);

            var response = restTemplate.postForEntity(
                "https://kapi.kakao.com/v2/api/talk/memo/default/send",
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("카카오톡 전송 성공 - kakaoId: {}, HTTP 상태: {}", kakaoId, response.getStatusCode());
            } else {
                log.warn("카카오톡 전송 실패 - kakaoId: {}, HTTP 상태: {}", kakaoId, response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("카카오톡 전송 중 오류 발생 - kakaoId: {}, 에러: {}", kakaoId, e.getMessage());
        }
    }

    /**
     * 약속 생성 완료 메시지 (방장용)
     * 이유: 방장에게 약속 생성이 완료되었음을 알리는 메시지를 생성하기 위해
     * 
     * @param meeting 약속 정보
     * @return 방장용 메시지 내용
     */
    private String createMeetingCreatedMessage(Meeting meeting) {
        return String.format(
            "🎉 약속방 생성 완료!\n\n" +
            "✨ 제목: %s\n" +
            "📍 장소: %s\n" +
            "⏰ 시간: %s\n\n" +
            "약속방이 성공적으로 생성되었습니다! 🎯",
            meeting.getTitle(),
            meeting.getLocationName(),
            meeting.getMeetingTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"))
        );
    }

    /**
     * 약속 초대 메시지 (초대된 사용자용)
     * 이유: 초대된 사용자에게 약속 참여 요청 알림을 전송하기 위해
     * 
     * @param meeting 약속 정보
     * @return 초대된 사용자용 메시지 내용
     */
    private String createMeetingInviteMessage(Meeting meeting) {
        return String.format(
            "약속이 잡혔습니다!\n\n" +
            "제목: %s\n" +
            "장소: %s\n" +
            "시간: %s\n\n" +
            "참석 확인 부탁드려요!",
            meeting.getTitle(),
            meeting.getLocationName(),
            meeting.getMeetingTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"))
        );
    }

    /**
     * 알림 결과 로깅
     * 이유: 알림 전송 결과를 추적하고 모니터링하기 위해
     * 
     * @param meetingId 약속 ID
     * @param recipientCount 수신자 수
     * @param status 전송 상태
     * @param message 결과 메시지
     */
    private void logNotificationResult(Long meetingId, int recipientCount, String status, String message) {
        try {
            // TODO: NotificationLogRepository 구현 후 실제 로깅
            log.info("알림 결과 로깅 - 약속: {}, 수신자: {}명, 상태: {}, 메시지: {}", 
                    meetingId, recipientCount, status, message);
            
        } catch (Exception e) {
            log.error("알림 결과 로깅 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 약속 생성 알림 전송 (Meeting만 받는 오버로드)
     * 이유: 약속 생성 시 Meeting 엔티티만으로 알림을 전송하기 위해
     * 
     * @param meeting 생성된 약속
     */
    public void sendMeetingCreatedNotification(Meeting meeting) {
        log.info("약속 생성 알림 전송 시작 - 약속 ID: {}", meeting.getId());

        try {
            // 1) 내부 알림 서비스 호출 (원래 있던 로직)
            List<Long> recipientUserIds = getInvitedUserIds(meeting);
            if (recipientUserIds.isEmpty()) {
                log.info("알림을 받을 초대된 사용자가 없음 - 약속 ID: {}", meeting.getId());
                // 호스트에게만 개발용 확인 메시지 (스모크)
                findKakaoIdByUserId(meeting.getHostId())
                    .ifPresent(kid -> sendKakaoMessageByKakaoId(kid, createMeetingCreatedMessage(meeting), meeting.getId()));
                return;
            }

            // 2) 내부 알림 서비스 호출
            NotificationRequest req = createNotificationRequest(
                recipientUserIds, "새로운 약속 초대", createMeetingInviteMessage(meeting),
                "MEETING_INVITATION", meeting.getId());
            NotificationResponse resp = sendNotification(req);
            log.info("내부 알림 서비스 결과 - 성공:{}, 실패:{}", resp.getSuccessCount(), resp.getFailureCount());

            // 3) 개발 환경: 카카오 직접 발송도 병행 (수신자 매핑)
            if (kakaoDirect) {
                log.info("카카오 직접 발송 모드 활성화 - 수신자 {}명", recipientUserIds.size());
                for (Long uid : recipientUserIds) {
                    findKakaoIdByUserId(uid).ifPresent(kid ->
                        sendKakaoMessageByKakaoId(kid, createMeetingInviteMessage(meeting), meeting.getId())
                    );
                }
            }
            
            // 4) 호스트에게도 약속 생성 완료 메시지
            findKakaoIdByUserId(meeting.getHostId())
                .ifPresent(kid -> sendKakaoMessageByKakaoId(kid, createMeetingCreatedMessage(meeting), meeting.getId()));
                
        } catch (Exception e) {
            log.error("약속 생성 알림 전송 실패 - 약속 ID: {}, 에러: {}", meeting.getId(), e.getMessage());
        }
    }














}
