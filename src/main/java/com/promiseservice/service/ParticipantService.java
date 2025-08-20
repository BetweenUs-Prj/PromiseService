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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final MeetingHistoryRepository historyRepository;
    private final UserService userService;

    /**
     * 추가 참여자 초대
     */
    @Transactional
    public InviteResponse inviteParticipants(Long meetingId, InviteParticipantsRequest request, Long hostId) {
        log.info("참여자 초대 시작 - 약속 ID: {}, 방장: {}, 초대할 사용자: {}", 
                meetingId, hostId, request.getParticipantUserIds());

        // 약속 존재 및 권한 확인
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        if (!meeting.getHostId().equals(hostId)) {
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
                participant.setMeeting(meeting);
                participant.setUserId(userId);
                participant.setResponse(MeetingParticipant.ResponseStatus.INVITED);
                participantRepository.save(participant);

                successfullyInvited.add(userId);

                // 히스토리 기록
                MeetingHistory history = new MeetingHistory();
                history.setMeeting(meeting);
                history.setAction(MeetingHistory.ActionType.UPDATED);
                history.setUserId(hostId);
                history.setDetails("참여자 초대: " + userId);
                historyRepository.save(history);

            } catch (Exception e) {
                log.error("사용자 초대 실패 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
                failedToInvite.add(userId);
            }
        }

        log.info("참여자 초대 완료 - 성공: {}, 이미 초대됨: {}, 실패: {}", 
                successfullyInvited.size(), alreadyInvited.size(), failedToInvite.size());

        return new InviteResponse(meetingId, successfullyInvited, alreadyInvited, failedToInvite);
    }

    /**
     * 초대 응답 (수락/거절)
     */
    @Transactional
    public void respondToInvite(Long meetingId, Long userId, MeetingParticipant.ResponseStatus response) {
        log.info("초대 응답 - 약속 ID: {}, 사용자: {}, 응답: {}", meetingId, userId, response);

        MeetingParticipant participant = participantRepository.findByMeetingIdAndUserId(meetingId, userId)
            .orElseThrow(() -> new RuntimeException("초대 정보를 찾을 수 없습니다"));

        if (participant.getResponse() != MeetingParticipant.ResponseStatus.INVITED) {
            throw new RuntimeException("이미 응답한 초대입니다");
        }

        participant.setResponse(response);
        participant.setRespondedAt(LocalDateTime.now());

        if (response == MeetingParticipant.ResponseStatus.ACCEPTED) {
            participant.setJoinedAt(LocalDateTime.now());
        }

        // 히스토리 기록
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(participant.getMeeting());
        history.setAction(response == MeetingParticipant.ResponseStatus.ACCEPTED ? 
                        MeetingHistory.ActionType.JOINED : MeetingHistory.ActionType.DECLINED);
        history.setUserId(userId);
        history.setDetails("초대 응답: " + response.name());
        historyRepository.save(history);

        log.info("초대 응답 완료 - 약속 ID: {}, 사용자: {}, 응답: {}", meetingId, userId, response);
    }

    /**
     * 참여자 제거 (방장만 가능)
     */
    @Transactional
    public void removeParticipant(Long meetingId, Long participantUserId, Long hostId) {
        log.info("참여자 제거 - 약속 ID: {}, 제거할 사용자: {}, 방장: {}", 
                meetingId, participantUserId, hostId);

        // 약속 존재 및 권한 확인
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        if (!meeting.getHostId().equals(hostId)) {
            throw new RuntimeException("참여자 제거 권한이 없습니다");
        }

        // 방장은 제거할 수 없음
        if (meeting.getHostId().equals(participantUserId)) {
            throw new RuntimeException("방장은 제거할 수 없습니다");
        }

        MeetingParticipant participant = participantRepository.findByMeetingIdAndUserId(meetingId, participantUserId)
            .orElseThrow(() -> new RuntimeException("참여자 정보를 찾을 수 없습니다"));

        // 히스토리 기록
        MeetingHistory history = new MeetingHistory();
        history.setMeeting(meeting);
        history.setAction(MeetingHistory.ActionType.UPDATED);
        history.setUserId(hostId);
        history.setDetails("참여자 제거: " + participantUserId);
        historyRepository.save(history);

        participantRepository.delete(participant);
        log.info("참여자 제거 완료 - 약속 ID: {}, 사용자: {}", meetingId, participantUserId);
    }

    /**
     * 참여자 목록 조회
     */
    public List<MeetingParticipant> getParticipants(Long meetingId) {
        return participantRepository.findByMeetingId(meetingId);
    }

    /**
     * 특정 응답 상태의 참여자 목록 조회
     */
    public List<MeetingParticipant> getParticipantsByResponse(Long meetingId, MeetingParticipant.ResponseStatus response) {
        return participantRepository.findByMeetingIdAndResponse(meetingId, response);
    }

    /**
     * 약속 참여자 수 조회
     */
    public long getParticipantCount(Long meetingId) {
        return participantRepository.countByMeetingId(meetingId);
    }

    /**
     * 수락한 참여자 수 조회
     */
    public long getAcceptedParticipantCount(Long meetingId) {
        return participantRepository.countAcceptedParticipantsByMeetingId(meetingId);
    }
}

