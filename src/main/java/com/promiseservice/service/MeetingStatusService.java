package com.promiseservice.service;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.entity.MeetingHistory;
import com.promiseservice.domain.entity.MeetingParticipant;
import com.promiseservice.domain.repository.MeetingRepository;
import com.promiseservice.domain.repository.MeetingParticipantRepository;
import com.promiseservice.domain.repository.MeetingHistoryRepository;
import com.promiseservice.dto.MeetingStatusUpdateRequest;
import com.promiseservice.dto.MeetingStatusResponse;
import com.promiseservice.dto.StatusHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingStatusService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final MeetingHistoryRepository historyRepository;
    private final NotificationService notificationService;

    /**
     * 약속 상태 변경
     */
    @Transactional
    public MeetingStatusResponse updateMeetingStatus(Long meetingId, MeetingStatusUpdateRequest request, Long userId) {
        log.info("약속 상태 변경 시작 - 약속 ID: {}, 사용자: {}, 새 상태: {}", 
                meetingId, userId, request.getStatus());

        // 약속 존재 및 권한 확인
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        // 방장만 상태 변경 가능
        if (!meeting.getHostId().equals(userId)) {
            throw new RuntimeException("약속 상태 변경 권한이 없습니다");
        }

        // 현재 상태 저장
        String previousStatus = meeting.getStatus().name();

        // 새 상태로 변경
        Meeting.MeetingStatus newStatus = Meeting.MeetingStatus.valueOf(request.getStatus());
        
        // 상태 변경 유효성 검사
        validateStatusTransition(meeting.getStatus(), newStatus, meeting);

        // 상태 변경
        meeting.setStatus(newStatus);

        // 히스토리 기록
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setAction(MeetingHistory.ActionType.STATUS_CHANGED);
        history.setUserId(userId);
        history.setDetails("상태 변경: " + previousStatus + " → " + newStatus.name() + 
                         (request.getReason() != null ? " (사유: " + request.getReason() + ")" : ""));
        historyRepository.save(history);

        // 특정 상태 변경 시 추가 처리
        handleStatusSpecificActions(meeting, newStatus, userId);

        // 상태 변경 알림 전송
        // 이유: 약속 상태가 변경되었을 때 모든 참여자들에게 실시간 알림을 전송하여 정보 공유 보장
        notificationService.sendMeetingStatusChangeNotification(
            meeting, Meeting.MeetingStatus.valueOf(previousStatus), newStatus, request.getReason(), userId);

        log.info("약속 상태 변경 완료 - 약속 ID: {}, 이전 상태: {}, 새 상태: {}", 
                meetingId, previousStatus, newStatus.name());

        return MeetingStatusResponse.from(meeting, previousStatus, request.getReason(), userId);
    }

    /**
     * 상태 변경 유효성 검사
     */
    private void validateStatusTransition(Meeting.MeetingStatus currentStatus, 
                                       Meeting.MeetingStatus newStatus, 
                                       Meeting meeting) {
        
        // 같은 상태로 변경 불가
        if (currentStatus == newStatus) {
            throw new RuntimeException("현재와 동일한 상태로 변경할 수 없습니다");
        }

        // 상태별 제약 조건 검사
        switch (newStatus) {
            case CONFIRMED:
                validateConfirmation(meeting);
                break;
            case COMPLETED:
                validateCompletion(meeting);
                break;
            case CANCELLED:
                validateCancellation(meeting);
                break;
            case WAITING:
                // WAITING으로 되돌리기는 항상 가능
                break;
        }

        // 상태 전환 규칙 검사
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new RuntimeException("유효하지 않은 상태 변경입니다: " + currentStatus + " → " + newStatus);
        }
    }

    /**
     * CONFIRMED 상태로 변경 시 유효성 검사
     */
    private void validateConfirmation(Meeting meeting) {
        // 최소 참여자 수 확인 (방장 포함 2명 이상)
        long acceptedCount = participantRepository.countAcceptedParticipantsByMeetingId(meeting.getId());
        if (acceptedCount < 2) {
            throw new RuntimeException("최소 2명 이상의 참여자가 필요합니다 (현재: " + acceptedCount + "명)");
        }

        // 약속 시간이 미래인지 확인
        if (meeting.getMeetingTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("과거 시간의 약속은 확정할 수 없습니다");
        }
    }

    /**
     * COMPLETED 상태로 변경 시 유효성 검사
     */
    private void validateCompletion(Meeting meeting) {
        // 약속 시간이 지났는지 확인
        if (meeting.getMeetingTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("아직 약속 시간이 되지 않았습니다");
        }

        // CONFIRMED 상태에서만 COMPLETED로 변경 가능
        if (meeting.getStatus() != Meeting.MeetingStatus.CONFIRMED) {
            throw new RuntimeException("확정된 약속만 완료 상태로 변경할 수 있습니다");
        }
    }

    /**
     * CANCELLED 상태로 변경 시 유효성 검사
     */
    private void validateCancellation(Meeting meeting) {
        // 이미 완료된 약속은 취소 불가
        if (meeting.getStatus() == Meeting.MeetingStatus.COMPLETED) {
            throw new RuntimeException("이미 완료된 약속은 취소할 수 없습니다");
        }
    }

    /**
     * 상태 전환 규칙 검사
     */
    private boolean isValidStatusTransition(Meeting.MeetingStatus currentStatus, 
                                         Meeting.MeetingStatus newStatus) {
        
        switch (currentStatus) {
            case WAITING:
                // WAITING → CONFIRMED, CANCELLED 가능
                return newStatus == Meeting.MeetingStatus.CONFIRMED || 
                       newStatus == Meeting.MeetingStatus.CANCELLED;
            
            case CONFIRMED:
                // CONFIRMED → COMPLETED, CANCELLED 가능
                return newStatus == Meeting.MeetingStatus.COMPLETED || 
                       newStatus == Meeting.MeetingStatus.CANCELLED;
            
            case COMPLETED:
                // COMPLETED → 다른 상태로 변경 불가
                return false;
            
            case CANCELLED:
                // CANCELLED → WAITING으로만 되돌리기 가능
                return newStatus == Meeting.MeetingStatus.WAITING;
            
            default:
                return false;
        }
    }

    /**
     * 상태별 추가 처리
     */
    private void handleStatusSpecificActions(Meeting meeting, Meeting.MeetingStatus newStatus, Long userId) {
        switch (newStatus) {
            case CONFIRMED:
                handleConfirmedStatus(meeting, userId);
                break;
            case CANCELLED:
                handleCancelledStatus(meeting, userId);
                break;
            case COMPLETED:
                handleCompletedStatus(meeting, userId);
                break;
        }
    }

    /**
     * CONFIRMED 상태 처리
     */
    private void handleConfirmedStatus(Meeting meeting, Long userId) {
        // 모든 초대된 참여자에게 알림 (실제로는 알림 서비스 호출)
        log.info("약속 확정 - 약속 ID: {}, 제목: {}", meeting.getId(), meeting.getTitle());
        
        // 히스토리 추가 기록
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setAction(MeetingHistory.ActionType.CONFIRMED);
        history.setUserId(userId);
        history.setDetails("약속 확정됨");
        historyRepository.save(history);
    }

    /**
     * CANCELLED 상태 처리
     */
    private void handleCancelledStatus(Meeting meeting, Long userId) {
        // 모든 참여자에게 취소 알림 (실제로는 알림 서비스 호출)
        log.info("약속 취소 - 약속 ID: {}, 제목: {}", meeting.getId(), meeting.getTitle());
        
        // 히스토리 추가 기록
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setAction(MeetingHistory.ActionType.CANCELLED);
        history.setUserId(userId);
        history.setDetails("약속 취소됨");
        historyRepository.save(history);
    }

    /**
     * COMPLETED 상태 처리
     */
    private void handleCompletedStatus(Meeting meeting, Long userId) {
        // 약속 완료 처리 (실제로는 완료 후처리 로직)
        log.info("약속 완료 - 약속 ID: {}, 제목: {}", meeting.getId(), meeting.getTitle());
        
        // 히스토리 추가 기록
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setAction(MeetingHistory.ActionType.COMPLETED);
        history.setUserId(userId);
        history.setDetails("약속 완료됨");
        historyRepository.save(history);
    }

    /**
     * 약속 상태 히스토리 조회
     */
    public List<StatusHistoryResponse> getStatusHistory(Long meetingId) {
        List<MeetingHistory> history = historyRepository.findByMeetingIdOrderByTimestampDesc(meetingId);
        return history.stream()
            .map(StatusHistoryResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 특정 상태의 약속 목록 조회
     */
    public List<Meeting> getMeetingsByStatus(Meeting.MeetingStatus status) {
        return meetingRepository.findByStatusOrderByMeetingTimeAsc(status);
    }

    /**
     * 약속 상태별 통계 조회
     */
    public Object getStatusStatistics() {
        long waitingCount = meetingRepository.countByStatus(Meeting.MeetingStatus.WAITING);
        long confirmedCount = meetingRepository.countByStatus(Meeting.MeetingStatus.CONFIRMED);
        long completedCount = meetingRepository.countByStatus(Meeting.MeetingStatus.COMPLETED);
        long cancelledCount = meetingRepository.countByStatus(Meeting.MeetingStatus.CANCELLED);

        return new Object() {
            public final long waiting = waitingCount;
            public final long confirmed = confirmedCount;
            public final long completed = completedCount;
            public final long cancelled = cancelledCount;
            public final long total = waitingCount + confirmedCount + completedCount + cancelledCount;
        };
    }
}
