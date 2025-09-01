package com.promiseservice.repository;

import com.promiseservice.model.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 장소 레포지토리
 * 이유: 장소 데이터에 대한 CRUD 작업과 검색 기능을 제공하기 위해
 *
 * @author PromiseService Team
 * @since 1.0.0
 */
@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    /**
     * 활성화된 장소만 조회
     * 이유: 논리적으로 삭제된 장소는 제외하고 유효한 장소만 반환하기 위해
     */
    @Query("SELECT p FROM Place p WHERE p.isActive = true")
    List<Place> findAllActive();

    /**
     * ID로 활성화된 장소 조회
     * 이유: 특정 장소를 조회할 때 삭제된 장소는 제외하기 위해
     */
    @Query("SELECT p FROM Place p WHERE p.id = :id AND p.isActive = true")
    Optional<Place> findByIdAndActive(@Param("id") Long id);

    /**
     * 이름으로 장소 검색
     * 이유: 사용자가 장소명으로 검색할 수 있는 기능을 제공하기 위해
     */
    @Query("SELECT p FROM Place p WHERE p.name LIKE %:name% AND p.isActive = true")
    List<Place> findByNameContaining(@Param("name") String name);

    /**
     * 카테고리별 장소 조회
     * 이유: 장소 유형별로 분류하여 조회할 수 있는 기능을 제공하기 위해
     */
    @Query("SELECT p FROM Place p WHERE p.category = :category AND p.isActive = true")
    List<Place> findByCategory(@Param("category") String category);

    /**
     * 주소로 장소 검색
     * 이유: 사용자가 주소로 장소를 검색할 수 있는 기능을 제공하기 위해
     */
    @Query("SELECT p FROM Place p WHERE p.address LIKE %:address% AND p.isActive = true")
    List<Place> findByAddressContaining(@Param("address") String address);
}