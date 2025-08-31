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
import org.springframework.context.ApplicationEventPublisher;
import com.promiseservice.event.MeetingCreatedEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Meeting 비즈니스 로직을 처리하는 서비스
 * 이유: 약속 관련 핵심 비즈니스 로직을 캡슐화하고 트랜잭션을 관리하기 위해
 */
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
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 약속방 생성
     * 이유: 사용자가 새로운 약속을 생성하고 다른 사용자들을 초대할 수 있도록 하기 위해
     * 
     * @param request 약속 생성 요청 정보
     * @param hostId 방장 사용자 ID
     * @return 생성된 약속 정보
     */
    @Transactional
    public MeetingResponse createMeeting(MeetingCreateRequest request, Long hostId) {
        log.info("약속방 생성 시작 - 방장: {}, 제목: {}", hostId, request.getTitle());

        // 방장 사용자 존재 여부 확인
        if (!userService.existsUser(hostId)) {
            throw new RuntimeException("존재하지 않는 사용자입니다: " + hostId);
        }

        // 총 초대 인원 수 검증
        if (!request.isValidParticipantCount(true)) {
            throw new RuntimeException("최대 참여자 수를 초과할 수 없습니다");
        }

        // 약속 생성
        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setMeetingTime(request.getMeetingTime());
        meeting.setMaxParticipants(request.getMaxParticipants());
        meeting.setLocationName(request.getLocationName());
        meeting.setLocationAddress(request.getLocationAddress());
        meeting.setLocationCoordinates(request.getLocationCoordinates());

        Meeting savedMeeting = meetingRepository.save(meeting);

        // 방장을 첫 번째 참여자로 추가 (수락 상태)
        // 이유: 방장은 자동으로 약속에 참여하므로 ACCEPTED 상태로 설정
        MeetingParticipant hostParticipant = new MeetingParticipant();
        hostParticipant.setMeetingId(savedMeeting.getId());
        hostParticipant.setUserId(hostId);
        hostParticipant.setMeeting(savedMeeting);
        hostParticipant.setResponse(MeetingParticipant.ResponseStatus.ACCEPTED);
        hostParticipant.setJoinedAt(LocalDateTime.now());
        participantRepository.save(hostParticipant);

        // 초대할 친구들을 참여자로 추가
        if (request.getParticipantUserIds() != null && !request.getParticipantUserIds().isEmpty()) {
            List<MeetingParticipant> participants = new ArrayList<>();
            
            for (Long userId : request.getParticipantUserIds()) {
                // 방장이 아닌 경우에만 초대
                if (!userId.equals(hostId)) {
                    // 사용자 존재 여부 확인
                    if (!userService.existsUser(userId)) {
                        throw new RuntimeException("존재하지 않는 사용자입니다: " + userId);
                    }
                    
                    MeetingParticipant participant = new MeetingParticipant();
                    participant.setMeetingId(savedMeeting.getId());
                    participant.setUserId(userId);
                    participant.setMeeting(savedMeeting);
                    participant.setResponse(MeetingParticipant.ResponseStatus.INVITED);
                    participants.add(participant);
                }
            }
            
            // 배치로 한 번에 저장
            if (!participants.isEmpty()) {
                participantRepository.saveAll(participants);
                log.info("참가자 {}명 저장 완료", participants.size());
                
                // Meeting 엔티티에 participants 추가
                savedMeeting.getParticipants().addAll(participants);
                
                // 강제로 영속성 컨텍스트 플러시
                participantRepository.flush();
            }
        }

        // 히스토리 기록
        MeetingHistory history = MeetingHistory.createHistory(savedMeeting, hostId);
        historyRepository.save(history);

        // 약속 생성 알림 전송
        // 이유: 새로운 약속이 생성되었을 때 초대된 사용자들에게 약속 참여 요청 알림을 전송하여 참여율 향상
        if (request.getSendNotification() != null && request.getSendNotification()) {
            try {
                // 방장을 포함한 모든 참가자에게 알림 발송
                List<Long> allParticipantIds = new ArrayList<>();
                allParticipantIds.add(hostId); // 방장 추가
                
                if (request.getParticipantUserIds() != null && !request.getParticipantUserIds().isEmpty()) {
                    // 초대된 사용자들 추가 (중복 제거)
                    for (Long participantId : request.getParticipantUserIds()) {
                        if   (!allParticipantIds.contains(participantId)) {
                            allParticipantIds.add(participantId);
                        }
                    }
                }
                
                if (!allParticipantIds.isEmpty()) {
                    log.info("카카오톡 약속 생성 알림 발송 시작 - 수신자: {}명 (방장 포함)", allParticipantIds.size());
                    notificationService.sendMeetingCreatedNotification(savedMeeting, allParticipantIds);
                } else {
                    log.info("알림 발송 대상이 없습니다");
                }
            } catch (Exception e) {
                log.error("카카오톡 알림 발송 실패: {}", e.getMessage());
                // 알림 실패는 약속 생성 실패로 처리하지 않음
            }
        }

        log.info("약속방 생성 완료 - ID: {}", savedMeeting.getId());
        
        // 저장된 약속을 다시 조회하여 최신 상태로 응답 생성
        Meeting finalMeeting = meetingRepository.findById(savedMeeting.getId())
            .orElseThrow(() -> new RuntimeException("생성된 약속을 찾을 수 없습니다: " + savedMeeting.getId()));
        
        // 트랜잭션 커밋 후 알림 발송 (이벤트로 분리)
        eventPublisher.publishEvent(new MeetingCreatedEvent(finalMeeting.getId()));
        log.info("약속 생성 완료 이벤트 발행 - 약속 ID: {}", finalMeeting.getId());
        
        return MeetingResponse.from(finalMeeting);
    }

    /**
     * 약속방 조회
     * 이유: 사용자가 특정 약속의 상세 정보를 확인할 수 있도록 하기 위해
     * 
     * @param meetingId 약속 ID
     * @return 약속 정보
     */
    public MeetingResponse getMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));
        
        return MeetingResponse.from(meeting);
    }

    /**
     * 방장이 생성한 약속 목록 조회
     * 이유: 사용자가 자신이 만든 약속들을 관리할 수 있도록 하기 위해
     * 
     * @param hostId 방장 사용자 ID
     * @return 약속 목록
     */
    public List<MeetingResponse> getMeetingsByHost(Long hostId) {
        List<Meeting> meetings = meetingRepository.findMeetingsByHostId(hostId);
        return meetings.stream()
            .map(MeetingResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 사용자가 참여한 약속 목록 조회
     * 이유: 사용자가 참여한 모든 약속을 확인하여 일정 관리를 할 수 있도록 하기 위해
     * 
     * @param userId 사용자 ID
     * @return 약속 목록
     */
    public List<MeetingResponse> getMeetingsByParticipant(Long userId) {
        List<Meeting> meetings = meetingRepository.findMeetingsByParticipantUserId(userId);
        return meetings.stream()
            .map(MeetingResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 약속 상태 변경
     * 이유: 방장이 약속의 진행 상태를 관리할 수 있도록 하기 위해
     * 
     * @param meetingId 약속 ID
     * @param status 새로운 상태
     * @param userId 요청 사용자 ID
     * @return 업데이트된 약속 정보
     */
    @Transactional
    public MeetingResponse updateMeetingStatus(Long meetingId, Meeting.MeetingStatus status, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        // 권한 확인 (방장만 상태 변경 가능)
        if (!meeting.isHost(userId)) {
            throw new RuntimeException("약속 상태 변경 권한이 없습니다");
        }

        Meeting.MeetingStatus previousStatus = meeting.getStatus();
        meeting.setStatus(status);

        // 히스토리 기록
        MeetingHistory history = MeetingHistory.updateHistory(meeting, userId);
        historyRepository.save(history);

        // 상태 변경 알림 (NotificationService에 구현 필요)
        // notificationService.sendMeetingStatusChangedNotification(meeting, previousStatus, status);

        log.info("약속 상태 변경 완료 - ID: {}, {} -> {}", meetingId, previousStatus, status);
        return MeetingResponse.from(meeting);
    }

    /**
     * 약속 삭제
     * 이유: 방장이 더 이상 필요하지 않은 약속을 삭제할 수 있도록 하기 위해
     * 
     * @param meetingId 약속 ID
     * @param userId 요청 사용자 ID
     */
    @Transactional
    public void deleteMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        // 권한 확인 (방장만 삭제 가능)
        if (!meeting.isHost(userId)) {
            throw new RuntimeException("약속 삭제 권한이 없습니다");
        }

        // 히스토리 기록
        MeetingHistory history = MeetingHistory.cancelHistory(meeting, userId);
        historyRepository.save(history);

        // 삭제 알림 전송 (NotificationService에 구현 필요)
        // notificationService.sendMeetingCancelledNotification(meeting);

        meetingRepository.delete(meeting);
        log.info("약속 삭제 완료 - ID: {}", meetingId);
    }

    /**
     * 약속 정보 수정
     * 이유: 방장이 약속의 세부 정보를 변경할 수 있도록 하기 위해
     * 
     * @param meetingId 약속 ID
     * @param request 수정 요청 정보
     * @param userId 요청 사용자 ID
     * @return 수정된 약속 정보
     */
    @Transactional
    public MeetingResponse updateMeeting(Long meetingId, MeetingCreateRequest request, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        // 권한 확인 (방장만 수정 가능)
        if (!meeting.isHost(userId)) {
            throw new RuntimeException("약속 수정 권한이 없습니다");
        }

        // 현재 참여자 수 확인
        if (request.getMaxParticipants() < meeting.getCurrentParticipantCount()) {
            throw new RuntimeException("현재 참여자 수보다 적은 최대 인원으로 변경할 수 없습니다");
        }

        // 약속 정보 업데이트
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setMeetingTime(request.getMeetingTime());
        meeting.setMaxParticipants(request.getMaxParticipants());
        meeting.setLocationName(request.getLocationName());
        meeting.setLocationAddress(request.getLocationAddress());
        meeting.setLocationCoordinates(request.getLocationCoordinates());

        // 히스토리 기록
        MeetingHistory history = MeetingHistory.updateHistory(meeting, userId);
        historyRepository.save(history);

        // 수정 알림 전송 (NotificationService에 구현 필요)
        // notificationService.sendMeetingUpdatedNotification(meeting);

        log.info("약속 정보 수정 완료 - ID: {}", meetingId);
        return MeetingResponse.from(meeting);
    }

    /**
     * 약속 완료 처리
     * 이유: 약속이 끝난 후 완료 상태로 변경하고 참여자들의 실제 참여 여부를 확인하기 위해
     * 
     * @param meetingId 약속 ID
     * @param userId 요청 사용자 ID
     * @return 완료 처리된 약속 정보
     */
    @Transactional
    public MeetingResponse completeMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        // 권한 확인 (방장만 완료 처리 가능)
        if (!meeting.isHost(userId)) {
            throw new RuntimeException("약속 완료 처리 권한이 없습니다");
        }

        // 상태를 완료로 변경
        meeting.setStatus(Meeting.MeetingStatus.COMPLETED);

        // 히스토리 기록
        MeetingHistory history = MeetingHistory.completeHistory(meeting, userId);
        historyRepository.save(history);

        // 완료 알림 전송 (NotificationService에 구현 필요)
        // notificationService.sendMeetingCompletedNotification(meeting);

        log.info("약속 완료 처리 완료 - ID: {}", meetingId);
        return MeetingResponse.from(meeting);
    }
}