package com.promiseservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
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
     * 위도
     * 이유: 지도 표시와 위치 기반 서비스를 위한 정확한 좌표 정보 저장
     */
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    /**
     * 경도
     * 이유: 지도 표시와 위치 기반 서비스를 위한 정확한 좌표 정보 저장
     */
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

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