package com.promiseservice.service;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.entity.Meeting.MeetingStatus;
import com.promiseservice.domain.repository.MeetingParticipantRepository;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 약속 관련 알림을 처리하는 서비스
 * 이유: 약속 상태 변경 시 사용자들에게 적절한 알림을 전송하여 약속 정보를 실시간으로 공유하고 
 * 사용자 참여도를 높이기 위해
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MeetingParticipantRepository participantRepository;
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

            // 알림 전송
            // 이유: 실제 알림 서비스를 통해 사용자들에게 알림을 전송하기 위해
            NotificationResponse response = sendNotification(notificationRequest);

            log.info("약속 상태 변경 알림 전송 완료 - 약속 ID: {}, 성공: {}, 실패: {}", 
                    meeting.getId(), response.getSuccessCount(), response.getFailureCount());

        } catch (Exception e) {
            log.error("약속 상태 변경 알림 전송 실패 - 약속 ID: {}, 에러: {}", meeting.getId(), e.getMessage());
            // 알림 전송 실패 시에도 약속 상태 변경은 계속 진행
        }
    }

    /**
     * 약속 생성 알림을 전송하는 메서드
     * 이유: 새로운 약속이 생성되었을 때 초대된 사용자들에게 약속 참여 요청 알림을 전송하여 
     * 약속 참여율을 높이기 위해
     * 
     * @param meeting 생성된 약속 정보
     */
    public void sendMeetingCreatedNotification(Meeting meeting) {
        log.info("약속 생성 알림 전송 시작 - 약속 ID: {}", meeting.getId());

        try {
            // 초대된 사용자 목록 조회 (방장 제외)
            // 이유: 방장이 아닌 초대된 사용자들에게만 약속 참여 요청 알림을 전송하기 위해
            List<Long> recipientUserIds = getInvitedUserIds(meeting);

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

            // 알림 전송
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
}
