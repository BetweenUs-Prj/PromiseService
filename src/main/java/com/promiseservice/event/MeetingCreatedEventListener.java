package com.promiseservice.event;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.repository.MeetingRepository;
import com.promiseservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 약속 생성 완료 이벤트 리스너
 * 이유: 트랜잭션 커밋 이후에 알림을 발송하여 참가자 정보가 정상적으로 조회되도록 하기 위해
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingCreatedEventListener {

    private final MeetingRepository meetingRepository;
    private final NotificationService notificationService;

    /**
     * 약속 생성 완료 이벤트 처리
     * 이유: 트랜잭션 커밋 이후에 알림을 발송하기 위해
     * 
     * @param event 약속 생성 완료 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMeetingCreated(MeetingCreatedEvent event) {
        log.info("약속 생성 완료 이벤트 처리 시작 - 약속 ID: {}", event.meetingId());
        
        try {
            Meeting meeting = meetingRepository.findById(event.meetingId())
                .orElseThrow(() -> new RuntimeException("생성된 약속을 찾을 수 없습니다: " + event.meetingId()));
            
            log.info("약속 조회 완료 - 제목: {}, 참가자 수: {}", meeting.getTitle(), meeting.getParticipants().size());
            
            // 알림 발송
            notificationService.sendMeetingCreatedNotification(meeting);
            
        } catch (Exception e) {
            log.error("약속 생성 완료 이벤트 처리 실패 - 약속 ID: {}, 에러: {}", event.meetingId(), e.getMessage());
        }
    }
}
