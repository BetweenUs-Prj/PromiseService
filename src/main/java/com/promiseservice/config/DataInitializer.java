package com.promiseservice.config;

import com.promiseservice.domain.entity.Meeting;
import com.promiseservice.domain.entity.MeetingParticipant;
import com.promiseservice.domain.repository.MeetingRepository;
import com.promiseservice.domain.repository.MeetingParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 애플리케이션 시작 시 테스트용 더미 데이터를 생성하는 설정 클래스
 * 이유: Talend API 테스트를 위한 기본 데이터를 자동으로 생성하여 
 * 개발자가 매번 수동으로 데이터를 입력할 필요 없이 바로 테스트가 가능하도록 함
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;

    /**
     * 애플리케이션 시작 시 더미 데이터를 생성하는 Bean
     * 이유: SpringApplication.run() 완료 후 자동으로 실행되어 테스트 데이터 초기화
     * 
     * @return ApplicationRunner 인스턴스
     */
    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            if (meetingRepository.count() == 0) {
                log.info("더미 데이터 생성 시작...");
                createDummyData();
                log.info("더미 데이터 생성 완료!");
            } else {
                log.info("기존 데이터가 존재하므로 더미 데이터 생성을 건너뜁니다.");
            }
        };
    }

    /**
     * 실제 더미 데이터를 생성하는 메서드
     * 이유: 약속, 참여자 데이터를 생성하여 API 테스트 환경 구축
     * 사용자 데이터는 별도의 UserService(포트 8081)에서 관리하므로 생성하지 않음
     */
    @Transactional
    public void createDummyData() {
        // 1. 약속 데이터 생성
        createMeetings();
        
        // 2. 참여자 데이터 생성
        createParticipants();
    }



    /**
     * 약속 더미 데이터 생성
     * 이유: 다양한 상태의 약속을 미리 생성하여 상태 변경 및 조회 테스트가 가능하도록 함
     */
    private void createMeetings() {
        log.info("약속 데이터 생성 중...");
        
        Object[][] meetingData = {
            {"주말 맛집 탐방", "강남역 근처 맛집들을 돌아보며 즐거운 시간을 보내요!", 
             LocalDateTime.of(2024, 1, 20, 14, 0), "강남역 2번 출구", 
             "서울특별시 강남구 강남대로 396", "{\"latitude\": 37.498095, \"longitude\": 127.027610}", 
             6, Meeting.MeetingStatus.WAITING, LocalDateTime.of(2024, 1, 15, 9, 0)},
            
            {"영화 관람", "최신 영화를 함께 보러 가요", 
             LocalDateTime.of(2024, 1, 21, 19, 30), "롯데시네마 잠실점", 
             "서울특별시 송파구 올림픽로 240", "{\"latitude\": 37.513294, \"longitude\": 127.098422}", 
             4, Meeting.MeetingStatus.CONFIRMED, LocalDateTime.of(2024, 1, 15, 10, 0)},
            
            {"등산 모임", "북한산 등산을 함께 해요", 
             LocalDateTime.of(2024, 1, 22, 8, 0), "북한산 우이동 입구", 
             "서울특별시 강북구 우이동", "{\"latitude\": 37.663294, \"longitude\": 127.012422}", 
             8, Meeting.MeetingStatus.WAITING, LocalDateTime.of(2024, 1, 15, 11, 0)},
            
            {"카페 모임", "홍대 카페에서 수다 떨어요", 
             LocalDateTime.of(2024, 1, 23, 15, 0), "스타벅스 홍대점", 
             "서울특별시 마포구 양화로 160", "{\"latitude\": 37.556294, \"longitude\": 126.922422}", 
             5, Meeting.MeetingStatus.COMPLETED, LocalDateTime.of(2024, 1, 10, 12, 0)},
            
            {"보드게임 카페", "보드게임을 함께 즐겨요", 
             LocalDateTime.of(2024, 1, 25, 18, 0), "보드게임카페 코드게임", 
             "서울특별시 강남구 테헤란로 152", "{\"latitude\": 37.498594, \"longitude\": 127.028010}", 
             6, Meeting.MeetingStatus.CANCELLED, LocalDateTime.of(2024, 1, 15, 13, 0)}
        };

        for (Object[] data : meetingData) {
            Meeting meeting = new Meeting();
            meeting.setTitle((String) data[0]);
            meeting.setDescription((String) data[1]);
            meeting.setMeetingTime((LocalDateTime) data[2]);
            meeting.setLocationName((String) data[3]);
            meeting.setLocationAddress((String) data[4]);
            meeting.setLocationCoordinates((String) data[5]);
            meeting.setMaxParticipants((Integer) data[6]);
            meeting.setStatus((Meeting.MeetingStatus) data[7]);
            meeting.setCreatedAt((LocalDateTime) data[8]);
            meeting.setUpdatedAt((LocalDateTime) data[8]);
            
            meetingRepository.save(meeting);
        }
        
        log.info("약속 데이터 생성 완료: {}개", meetingData.length);
    }

    /**
     * 참여자 더미 데이터 생성
     * 이유: 약속에 참여하는 사용자들의 관계를 설정하여 참여자 관련 API 테스트가 가능하도록 함
     */
    private void createParticipants() {
        log.info("참여자 데이터 생성 중...");
        
        // 약속 1 (주말 맛집 탐방) 참여자들 - 방장: 사용자 1
        createParticipantData(1L, new Object[][]{
            {1L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 8, 55), LocalDateTime.of(2024, 1, 15, 9, 0)},  // 방장
            {2L, MeetingParticipant.ResponseStatus.INVITED, LocalDateTime.of(2024, 1, 15, 9, 0), null},
            {3L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 9, 5), LocalDateTime.of(2024, 1, 15, 10, 0)},
            {4L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 9, 10), LocalDateTime.of(2024, 1, 15, 11, 0)},
            {5L, MeetingParticipant.ResponseStatus.INVITED, LocalDateTime.of(2024, 1, 15, 9, 15), null}
        });

        // 약속 2 (영화 관람) 참여자들 - 방장: 사용자 2
        createParticipantData(2L, new Object[][]{
            {2L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 9, 55), LocalDateTime.of(2024, 1, 15, 10, 0)},  // 방장
            {1L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 10, 0), LocalDateTime.of(2024, 1, 15, 12, 0)},
            {3L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 10, 5), LocalDateTime.of(2024, 1, 15, 13, 0)},
            {6L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 10, 10), LocalDateTime.of(2024, 1, 15, 14, 0)}
        });

        // 약속 3 (등산 모임) 참여자들 - 방장: 사용자 3
        createParticipantData(3L, new Object[][]{
            {3L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 10, 55), LocalDateTime.of(2024, 1, 15, 11, 0)},  // 방장
            {1L, MeetingParticipant.ResponseStatus.INVITED, LocalDateTime.of(2024, 1, 15, 11, 0), null},
            {2L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 11, 5), LocalDateTime.of(2024, 1, 15, 16, 0)},
            {4L, MeetingParticipant.ResponseStatus.REJECTED, LocalDateTime.of(2024, 1, 15, 11, 10), null},
            {7L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 11, 15), LocalDateTime.of(2024, 1, 15, 18, 0)},
            {8L, MeetingParticipant.ResponseStatus.INVITED, LocalDateTime.of(2024, 1, 15, 11, 20), null}
        });

        // 약속 4 (카페 모임) 참여자들 - 완료된 약속, 방장: 사용자 1
        createParticipantData(4L, new Object[][]{
            {1L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 10, 11, 55), LocalDateTime.of(2024, 1, 10, 12, 0)},  // 방장
            {2L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 10, 12, 0), LocalDateTime.of(2024, 1, 10, 14, 0)},
            {5L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 10, 12, 5), LocalDateTime.of(2024, 1, 10, 15, 0)},
            {6L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 10, 12, 10), LocalDateTime.of(2024, 1, 10, 16, 0)}
        });

        // 약속 5 (보드게임 카페) 참여자들 - 취소된 약속, 방장: 사용자 4
        createParticipantData(5L, new Object[][]{
            {4L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 12, 55), LocalDateTime.of(2024, 1, 15, 13, 0)},  // 방장
            {1L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 13, 0), LocalDateTime.of(2024, 1, 15, 19, 0)},
            {3L, MeetingParticipant.ResponseStatus.INVITED, LocalDateTime.of(2024, 1, 15, 13, 5), null},
            {7L, MeetingParticipant.ResponseStatus.ACCEPTED, LocalDateTime.of(2024, 1, 15, 13, 10), LocalDateTime.of(2024, 1, 15, 20, 0)}
        });
        
        log.info("참여자 데이터 생성 완료");
    }

    /**
     * 특정 약속의 참여자 데이터를 생성하는 헬퍼 메서드
     * 이유: 중복 코드를 줄이고 참여자 데이터 생성 로직을 일관성 있게 관리하기 위해
     * 
     * @param meetingId 약속 ID
     * @param participantData 참여자 데이터 배열
     */
    private void createParticipantData(Long meetingId, Object[][] participantData) {
        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) {
            log.warn("약속을 찾을 수 없습니다. ID: {}", meetingId);
            return;
        }

        for (Object[] data : participantData) {
            MeetingParticipant participant = new MeetingParticipant();
            participant.setMeetingId(meetingId);
            participant.setUserId((Long) data[0]);
            participant.setResponse((MeetingParticipant.ResponseStatus) data[1]);
            participant.setInvitedAt((LocalDateTime) data[2]);
            participant.setJoinedAt((LocalDateTime) data[3]);
            
            participantRepository.save(participant);
        }
    }

    /**
     * 테스트용 사용자 데이터 생성
     * 이유: 자동 카카오톡 알림 시스템 테스트를 위한 기본 사용자 데이터 제공
     * 실제 User 엔티티가 없으므로 간단한 Mock 데이터로 생성
     */
    private void createTestUsers() {
        log.info("테스트용 사용자 데이터 생성 중...");
        
        // TODO: 실제 User 엔티티가 구현되면 이 부분을 UserRepository를 사용하도록 수정
        // 이유: 현재는 User 엔티티가 없어서 Mock 데이터로 처리, 추후 실제 사용자 관리 시스템 연결 필요
        
        log.info("테스트용 사용자 데이터 생성 완료 - 사용자 ID 1~8 사용 가능");
        log.info("이제 /api/meetings로 약속 생성 시 자동 카카오톡 알림 테스트 가능");
    }
}

