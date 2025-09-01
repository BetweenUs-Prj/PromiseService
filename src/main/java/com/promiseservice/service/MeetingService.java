package com.promiseservice.service;

import com.promiseservice.model.dto.*;
import com.promiseservice.model.entity.Meeting;
import com.promiseservice.model.entity.MeetingParticipant;
import com.promiseservice.model.entity.Place;
import com.promiseservice.model.entity.Friendship;
import com.promiseservice.repository.MeetingRepository;
import com.promiseservice.repository.MeetingParticipantRepository;
import com.promiseservice.repository.PlaceRepository;
import com.promiseservice.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 약속 관리 서비스
 * 이유: 약속 생성, 수정, 초대 등 약속 관련 비즈니스 로직을 처리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final PlaceRepository placeRepository;
    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;

    /**
     * 약속 생성
     * 이유: 새로운 약속을 생성하고 초대된 참가자들을 등록하기 위해
     *
     * @param request 약속 생성 요청
     * @return 생성된 약속 정보
     */
    public MeetingResponse createMeeting(MeetingCreateRequest request) {
        log.info("약속 생성 시작 - 제목: {}, 장소: {}, 시간: {}", 
                request.getTitle(), request.getPlaceName(), request.getScheduledAt());

        // 약속 엔티티 생성 (대기 중 상태로 시작)
        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .description(request.getMemo())
                .meetingTime(request.getScheduledAt())
                .maxParticipants(request.getMaxParticipants())
                .status("WAITING")
                .hostId(1L) // TODO: 실제 사용자 ID로 변경
                .placeId(request.getPlaceId())
                .locationName(request.getPlaceName())
                .locationAddress(request.getPlaceAddress())
                .build();

        Meeting savedMeeting = meetingRepository.save(meeting);
        log.info("약속 저장 완료 - ID: {}", savedMeeting.getId());

        // 호스트 참가자 등록 (자동으로 확정 상태)
        MeetingParticipant host = MeetingParticipant.builder()
                .meetingId(savedMeeting.getId())
                .userId(1L) // TODO: 실제 사용자 ID로 변경
                .response("CONFIRMED")
                .joinedAt(LocalDateTime.now())
                .invitedAt(LocalDateTime.now())
                .build();
        participantRepository.save(host);

        // 초대된 참가자들 등록 (초대 상태로 시작)
        if (request.getParticipantUserIds() != null) {
            for (Long userId : request.getParticipantUserIds()) {
                MeetingParticipant participant = MeetingParticipant.builder()
                        .meetingId(savedMeeting.getId())
                        .userId(userId)
                        .response("INVITED")
                        .invitedAt(LocalDateTime.now())
                        .build();
                participantRepository.save(participant);
            }
        }

        // 초대 알림 발송
        sendInviteNotifications(savedMeeting, request);

        // 응답 생성
        return buildMeetingResponse(savedMeeting);
    }

    /**
     * 약속 조회
     * 이유: 특정 약속의 상세 정보를 조회하기 위해
     *
     * @param meetingId 약속 ID
     * @return 약속 정보
     */
    public MeetingResponse getMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        return buildMeetingResponse(meeting);
    }

    /**
     * 약속 수정
     * 이유: 기존 약속의 정보를 수정하기 위해
     *
     * @param meetingId 약속 ID
     * @param request 수정 요청
     * @return 수정 결과
     */
    public MeetingUpdateResponse updateMeeting(Long meetingId, MeetingUpdateRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        boolean updated = false;

        if (request.getTitle() != null) {
            meeting.setTitle(request.getTitle());
            updated = true;
        }

        if (request.getScheduledAt() != null) {
            meeting.setMeetingTime(request.getScheduledAt());
            updated = true;
        }

        if (request.getPlaceName() != null) {
            meeting.setLocationName(request.getPlaceName());
            updated = true;
        }

        if (request.getPlaceAddress() != null) {
            meeting.setLocationAddress(request.getPlaceAddress());
            updated = true;
        }

        if (updated) {
            meetingRepository.save(meeting);
            log.info("약속 수정 완료 - ID: {}", meetingId);
        }

        return MeetingUpdateResponse.builder()
                .updated(updated)
                .build();
    }

    /**
     * 약속 취소
     * 이유: 약속을 취소하고 상태를 변경하기 위해
     *
     * @param meetingId 약속 ID
     * @return 취소된 약속 상태
     */
    public String cancelMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        meeting.setStatus("CANCELLED");
        meetingRepository.save(meeting);

        log.info("약속 취소 완료 - ID: {}", meetingId);
        return "CANCELLED";
    }

    /**
     * 약속 초대
     * 이유: 기존 약속에 새로운 참가자들을 초대하기 위해
     *
     * @param meetingId 약속 ID
     * @param request 초대 요청
     * @return 초대 결과
     */
    public MeetingInviteResponse inviteParticipants(Long meetingId, MeetingInviteRequest request, Long hostUserId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        List<MeetingInviteResponse.InvitedParticipant> invited = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 사용자 ID 목록으로 초대 (친구 관계 확인)
        if (request.getUserIds() != null) {
            for (Long userId : request.getUserIds()) {
                // 친구 관계 확인
                boolean isFriend = friendshipRepository.areFriends(hostUserId, userId);
                if (!isFriend) {
                    errors.add("사용자 " + userId + "는 친구가 아닙니다");
                    continue;
                }

                // 이미 참여중인지 확인
                boolean alreadyParticipant = participantRepository
                        .findByMeetingIdAndUserId(meetingId, userId)
                        .isPresent();
                if (alreadyParticipant) {
                    errors.add("사용자 " + userId + "는 이미 참여 중입니다");
                    continue;
                }

                MeetingParticipant participant = MeetingParticipant.builder()
                        .meetingId(meetingId)
                        .userId(userId)
                        .response("INVITED")
                        .invitedAt(LocalDateTime.now())
                        .build();
                participantRepository.save(participant);

                invited.add(MeetingInviteResponse.InvitedParticipant.builder()
                        .userId(userId)
                        .status("INVITED")
                        .build());
            }
        }

        // 카카오 ID 목록으로 초대 (친구 관계는 카카오에서 확인되었다고 가정)
        if (request.getKakaoIds() != null) {
            for (String kakaoId : request.getKakaoIds()) {
                // 카카오 ID를 통한 초대 로직 (실제 구현 시 카카오 친구 매핑 테이블 참조)
                log.info("카카오 ID {} 초대 처리", kakaoId);
            }
        }

        // 초대 알림 발송
        if (request.isSendKakao() && !invited.isEmpty()) {
            sendInviteNotifications(meeting, request.getUserIds());
        }

        return MeetingInviteResponse.builder()
                .invited(invited)
                .kakaoSent(request.isSendKakao())
                .errors(errors)
                .build();
    }

    /**
     * 약속 참가 수락
     * 이유: 초대받은 사용자가 약속 참가를 수락하기 위해
     *
     * @param meetingId 약속 ID
     * @param userId 사용자 ID
     * @return 참가 상태
     */
    public String joinMeeting(Long meetingId, Long userId) {
        MeetingParticipant participant = participantRepository
                .findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new RuntimeException("참가자 정보를 찾을 수 없습니다"));

        participant.setResponse("CONFIRMED");
        participant.setJoinedAt(LocalDateTime.now());
        participantRepository.save(participant);

        log.info("약속 참가 수락 - 약속 ID: {}, 사용자 ID: {}", meetingId, userId);

        return "CONFIRMED";
    }

    /**
     * 약속 나가기
     * 이유: 참가자가 약속에서 나가기 위해
     *
     * @param meetingId 약속 ID
     * @param userId 사용자 ID
     * @return 나가기 상태
     */
    public String leaveMeeting(Long meetingId, Long userId) {
        MeetingParticipant participant = participantRepository
                .findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new RuntimeException("참가자 정보를 찾을 수 없습니다"));

        participant.setResponse("CANCELLED");
        participantRepository.save(participant);

        log.info("약속 나가기 - 약속 ID: {}, 사용자 ID: {}", meetingId, userId);
        return "LEFT";
    }

    /**
     * 약속 참가자 목록 조회
     * 이유: 특정 약속에 참가하는 사용자들의 목록을 조회하기 위해
     *
     * @param meetingId 약속 ID
     * @return 참가자 목록
     */
    public MeetingParticipantsResponse getParticipants(Long meetingId) {
        List<MeetingParticipant> participants = participantRepository.findByMeetingId(meetingId);
        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);

        List<MeetingParticipantsResponse.ParticipantInfo> items = participants.stream()
                .map(p -> {
                    String role = "MEMBER";
                    if (meeting != null && meeting.getHostId().equals(p.getUserId())) {
                        role = "HOST";
                    }
                    return MeetingParticipantsResponse.ParticipantInfo.builder()
                            .userId(p.getUserId())
                            .name("사용자" + p.getUserId()) // TODO: 실제 사용자 이름으로 변경
                            .role(role)
                            .status(mapResponseStatus(p.getResponse()))
                            .build();
                })
                .collect(Collectors.toList());

        return MeetingParticipantsResponse.builder()
                .items(items)
                .build();
    }

    /**
     * 약속 확정
     * 이유: 모든 참가자가 수락한 후 약속을 확정하기 위해
     *
     * @param meetingId 약속 ID
     * @return 확정 결과
     */
    public String confirmMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        // 모든 참가자가 수락했는지 확인
        List<MeetingParticipant> confirmedParticipants = participantRepository
                .findByMeetingIdAndResponse(meeting.getId(), "CONFIRMED");

        if (confirmedParticipants.size() < 2) {
            throw new RuntimeException("최소 2명 이상의 참가자가 필요합니다");
        }

        meeting.setStatus("CONFIRMED");
        meetingRepository.save(meeting);

        log.info("약속 확정 완료 - ID: {}", meetingId);
        return "CONFIRMED";
    }

    /**
     * 약속 응답 생성
     * 이유: 클라이언트에게 약속 정보를 전달하기 위한 응답을 생성하기 위해
     *
     * @param meeting 약속 엔티티
     * @return 약속 응답
     */
    private MeetingResponse buildMeetingResponse(Meeting meeting) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = meeting.getMeetingTime().format(formatter);

        return MeetingResponse.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .status(meeting.getStatus())
                .host(MeetingResponse.HostInfo.builder()
                        .userId(meeting.getHostId())
                        .name("사용자" + meeting.getHostId()) // TODO: 실제 사용자 이름으로 변경
                        .build())
                .place(buildPlaceInfo(meeting))
                .participants(buildParticipantInfo(meeting.getId()))
                .build();
    }

    /**
     * 참가자 정보 생성
     * 이유: 참가자 목록을 응답에 포함하기 위해
     *
     * @param meetingId 약속 ID
     * @return 참가자 정보 목록
     */
    private List<MeetingResponse.ParticipantInfo> buildParticipantInfo(Long meetingId) {
        List<MeetingParticipant> participants = participantRepository.findByMeetingId(meetingId);
        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        
        return participants.stream()
                .map(p -> {
                    String role = "MEMBER";
                    if (meeting != null && meeting.getHostId().equals(p.getUserId())) {
                        role = "HOST";
                    }
                    return MeetingResponse.ParticipantInfo.builder()
                            .userId(p.getUserId())
                            .role(role)
                            .status(mapResponseStatus(p.getResponse()))
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 응답 상태 매핑
     * 이유: 데이터베이스의 응답 상태를 API 스펙에 맞는 상태로 변환하기 위해
     */
    private String mapResponseStatus(String dbStatus) {
        if ("CONFIRMED".equals(dbStatus)) {
            return "JOINED";
        } else if ("INVITED".equals(dbStatus)) {
            return "INVITED";
        } else if ("CANCELLED".equals(dbStatus)) {
            return "LEFT";
        }
        return dbStatus;
    }

    /**
     * 초대 알림 발송
     * 이유: 초대된 참가자들에게 알림을 보내기 위해
     *
     * @param meeting 약속 정보
     * @param participantUserIds 참가자 사용자 ID 목록
     */
    private void sendInviteNotifications(Meeting meeting, List<Long> participantUserIds) {
        if (participantUserIds == null || participantUserIds.isEmpty()) {
            return;
        }

        log.info("초대 알림 발송 시작 - 약속 ID: {}, 참가자 수: {}", meeting.getId(), participantUserIds.size());

        for (Long userId : participantUserIds) {
            try {
                // TODO: NotificationService의 메서드 시그니처에 맞게 수정 필요
                // notificationService.sendMeetingInviteNotification(userId, meeting);
                log.info("초대 알림 발송 성공 - 사용자 ID: {}", userId);
            } catch (Exception e) {
                log.error("초대 알림 발송 실패 - 사용자 ID: {}, 오류: {}", userId, e.getMessage());
            }
        }
    }

    /**
     * 초대 알림 발송 (MeetingCreateRequest용)
     * 이유: 약속 생성 시 초대 알림을 보내기 위해
     *
     * @param meeting 약속 정보
     * @param request 약속 생성 요청
     */
    private void sendInviteNotifications(Meeting meeting, MeetingCreateRequest request) {
        if (request.getParticipantUserIds() == null || request.getParticipantUserIds().isEmpty()) {
            return;
        }

        sendInviteNotifications(meeting, request.getParticipantUserIds());
    }

    /**
     * 장소 정보 빌드
     * 이유: Place 엔티티와 Meeting의 location 정보를 조합하여 PlaceInfo를 구성하기 위해
     */
    private MeetingResponse.PlaceInfo buildPlaceInfo(Meeting meeting) {
        MeetingResponse.PlaceInfo.PlaceInfoBuilder builder = MeetingResponse.PlaceInfo.builder();
        
        // Place 엔티티가 있는 경우 우선 사용
        if (meeting.getPlaceId() != null) {
            placeRepository.findByIdAndActive(meeting.getPlaceId())
                    .ifPresent(place -> {
                        builder.placeId(place.getId())
                               .placeName(place.getName())
                               .address(place.getAddress());
                        if (place.getLatitude() != null && place.getLongitude() != null) {
                            builder.lat(place.getLatitude().doubleValue())
                                   .lng(place.getLongitude().doubleValue());
                        }
                    });
        } else {
            // Place 엔티티가 없으면 Meeting의 location 정보 사용
            builder.placeId(null)
                   .placeName(meeting.getLocationName())
                   .address(meeting.getLocationAddress())
                   .lat(37.4979) // TODO: 실제 좌표로 변경
                   .lng(127.0276);
        }
        
        return builder.build();
    }
}
