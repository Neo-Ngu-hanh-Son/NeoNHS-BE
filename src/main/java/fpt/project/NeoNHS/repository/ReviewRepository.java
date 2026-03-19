package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Review;
import fpt.project.NeoNHS.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByWorkshopTemplateIdAndStatus(UUID workshopTemplateId, ReviewStatus status, Pageable pageable);

    Long countByWorkshopTemplateIdAndStatus(UUID workshopTemplateId, ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.workshopTemplate.id = :workshopTemplateId AND r.status = :status")
    Double getAverageRatingByWorkshopTemplateId(@Param("workshopTemplateId") UUID workshopTemplateId, @Param("status") ReviewStatus status);

    @Query("""
        SELECT wt.id, wt.name, COUNT(r), COALESCE(AVG(r.rating), 0),
               SUM(CASE WHEN r.createdAt >= :since THEN 1 ELSE 0 END)
        FROM Review r
        JOIN r.workshopTemplate wt
        WHERE wt.vendor.id = :vendorId
          AND r.status = fpt.project.NeoNHS.enums.ReviewStatus.VISIBLE
        GROUP BY wt.id, wt.name
        ORDER BY COUNT(r) DESC
    """)
    List<Object[]> findWorkshopReviewSummariesByVendorId(
            @Param("vendorId") UUID vendorId,
            @Param("since") LocalDateTime since);
}
