package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PointRepository extends JpaRepository<Point, UUID> {
        List<Point> findByAttractionIdOrderByOrderIndexAsc(UUID attractionId);

        // Phân trang danh sách các điểm theo Attraction ID
        @Query("SELECT p FROM Point p WHERE p.attraction.id = :attractionId " +
                        "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Point> findByAttractionIdWithSearch(
                        @Param("attractionId") UUID attractionId,
                        @Param("search") String search,
                        Pageable pageable);

        long countByDeletedAtIsNull();
}
