package com.promiseservice.model.entity;

import com.promiseservice.model.enums.PlaceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 장소 엔티티
 * 이유: 약속 장소 정보를 체계적으로 관리하고 placeId 기반 API를 지원하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Entity
@Table(name = "places")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Place {

    /**
     * PK
     * 이유: 장소를 고유하게 구분하기 위한 기본 키, API의 placeId로 사용됨
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 장소 이름
     * 이유: 사용자가 쉽게 식별할 수 있는 장소명을 저장하기 위해
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * 장소 주소
     * 이유: 장소의 정확한 위치 정보를 제공하기 위해
     */
    @Column(name = "address", length = 500)
    private String address;


    /**
     * 장소 카테고리
     * 이유: 장소 유형별 분류와 검색 기능을 위해
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 활성화 여부
     * 이유: 장소를 논리적으로 삭제하여 기존 약속의 참조 무결성을 보장하기 위해
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 장소 상태
     * 이유: 장소의 생명주기를 관리하기 위해 (DRAFT/ACTIVE/INACTIVE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PlaceStatus status = PlaceStatus.DRAFT;

    /**
     * 외부 소스
     * 이유: 카카오맵, 네이버맵 등 외부 API에서 가져온 장소인지 구분하기 위해
     */
    @Column(name = "source", length = 20)
    private String source;

    /**
     * 외부 ID
     * 이유: 외부 API의 장소 ID를 저장하여 중복 생성을 방지하기 위해
     */
    @Column(name = "external_id", length = 100)
    private String externalId;

    /**
     * 생성 시각
     * 이유: 장소 정보가 언제 생성되었는지 기록하기 위해
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     * 이유: 장소 정보가 언제 마지막으로 수정되었는지 기록하기 위해
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}