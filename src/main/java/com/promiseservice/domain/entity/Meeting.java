package com.promiseservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meetings")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_id", nullable = false)
    private Long hostId; // UserService의 User.id 참조

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)  // H2 호환을 위해 VARCHAR로 변경
    private String description;

    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

    @Column(name = "max_participants")
    private Integer maxParticipants = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "`status`", nullable = false)
    private MeetingStatus status = MeetingStatus.WAITING;

    @Column(name = "location_name", length = 500)
    private String locationName;

    @Column(name = "location_address", length = 500)
    private String locationAddress;

    @Column(name = "location_coordinates")
    private String locationCoordinates; // JSON 형태로 위도,경도

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingHistory> history = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MeetingStatus {
        WAITING, CONFIRMED, COMPLETED, CANCELLED;

        /**
         * 상태의 사용자 친화적인 표시명을 반환하는 메서드
         * 이유: 알림이나 UI에서 약속 상태를 사용자가 이해하기 쉽게 표시하기 위해
         * 
         * @return 상태의 표시명
         */
        public String getDisplayName() {
            switch (this) {
                case WAITING:
                    return "대기 중";
                case CONFIRMED:
                    return "확정";
                case COMPLETED:
                    return "완료";
                case CANCELLED:
                    return "취소";
                default:
                    return this.name();
            }
        }
    }
}

