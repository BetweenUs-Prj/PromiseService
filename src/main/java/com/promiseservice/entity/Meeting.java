package com.promiseservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 약속(미팅) 정보를 저장하고 관리하는 JPA 엔티티
 * 이유: 사용자가 생성한 약속의 상세 정보, 참여자 관리, 상태 추적을 통합적으로 처리하여
 * 약속 생성부터 완료까지의 전체 생명주기를 체계적으로 관리하기 위해
 * 
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "meeting", indexes = {
    // 이유: 약속 시간으로 검색하는 쿼리 성능 최적화를 위해
    @Index(name = "idx_meeting_time", columnList = "meeting_time"),
    // 이유: 약속 상태별 조회 성능 향상을 위해 
    @Index(name = "idx_status", columnList = "status"),
    // 이유: 방장이 주최한 약속 목록 조회 성능 최적화를 위해
    @Index(name = "idx_host_id", columnList = "host_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Meeting {

    /**
     * 약속 고유 식별자
     * 이유: 시스템 내에서 각 약속을 유일하게 구분하고 참조하기 위한 기본키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약속 제목
     * 이유: 사용자가 약속을 쉽게 식별하고 구분할 수 있도록 하기 위해
     * 알림 메시지 및 목록 화면에서 주요 식별 정보로 활용
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * 약속 상세 설명
     * 이유: 약속의 목적, 준비사항, 주의사항 등 부가 정보를 제공하여
     * 참여자들이 약속에 대해 충분히 이해할 수 있도록 지원
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 약속 예정 시간
     * 이유: 실제 모임이 시작되는 정확한 시점을 저장하여
     * 알림 발송, 일정 관리, 과거/미래 약속 구분의 기준으로 활용
     */
    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

    /**
     * 최대 참여 인원 수
     * 이유: 장소 수용 인원이나 모임 규모를 제한하여 원활한 진행을 보장하고
     * 참여 신청 시 인원 초과를 방지하기 위해
     */
    @Column(name = "max_participants")
    private Integer maxParticipants = 10;

    /**
     * 약속 진행 상태
     * 이유: 약속의 현재 진행 단계를 추적하여 각 상태에 맞는 비즈니스 로직을 적용하고
     * 사용자에게 정확한 상태 정보를 제공하기 위해
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MeetingStatus status = MeetingStatus.WAITING;

    /**
     * 약속 방장(호스트) 사용자 ID
     * 이유: 약속을 생성한 사용자를 식별하여 방장 권한(수정, 삭제, 확정)을 제어하고
     * 방장 관련 비즈니스 로직을 처리하기 위해
     */
    @Column(name = "host_id", nullable = false)
    private Long hostId;

    /**
     * 약속 장소명
     * 이유: 참여자들이 쉽게 인식할 수 있는 장소 이름을 제공하여
     * 약속 위치를 직관적으로 파악할 수 있도록 지원
     */
    @Column(name = "location_name", length = 500)
    private String locationName;

    /**
     * 약속 장소 상세 주소
     * 이유: 정확한 위치 정보를 제공하여 참여자들의 길찾기를 지원하고
     * 지도 서비스와 연동할 때 활용하기 위해
     */
    @Column(name = "location_address", length = 500)
    private String locationAddress;

    /**
     * 약속 장소 좌표 정보 (JSON 형태의 위도/경도)
     * 이유: 지도 서비스 연동 및 위치 기반 기능(거리 계산, 경로 안내 등)을 제공하기 위해
     * JSON 형태로 저장하여 확장성과 유연성을 확보
     */
    @Column(name = "location_coordinates", columnDefinition = "TEXT")
    private String locationCoordinates;

    /**
     * 약속 참여자 목록
     * 이유: 약속에 초대된 사용자들과 그들의 응답 상태를 관리하여
     * 참여자 현황 파악 및 알림 발송 대상 선정에 활용
     */
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MeetingParticipant> participants = new ArrayList<>();

    /**
     * 약속 상태 변경 이력
     * 이유: 약속의 상태 변경 과정을 추적하여 감사 로그를 제공하고
     * 문제 발생 시 원인 분석 및 디버깅을 지원
     */
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingHistory> history = new ArrayList<>();

    /**
     * 약속 생성 시간
     * 이유: 약속이 언제 생성되었는지 기록하여 생성 순서 정렬, 통계 분석,
     * 오래된 데이터 정리 등에 활용하기 위해 (자동 설정)
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 약속 정보 최종 수정 시간
     * 이유: 약속 정보의 최신성을 추적하고 변경 이력을 관리하여
     * 동시성 제어 및 캐시 무효화 판단 기준으로 활용 (자동 갱신)
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 약속 진행 상태를 나타내는 열거형
     * 이유: 약속의 생명주기를 체계적으로 관리하고 각 단계에 맞는 비즈니스 로직을 적용하기 위해
     * 상태별로 허용되는 작업을 제한하여 데이터 일관성과 비즈니스 규칙을 보장
     */
    public enum MeetingStatus {
        /** 대기 중: 방장이 약속을 생성했지만 아직 확정하지 않은 상태 */
        WAITING("대기 중"),
        /** 확정: 방장이 약속을 확정하여 참여자들에게 알림이 발송된 상태 */
        CONFIRMED("확정"),
        /** 완료: 약속이 끝나고 완료 처리된 상태 */
        COMPLETED("완료"),
        /** 취소: 약속이 취소된 상태 */
        CANCELLED("취소");

        private final String displayName;

        /**
         * MeetingStatus 생성자
         * 이유: 각 상태별로 사용자에게 표시될 한글 이름을 설정하여
         * UI에서 직관적인 상태 정보를 제공하기 위해
         */
        MeetingStatus(String displayName) {
            this.displayName = displayName;
        }

        /**
         * 상태의 사용자 친화적인 표시명을 반환하는 메서드
         * 이유: 알림 메시지, UI 화면에서 약속 상태를 사용자가 이해하기 쉽게 표시하기 위해
         * 시스템 내부 코드값 대신 한글 표시명을 제공하여 사용자 경험을 향상
         * 
         * @return 상태의 한글 표시명
         */
        public String getDisplayName() {
            return this.displayName;
        }

        /**
         * 약속 상태가 수정 가능한지 확인하는 메서드
         * 이유: 비즈니스 규칙에 따라 특정 상태에서만 약속 정보 수정을 허용하여
         * 데이터 무결성과 사용자 혼란을 방지하기 위해
         * 
         * @return 수정 가능 여부 (WAITING, CONFIRMED 상태에서만 true)
         */
        public boolean isEditable() {
            return this == WAITING || this == CONFIRMED;
        }

        /**
         * 약속 상태가 완료된 상태인지 확인하는 메서드
         * 이유: 완료된 약속에 대한 특별한 처리 로직을 적용하기 위해
         * 
         * @return 완료 상태 여부 (COMPLETED, CANCELLED인 경우 true)
         */
        public boolean isFinal() {
            return this == COMPLETED || this == CANCELLED;
        }
    }

    /**
     * 특정 사용자가 약속의 방장(호스트)인지 확인하는 메서드
     * 이유: 약속 수정, 삭제, 확정 등의 방장 전용 기능에 대한 권한을 검증하여
     * 무단 수정을 방지하고 보안을 강화하기 위해
     * 
     * @param userId 확인할 사용자 ID (null 체크 포함)
     * @return 방장 여부 (hostId가 null이거나 일치하지 않으면 false)
     */
    public boolean isHost(Long userId) {
        // null 안전성 확보
        // 이유: userId나 hostId가 null인 경우 NullPointerException을 방지하기 위해
        return this.hostId != null && this.hostId.equals(userId);
    }

    /**
     * 약속의 기본 정보(제목, 설명, 시간)를 업데이트하는 메서드
     * 이유: 도메인 객체의 캡슐화를 유지하면서 필수 정보를 안전하게 변경하고
     * 비즈니스 규칙 검증을 한 곳에서 처리하기 위해
     * 
     * @param title 새로운 약속 제목 (null 및 빈 문자열 체크 필요)
     * @param description 새로운 약속 설명 (null 허용)
     * @param meetingTime 새로운 약속 시간 (과거 시간 검증 필요)
     * @throws IllegalArgumentException 잘못된 입력값인 경우
     */
    public void updateMeetingInfo(String title, String description, LocalDateTime meetingTime) {
        // 필수 입력값 검증
        // 이유: 데이터 무결성을 보장하고 잘못된 값으로 인한 오류를 사전에 방지하기 위해
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("약속 제목은 필수 입력값입니다");
        }
        if (meetingTime == null) {
            throw new IllegalArgumentException("약속 시간은 필수 입력값입니다");
        }
        
        // 과거 시간 검증 (선택적 - 비즈니스 요구사항에 따라)
        // 이유: 이미 지난 시간으로 약속을 설정하는 것을 방지하기 위해
        if (meetingTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("약속 시간은 현재 시간 이후여야 합니다");
        }

        this.title = title.trim();
        this.description = description;
        this.meetingTime = meetingTime;
    }

    /**
     * 약속 상태를 변경하는 메서드
     * 이유: 상태 변경 시 비즈니스 규칙을 적용하고 유효성을 검증하여
     * 잘못된 상태 전환을 방지하고 데이터 일관성을 보장하기 위해
     * 
     * @param newStatus 변경할 새로운 상태 (null 체크 포함)
     * @throws IllegalArgumentException 잘못된 상태 전환인 경우
     * @throws IllegalStateException 현재 상태에서 변경이 불가능한 경우
     */
    public void updateStatus(MeetingStatus newStatus) {
        // 입력값 검증
        // 이유: null 상태로의 변경을 방지하여 데이터 무결성을 보장하기 위해
        if (newStatus == null) {
            throw new IllegalArgumentException("약속 상태는 null일 수 없습니다");
        }

        // 상태 전환 유효성 검증
        // 이유: 비즈니스 규칙에 따라 불가능한 상태 전환을 차단하기 위해
        if (this.status.isFinal() && !newStatus.isFinal()) {
            throw new IllegalStateException("완료된 약속의 상태는 되돌릴 수 없습니다");
        }

        this.status = newStatus;
    }

    /**
     * 약속 장소 관련 정보를 일괄 업데이트하는 메서드
     * 이유: 장소명, 주소, 좌표 정보를 원자적으로 처리하여 데이터 일관성을 보장하고
     * 부분적인 업데이트로 인한 정보 불일치를 방지하기 위해
     * 
     * @param locationName 장소명 (null 허용)
     * @param locationAddress 상세 주소 (null 허용)
     * @param locationCoordinates 좌표 정보 JSON (null 허용, 형식 검증 필요)
     */
    public void updateLocation(String locationName, String locationAddress, String locationCoordinates) {
        // 좌표 정보 형식 검증 (JSON 형태)
        // 이유: 잘못된 형식의 좌표 데이터로 인한 지도 서비스 연동 오류를 방지하기 위해
        if (locationCoordinates != null && !locationCoordinates.trim().isEmpty()) {
            if (!isValidJsonCoordinates(locationCoordinates)) {
                throw new IllegalArgumentException("좌표 정보는 올바른 JSON 형식이어야 합니다");
            }
        }

        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationCoordinates = locationCoordinates;
    }

    /**
     * 현재 약속에 참여 확정한 사용자 수를 계산하는 메서드
     * 이유: 실시간으로 참여자 현황을 파악하여 최대 인원 제한 검증,
     * UI 표시, 알림 발송 여부 결정에 활용하기 위해
     * 
     * @return 참여 확정(ACCEPTED) 상태인 참여자 수 (0 이상)
     */
    public int getCurrentParticipantCount() {
        // Stream API를 활용한 효율적인 필터링 및 카운팅
        // 이유: 대량의 참여자 데이터도 효율적으로 처리하고 가독성을 높이기 위해
        return (int) participants.stream()
                .filter(participant -> participant.getResponse() == MeetingParticipant.ResponseStatus.ACCEPTED)
                .count();
    }

    /**
     * 최대 참여자 수에 도달했는지 확인하는 메서드
     * 이유: 새로운 참여자 초대 또는 참여 신청 시 인원 제한을 확인하여
     * 과도한 참여로 인한 문제를 사전에 방지하기 위해
     * 
     * @return 최대 참여자 수 도달 여부 (도달 시 true, 여유 있음 false)
     */
    public boolean isMaxParticipantsReached() {
        return getCurrentParticipantCount() >= maxParticipants;
    }

    /**
     * 약속이 과거 시간인지 확인하는 메서드
     * 이유: 이미 지난 약속에 대한 특별한 처리 로직 적용 및
     * 새로운 참여자 초대 제한, 알림 발송 방지 등에 활용하기 위해
     * 
     * @return 과거 약속 여부 (현재 시간보다 이전이면 true)
     */
    public boolean isPastMeeting() {
        return this.meetingTime != null && this.meetingTime.isBefore(LocalDateTime.now());
    }

    /**
     * JSON 좌표 정보의 유효성을 검증하는 private 유틸리티 메서드
     * 이유: 좌표 정보 형식 검증 로직을 분리하여 재사용성을 높이고
     * 메인 비즈니스 로직의 가독성을 향상시키기 위해
     * 
     * @param coordinates 검증할 좌표 JSON 문자열
     * @return 유효한 JSON 형식 여부
     */
    private boolean isValidJsonCoordinates(String coordinates) {
        // 간단한 JSON 형식 검증 (실제로는 JSON 파서 사용 권장)
        // 이유: 기본적인 형식 오류를 빠르게 감지하기 위해
        return coordinates.trim().startsWith("{") && coordinates.trim().endsWith("}");
    }

    /**
     * 정적 팩토리 메서드 - 새로운 약속 생성
     * 이유: 복잡한 객체 생성 과정을 단순화하고 필수/선택 필드를 명확히 구분하여
     * 객체 생성 시 실수를 방지하고 코드 가독성을 향상시키기 위해
     * 
     * @param title 약속 제목 (필수)
     * @param description 약속 설명 (선택)
     * @param meetingTime 약속 시간 (필수)
     * @param maxParticipants 최대 참여자 수 (선택, 기본값 10)
     * @param hostId 방장 사용자 ID (필수)
     * @param locationName 장소명 (선택)
     * @param locationAddress 장소 주소 (선택)
     * @param locationCoordinates 장소 좌표 (선택)
     */
    public static Meeting create(String title, String description, LocalDateTime meetingTime, 
                               Integer maxParticipants, Long hostId, String locationName, 
                               String locationAddress, String locationCoordinates) {
        // 필수 필드 유효성 검증
        // 이유: 생성 시점에 필수 데이터의 누락을 방지하기 위해
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("약속 제목은 필수입니다");
        }
        if (meetingTime == null) {
            throw new IllegalArgumentException("약속 시간은 필수입니다");
        }
        if (hostId == null) {
            throw new IllegalArgumentException("방장 ID는 필수입니다");
        }

        Meeting meeting = new Meeting();
        meeting.setTitle(title.trim());
        meeting.setDescription(description);
        meeting.setMeetingTime(meetingTime);
        // 기본값 설정 - maxParticipants가 null인 경우 10으로 설정
        meeting.setMaxParticipants(maxParticipants != null ? maxParticipants : 10);
        meeting.setHostId(hostId);
        meeting.setLocationName(locationName);
        meeting.setLocationAddress(locationAddress);
        meeting.setLocationCoordinates(locationCoordinates);
        // 기본 상태 설정
        meeting.setStatus(MeetingStatus.WAITING);
        // 컬렉션 초기화
        meeting.setParticipants(new ArrayList<>());
        meeting.setHistory(new ArrayList<>());
        
        return meeting;
    }

    /**
     * 정적 팩토리 메서드 - 기본 약속 생성 (필수 정보만)
     * 이유: 간단한 약속 생성을 위한 편의 메서드
     */
    public static Meeting createBasic(String title, LocalDateTime meetingTime, Long hostId) {
        return create(title, null, meetingTime, null, hostId, null, null, null);
    }
}