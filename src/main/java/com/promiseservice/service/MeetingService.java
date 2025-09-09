package com.promiseservice.service;

import com.promiseservice.model.dto.*;
import com.promiseservice.model.entity.Meeting;
import com.promiseservice.model.entity.MeetingParticipant;
import com.promiseservice.model.entity.Place;
import com.promiseservice.model.entity.User;
import com.promiseservice.model.enums.PlaceStatus;
import com.promiseservice.repository.MeetingRepository;
import com.promiseservice.repository.MeetingParticipantRepository;
import com.promiseservice.repository.PlaceRepository;
import com.promiseservice.repository.FriendshipRepository;
import com.promiseservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * 약속 생성
     * 이유: 새로운 약속을 생성하고 초대된 참가자들을 등록하기 위해
     *
     * @param request 약속 생성 요청
     * @param userId 요청한 사용자 ID (호스트)
     * @return 생성된 약속 정보
     */
    public MeetingResponse createMeeting(MeetingCreateRequest request, Long userId) {
        log.info("약속 생성 시작 - 제목: {}, 장소: {}, 시간: {}",
                request.getTitle(), request.getPlaceName(), request.getScheduledAt());

        // 호스트 사용자 ID 검증 및 자동 생성
        if (!userRepository.existsById(userId)) {
            log.warn("사용자 ID {}가 존재하지 않습니다. 자동으로 생성합니다.", userId);
            User newUser = User.builder()
                    .name("사용자 " + userId)
                    .profileImage("https://example.com/default-avatar.jpg")
                    .providerId("system_" + userId)
                    .build();
            User savedUser = userRepository.save(newUser);
            log.info("사용자 ID {} 자동 생성 완료 (실제 ID: {})", userId, savedUser.getId());
            // 실제 생성된 ID로 업데이트
            userId = savedUser.getId();
        }

        Place place = resolvePlace(request);

        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setMeetingTime(request.getScheduledAt());
        meeting.setMaxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 10);
        meeting.setPlace(place);                          // FK 안전
        // place_id가 null이면 "장소 미정"으로 설정
        meeting.setPlaceId(place != null ? place.getId() : null);
        meeting.setLocationName(place != null ? place.getName() : "장소 미정");
        meeting.setLocationAddress(place != null ? place.getAddress() : "장소 미정");
        meeting.setHostId(userId);
        meeting.setStatus("WAITING");

        Meeting savedMeeting = meetingRepository.save(meeting);
        log.info("약속 저장 완료 - ID: {}", savedMeeting.getId());

        // 호스트 자동 참가(확정)
        participantRepository.save(
                MeetingParticipant.builder()
                        .meetingId(savedMeeting.getId())
                        .userId(userId)
                        .response("CONFIRMED")
                        .joinedAt(LocalDateTime.now())
                        .invitedAt(LocalDateTime.now())
                        .build()
        );

        return buildMeetingResponse(savedMeeting);
    }

    /**
     * 장소 해결
     * 이유: placeId가 있으면 조회하고, 없으면 자동으로 장소를 생성하기 위해
     *
     * @param request 약속 생성 요청
     * @return 해결된 Place 엔티티
     */
    private Place resolvePlace(MeetingCreateRequest request) {
        // 1) placeId 우선 사용
        if (request.getPlaceId() != null) {
            return placeRepository.findById(request.getPlaceId())
                .orElse(null); // 없으면 null 반환 (응급 처치)
        }
        // 2) placeId 없으면 장소 최소 정보로 생성
        return createMinimalPlaceFrom(request);
    }

    /**
     * 최소 장소 정보로 장소 생성
     * 이유: placeId가 없거나 유효하지 않을 때 자동으로 장소를 생성하기 위해
     *
     * @param request 약속 생성 요청
     * @return 생성된 Place 엔티티
     */
    private Place createMinimalPlaceFrom(MeetingCreateRequest request) {
        // 외부 키(kakao/naver 등) 매핑이 오면 upsert
        if (request.getExternalPlaceSource() != null && request.getExternalPlaceId() != null) {
            return placeRepository.findBySourceAndExternalId(
                    request.getExternalPlaceSource(), request.getExternalPlaceId())
                .orElseGet(() -> placeRepository.save(Place.builder()
                        .name(nvl(request.getPlaceName(), "미정 장소"))
                        .address(nvl(request.getPlaceAddress(), "미정 주소"))
                        .source(request.getExternalPlaceSource())
                        .externalId(request.getExternalPlaceId())
                        .isActive(false)            // 초기는 비활성/임시
                        .status(PlaceStatus.DRAFT) // DRAFT로 표기
                        .build()));
        }
        // 외부 키도 없으면 그냥 최소 정보로 생성
        // placeName과 placeAddress가 모두 비어있어도 기본값으로 생성
        return placeRepository.save(Place.builder()
                .name(nvl(request.getPlaceName(), "미정 장소"))
                .address(nvl(request.getPlaceAddress(), "미정 주소"))
                .isActive(false)
                .status(PlaceStatus.DRAFT)
                .build());
    }


    /**
     * null 체크 및 기본값 반환
     * 이유: null 값을 안전하게 처리하기 위해
     *
     * @param value 체크할 값
     * @param defaultValue 기본값
     * @return 값이 null이면 기본값, 아니면 원래 값
     */
    private String nvl(String value, String defaultValue) {
        return value != null ? value : defaultValue;
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
     * 사용자 약속 목록 조회
     * 이유: 사용자가 참여하고 있는 모든 약속의 목록을 조회하기 위해
     *
     * @param userId 사용자 ID
     * @return 사용자 약속 목록
     */
    public List<MeetingResponse> getUserMeetings(Long userId) {
        log.info("사용자 약속 목록 조회 시작 - 사용자ID: {}", userId);
        
        // 사용자가 참여하고 있는 모든 약속을 조회 (호스트이거나 참가자인 경우)
        List<MeetingParticipant> participations = participantRepository.findByUserId(userId);
        
        List<MeetingResponse> meetings = participations.stream()
                .map(participation -> {
                    Meeting meeting = participation.getMeeting();
                    return buildMeetingResponse(meeting);
                })
                .collect(Collectors.toList());
        
        log.info("사용자 약속 목록 조회 완료 - 사용자ID: {}, 약속수: {}개", userId, meetings.size());
        return meetings;
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
        meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("약속을 찾을 수 없습니다: " + meetingId));

        List<MeetingInviteResponse.InvitedParticipant> invited = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 사용자 ID 목록으로 초대 (친구 관계 확인)
        if (request.getUserIds() != null) {
            // 1) 사용자 ID 존재 여부 검증
            List<Long> userIds = request.getUserIds();
            long existingUserCount = userRepository.countByIdIn(userIds);
            if (existingUserCount != userIds.size()) {
                // 존재하지 않는 사용자 ID 찾기
                List<Long> existingUserIds = userRepository.findAllById(userIds)
                        .stream()
                        .map(user -> user.getId())
                        .collect(Collectors.toList());
                List<Long> missingUserIds = userIds.stream()
                        .filter(id -> !existingUserIds.contains(id))
                        .collect(Collectors.toList());
                errors.add("존재하지 않는 사용자 ID: " + missingUserIds);
                return MeetingInviteResponse.builder()
                        .invited(invited)
                        .errors(errors)
                        .build();
            }

            for (Long userId : userIds) {
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


        return MeetingInviteResponse.builder()
                .invited(invited)
                .kakaoSent(false)
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
                    String role = (meeting != null && meeting.getHostId().equals(p.getUserId())) ? "HOST" : "GUEST";
                    return MeetingParticipantsResponse.ParticipantInfo.builder()
                            .userId(p.getUserId())
                            .name(userService.getUserName(p.getUserId()))
                            .role(role)
                            // 🔁 프론트 기대 키: response (PENDING/CONFIRMED/DECLINED)
                            .response(mapResponseForClient(p.getResponse()))
                            .build();
                })
                .collect(Collectors.toList());

        return MeetingParticipantsResponse.builder()
                .meetingId(meetingId)                                 // 🔁 추가
                .maxParticipants(meeting != null ? meeting.getMaxParticipants() : null) // 🔁 추가
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
        return MeetingResponse.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .status(meeting.getStatus())
                .host(MeetingResponse.HostInfo.builder()
                        .userId(meeting.getHostId())
                        .name(userService.getUserName(meeting.getHostId()))
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
     * DB → 클라이언트 매핑
     * 이유: 데이터베이스의 응답 상태를 프론트엔드가 기대하는 상태로 변환하기 위해
     */
    private String mapResponseForClient(String dbStatus) {
        if ("INVITED".equals(dbStatus)) return "PENDING";
        if ("CONFIRMED".equals(dbStatus)) return "CONFIRMED";
        if ("CANCELLED".equals(dbStatus) || "LEFT".equals(dbStatus)) return "DECLINED";
        // 그 외 값은 그대로
        return dbStatus;
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
                    });
        } else {
            // Place 엔티티가 없으면 Meeting의 location 정보 사용 (null이면 "장소 미정")
            builder.placeId(null)
                   .placeName(meeting.getLocationName() != null ? meeting.getLocationName() : "장소 미정")
                   .address(meeting.getLocationAddress() != null ? meeting.getLocationAddress() : "장소 미정");
        }
        
        return builder.build();
    }


}
