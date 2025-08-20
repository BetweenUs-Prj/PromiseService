package com.promiseservice.service;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.entity.MeetingParticipant;
import com.promiseservice.domain.entity.MeetingHistory;
import com.promiseservice.domain.repository.MeetingRepository;
import com.promiseservice.domain.repository.MeetingParticipantRepository;
import com.promiseservice.domain.repository.MeetingHistoryRepository;
import com.promiseservice.dto.MeetingCreateRequest;
import com.promiseservice.dto.MeetingResponse;
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
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final MeetingHistoryRepository historyRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * 약속방 생성
     */
    @Transactional
    public MeetingResponse createMeeting(MeetingCreateRequest request, Long hostId) {
        log.info("약속방 생성 시작 - 방장: {}, 제목: {}", hostId, request.getTitle());

        // 약속 생성
        Meeting meeting = new Meeting();
        meeting.setHostId(hostId);
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setMeetingTime(request.getMeetingTime());
        meeting.setMaxParticipants(request.getMaxParticipants());
        meeting.setLocationName(request.getLocationName());
        meeting.setLocationAddress(request.getLocationAddress());
        meeting.setLocationCoordinates(request.getLocationCoordinates());

        Meeting savedMeeting = meetingRepository.save(meeting);

        // 방장을 참여자로 추가 (수락 상태)
        MeetingParticipant hostParticipant = new MeetingParticipant();
        hostParticipant.setMeeting(savedMeeting);
        hostParticipant.setUserId(hostId);
        hostParticipant.setResponse(MeetingParticipant.ResponseStatus.ACCEPTED);
        hostParticipant.setJoinedAt(LocalDateTime.now());
        participantRepository.save(hostParticipant);

        // 초대할 친구들을 참여자로 추가
        if (request.getParticipantUserIds() != null && !request.getParticipantUserIds().isEmpty()) {
            for (Long userId : request.getParticipantUserIds()) {
                // 방장이 아닌 경우에만 초대
                if (!userId.equals(hostId)) {
                    // 사용자 존재 여부 확인
                    if (!userService.existsUser(userId)) {
                        throw new RuntimeException("존재하지 않는 사용자입니다: " + userId);
                    }
                    
                    MeetingParticipant participant = new MeetingParticipant();
                    participant.setMeeting(savedMeeting);
                    participant.setUserId(userId);
                    participant.setResponse(MeetingParticipant.ResponseStatus.INVITED);
                    participantRepository.save(participant);
                }
            }
        }

        // 히스토리 기록
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(savedMeeting);
        history.setAction(MeetingHistory.ActionType.CREATED);
        history.setUserId(hostId);
        history.setDetails("약속방 생성: " + request.getTitle());
        historyRepository.save(history);

        // 약속 생성 알림 전송
        // 이유: 새로운 약속이 생성되었을 때 초대된 사용자들에게 약속 참여 요청 알림을 전송하여 참여율 향상
        notificationService.sendMeetingCreatedNotification(savedMeeting);

        log.info("약속방 생성 완료 - ID: {}", savedMeeting.getId());
        return MeetingResponse.from(savedMeeting);
    }

    /**
     * 약속방 조회
     */
    public MeetingResponse getMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));
        
        return MeetingResponse.from(meeting);
    }

    /**
     * 방장이 생성한 약속 목록 조회
     */
    public List<MeetingResponse> getMeetingsByHost(Long hostId) {
        List<Meeting> meetings = meetingRepository.findByHostIdOrderByCreatedAtDesc(hostId);
        return meetings.stream()
            .map(MeetingResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 사용자가 참여한 약속 목록 조회
     */
    public List<MeetingResponse> getMeetingsByParticipant(Long userId) {
        List<Meeting> meetings = meetingRepository.findMeetingsByParticipantUserId(userId);
        return meetings.stream()
            .map(MeetingResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 약속 상태 변경
     */
    @Transactional
    public MeetingResponse updateMeetingStatus(Long meetingId, Meeting.MeetingStatus status, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        // 권한 확인 (방장만 상태 변경 가능)
        if (!meeting.getHostId().equals(userId)) {
            throw new RuntimeException("약속 상태 변경 권한이 없습니다");
        }

        meeting.setStatus(status);

        // 히스토리 기록
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setAction(MeetingHistory.ActionType.UPDATED);
        history.setUserId(userId);
        history.setDetails("약속 상태 변경: " + status.name());
        historyRepository.save(history);

        return MeetingResponse.from(meeting);
    }

    /**
     * 약속 삭제
     */
    @Transactional
    public void deleteMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        // 권한 확인 (방장만 삭제 가능)
        if (!meeting.getHostId().equals(userId)) {
            throw new RuntimeException("약속 삭제 권한이 없습니다");
        }

        // 히스토리 기록
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setAction(MeetingHistory.ActionType.CANCELLED);
        history.setUserId(userId);
        history.setDetails("약속 삭제");
        historyRepository.save(history);

        meetingRepository.delete(meeting);
        log.info("약속 삭제 완료 - ID: {}", meetingId);
    }
}
