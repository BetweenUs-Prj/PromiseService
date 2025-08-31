package com.promiseservice.service;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.entity.MeetingParticipant;
import com.promiseservice.domain.entity.MeetingHistory;

import com.promiseservice.domain.repository.MeetingRepository;
import com.promiseservice.domain.repository.MeetingParticipantRepository;
import com.promiseservice.domain.repository.MeetingHistoryRepository;
import com.promiseservice.dto.InviteParticipantsRequest;
import com.promiseservice.dto.InviteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Meeting 참여자 관리를 위한 서비스
 * 이유: 약속 참여자들의 초대, 응답, 관리 등의 비즈니스 로직을 캡슐화하기 위해
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final MeetingHistoryRepository historyRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * 추가 참여자 초대
     * 이유: 약속 생성 후 추가로 사용자를 초대할 수 있도록 하기 위해
     * 
     * @param meetingId 약속 ID
     * @param request 초대 요청 정보
     * @param hostId 방장 사용자 ID
     * @return 초대 결과
     */
    @Transactional
    public InviteResponse inviteParticipants(Long meetingId, InviteParticipantsRequest request, Long hostId) {
        log.info("참여자 초대 시작 - 약속 ID: {}, 방장: {}, 초대할 사용자: {}", 
                meetingId, hostId, request.getParticipantUserIds());

        // 약속 존재 및 권한 확인
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        if (!meeting.isHost(hostId)) {
            throw new RuntimeException("참여자 초대 권한이 없습니다");
        }

        // 최대 참여자 수 확인
        long currentParticipantCount = participantRepository.countByMeetingId(meetingId);
        if (currentParticipantCount + request.getParticipantUserIds().size() > meeting.getMaxParticipants()) {
            throw new RuntimeException("최대 참여자 수를 초과할 수 없습니다");
        }

        List<Long> successfullyInvited = new ArrayList<>();
        List<Long> alreadyInvited = new ArrayList<>();
        List<Long> failedToInvite = new ArrayList<>();

        for (Long userId : request.getParticipantUserIds()) {
            try {
                // 이미 초대된 사용자인지 확인
                Optional<MeetingParticipant> existingParticipant = 
                    participantRepository.findByMeetingIdAndUserId(meetingId, userId);
                
                if (existingParticipant.isPresent()) {
                    alreadyInvited.add(userId);
                    continue;
                }

                // 사용자 존재 여부 확인 (UserService 연동)
                if (!userService.existsUser(userId)) {
                    failedToInvite.add(userId);
                    continue;
                }

                // 새로운 참여자 추가
                MeetingParticipant participant = new MeetingParticipant();
                participant.setMeetingId(meetingId);
                participant.setUserId(userId);
                participant.setMeeting(meeting);
                participant.setResponse(MeetingParticipant.ResponseStatus.INVITED);
                participantRepository.save(participant);

                successfullyInvited.add(userId);

                // 히스토리 기록
                MeetingHistory history = MeetingHistory.updateHistory(meeting, hostId);
                historyRepository.save(history);

                // 초대 알림 전송 (NotificationService에 구현 필요)
                // notificationService.sendParticipantInvitedNotification(meeting, userId);

            } catch (Exception e) {
                log.error("사용자 초대 실패 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
                failedToInvite.add(userId);
            }
        }

        log.info("참여자 초대 완료 - 성공: {}, 이미 초대됨: {}, 실패: {}", 
                successfullyInvited.size(), alreadyInvited.size(), failedToInvite.size());

        return InviteResponse.builder()
                .successfullyInvited(successfullyInvited)
                .alreadyInvited(alreadyInvited)
                .failedToInvite(failedToInvite)
                .build();
    }

    /**
     * 초대 응답 처리
     * 이유: 초대받은 사용자가 참여 의사를 표명할 수 있도록 하기 위해
     * 
     * @param meetingId 약속 ID
     * @param userId 응답하는 사용자 ID
     * @param response 응답 상태
     */
    @Transactional
    public void respondToInvitation(Long meetingId, Long userId, MeetingParticipant.ResponseStatus response) {
        log.info("초대 응답 처리 - 약속 ID: {}, 사용자: {}, 응답: {}", meetingId, userId, response);

        MeetingParticipant participant = participantRepository.findByMeetingIdAndUserId(meetingId, userId)
            .orElseThrow(() -> new RuntimeException("참여자 정보를 찾을 수 없습니다"));

        // 기존 응답과 동일한 경우 처리하지 않음
        if (participant.getResponse() == response) {
            log.info("이미 동일한 응답 상태입니다 - 약속 ID: {}, 사용자: {}, 응답: {}", meetingId, userId, response);
            return;
        }

        // 응답 상태 업데이트 (Entity 메서드 사용)
        participant.updateResponse(response);
        participantRepository.save(participant);

        // 히스토리 기록
        MeetingHistory.ActionType actionType = response == MeetingParticipant.ResponseStatus.ACCEPTED 
            ? MeetingHistory.ActionType.JOINED 
            : MeetingHistory.ActionType.DECLINED;
        
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(participant.getMeeting());
        history.setUserId(userId);
        history.setAction(actionType);
        historyRepository.save(history);

        // 응답 알림 전송 (NotificationService에 구현 필요)
        if (response == MeetingParticipant.ResponseStatus.ACCEPTED) {
            // notificationService.sendParticipantAcceptedNotification(participant.getMeeting(), userId);
        } else if (response == MeetingParticipant.ResponseStatus.REJECTED) {
            // notificationService.sendParticipantDeclinedNotification(participant.getMeeting(), userId);
        }

        log.info("초대 응답 처리 완료 - 약속 ID: {}, 사용자: {}, 응답: {}", meetingId, userId, response);
    }

    /**
     * 참여자 제거 (방장만 가능)
     * 이유: 방장이 부적절한 참여자를 제거할 수 있도록 하기 위해
     * 
     * @param meetingId 약속 ID
     * @param participantUserId 제거할 참여자 사용자 ID
     * @param hostId 방장 사용자 ID
     */
    @Transactional
    public void removeParticipant(Long meetingId, Long participantUserId, Long hostId) {
        log.info("참여자 제거 - 약속 ID: {}, 제거할 사용자: {}, 방장: {}", 
                meetingId, participantUserId, hostId);

        // 약속 존재 및 권한 확인
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        if (!meeting.isHost(hostId)) {
            throw new RuntimeException("참여자 제거 권한이 없습니다");
        }

        // 방장은 제거할 수 없음
        if (meeting.isHost(participantUserId)) {
            throw new RuntimeException("방장은 제거할 수 없습니다");
        }

        MeetingParticipant participant = participantRepository.findByMeetingIdAndUserId(meetingId, participantUserId)
            .orElseThrow(() -> new RuntimeException("참여자 정보를 찾을 수 없습니다"));

        // 히스토리 기록
        MeetingHistory history = MeetingHistory.updateHistory(meeting, hostId);
        historyRepository.save(history);

        // 제거 알림 전송 (NotificationService에 구현 필요)
        // notificationService.sendParticipantRemovedNotification(meeting, participantUserId);

        participantRepository.delete(participant);
        log.info("참여자 제거 완료 - 약속 ID: {}, 사용자: {}", meetingId, participantUserId);
    }

    /**
     * 참여자 목록 조회
     * 이유: 약속의 모든 참여자 정보를 조회하기 위해
     * 
     * @param meetingId 약속 ID
     * @return 참여자 목록
     */
    public List<MeetingParticipant> getParticipants(Long meetingId) {
        return participantRepository.findByMeetingIdOrderByInvitedAtAsc(meetingId);
    }

    /**
     * 특정 응답 상태의 참여자 목록 조회
     * 이유: 초대됨, 수락됨, 거부됨 등 상태별 참여자를 관리하기 위해
     * 
     * @param meetingId 약속 ID
     * @param response 응답 상태
     * @return 해당 상태의 참여자 목록
     */
    public List<MeetingParticipant> getParticipantsByResponse(Long meetingId, MeetingParticipant.ResponseStatus response) {
        return participantRepository.findByMeetingIdAndResponse(meetingId, response);
    }

    /**
     * 약속 참여자 수 조회
     * 이유: 총 초대된 참여자 수를 확인하기 위해
     * 
     * @param meetingId 약속 ID
     * @return 총 참여자 수
     */
    public long getParticipantCount(Long meetingId) {
        return participantRepository.countByMeetingId(meetingId);
    }

    /**
     * 수락한 참여자 수 조회
     * 이유: 실제 참여할 참여자 수를 확인하기 위해
     * 
     * @param meetingId 약속 ID
     * @return 수락한 참여자 수
     */
    public long getAcceptedParticipantCount(Long meetingId) {
        return participantRepository.countAcceptedParticipantsByMeetingId(meetingId);
    }

    /**
     * 초대된 참여자 수 조회
     * 이유: 아직 응답하지 않은 참여자 수를 확인하기 위해
     * 
     * @param meetingId 약속 ID
     * @return 초대된 참여자 수
     */
    public long getInvitedParticipantCount(Long meetingId) {
        return participantRepository.countInvitedParticipantsByMeetingId(meetingId);
    }

    /**
     * 특정 사용자의 약속 참여 정보 조회
     * 이유: 사용자의 특정 약속에 대한 참여 상태를 확인하기 위해
     * 
     * @param meetingId 약속 ID
     * @param userId 사용자 ID
     * @return 참여 정보 (없으면 Optional.empty())
     */
    public Optional<MeetingParticipant> getParticipantInfo(Long meetingId, Long userId) {
        return participantRepository.findByMeetingIdAndUserId(meetingId, userId);
    }

    /**
     * 방장 정보 조회
     * 이유: 약속의 방장 정보를 쉽게 조회하기 위해
     * 
     * @param meetingId 약속 ID
     * @return 방장 참여자 정보
     */
    public Optional<MeetingParticipant> getHostInfo(Long meetingId) {
        return participantRepository.findHostByMeetingId(meetingId);
    }

    /**
     * 사용자가 방장인지 확인
     * 이유: 권한 검증을 위해 빠른 확인을 제공
     * 
     * @param meetingId 약속 ID
     * @param userId 사용자 ID
     * @return 방장 여부
     */
    public boolean isHost(Long meetingId, Long userId) {
        return participantRepository.isHost(meetingId, userId);
    }

    /**
     * 실제 참여한 참여자 목록 조회
     * 이루: 약속 완료 후 실제 참석자 통계를 위해
     * 
     * @param meetingId 약속 ID
     * @return 실제 참여한 참여자 목록
     */
    public List<MeetingParticipant> getActualParticipants(Long meetingId) {
        return participantRepository.findActualParticipantsByMeetingId(meetingId);
    }

    /**
     * 참여자 통계 정보 조회
     * 이유: 약속의 참여 현황을 한 번에 확인하기 위해
     * 
     * @param meetingId 약속 ID
     * @return 참여자 통계
     */
    public ParticipantStats getParticipantStats(Long meetingId) {
        long totalCount = getParticipantCount(meetingId);
        long acceptedCount = getAcceptedParticipantCount(meetingId);
        long invitedCount = getInvitedParticipantCount(meetingId);
        long rejectedCount = participantRepository.countRejectedParticipantsByMeetingId(meetingId);

        return ParticipantStats.builder()
                .totalCount(totalCount)
                .acceptedCount(acceptedCount)
                .invitedCount(invitedCount)
                .rejectedCount(rejectedCount)
                .build();
    }

    /**
     * 참여자 통계를 위한 내부 클래스
     */
    @lombok.Builder
    @lombok.Getter
    public static class ParticipantStats {
        private final long totalCount;
        private final long acceptedCount;
        private final long invitedCount;
        private final long rejectedCount;
    }
}