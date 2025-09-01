package com.promiseservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 약속 엔티티
 * 이유: 사용자들이 모여서 만날 약속의 정보를 관리하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "meeting")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Meeting {

    /**
     * PK
     * 이유: 약속을 고유하게 구분하기 위한 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 약속 제목
     * 이유: 약속을 식별할 수 있는 제목을 제공하기 위해
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * 약속 상세 설명
     * 이유: 약속의 목적, 준비사항, 주의사항 등 부가 정보를 제공하기 위해
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 약속 예정 시간
     * 이유: 언제 약속이 진행될지 명시하기 위해
     */
    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

    /**
     * 최대 참여 인원 수
     * 이유: 약속에 참여할 수 있는 최대 인원을 제한하기 위해
     */
    @Column(name = "max_participants")
    private Integer maxParticipants;

    /**
     * 약속 진행 상태
     * 이유: 약속의 현재 진행 상황을 관리하기 위해
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    /**
     * 약속 방장(호스트) 사용자 ID
     * 이유: 누가 약속을 주도하는지 식별하기 위해
     */
    @Column(name = "host_id", nullable = false)
    private Long hostId;

    /**
     * 약속 장소명
     * 이유: 어디서 약속이 진행될지 명시하기 위해
     */
    @Column(name = "location_name", length = 500)
    private String locationName;

    /**
     * 약속 장소 상세 주소
     * 이유: 약속 장소의 정확한 위치를 제공하기 위해
     */
    @Column(name = "location_address", length = 500)
    private String locationAddress;

    /**
     * 약속 장소 좌표 정보
     * 이유: 지도 표시나 경로 안내를 위한 좌표 정보를 저장하기 위해
     */
    @Column(name = "location_coordinates", columnDefinition = "TEXT")
    private String locationCoordinates;

    /**
     * 생성 시각
     * 이유: 약속이 언제 생성되었는지 기록하기 위해
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     * 이유: 약속 정보가 언제 마지막으로 수정되었는지 기록하기 위해
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 참가자 목록
     * 이유: 약속에 참여하는 사용자들을 관리하기 위해
     */
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeetingParticipant> participants = new ArrayList<>();
}
