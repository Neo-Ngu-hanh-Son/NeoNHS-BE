package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.enums.PointType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public interface PointRepository extends JpaRepository<Point, UUID>, JpaSpecificationExecutor<Point> {
    @Query("SELECT p FROM Point p WHERE p.attraction.id = :attractionId " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.orderIndex ASC")
    List<Point> findByAttractionIdOrderByOrderIndexAsc(UUID attractionId);

    // Phân trang danh sách các điểm theo Attraction ID
    @Query("SELECT p FROM Point p WHERE p.attraction.id = :attractionId " +
            "AND p.deletedAt IS NULL " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Point> findByAttractionIdWithSearch(
            @Param("attractionId") UUID attractionId,
            @Param("search") String search,
            Pageable pageable);

    long countByDeletedAtIsNull();

    Page<Point> findAll(Specification<Point> spec, Pageable pageable);
}
