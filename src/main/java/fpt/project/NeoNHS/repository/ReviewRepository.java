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

    Page<Review> findByReviewTypeIdAndReviewTypeFlgAndStatus(UUID reviewTypeId, fpt.project.NeoNHS.enums.ReviewTypeFlg reviewTypeFlg, ReviewStatus status, Pageable pageable);

    @Query("""
            SELECT r FROM Review r
            JOIN WorkshopTemplate wt ON r.reviewTypeId = wt.id
            WHERE wt.id = :workshopTemplateId AND r.reviewTypeFlg = fpt.project.NeoNHS.enums.ReviewTypeFlg.WORKSHOP AND r.status = :status
            """)
    Page<Review> pageVisibleReviewsForWorkshopTemplate(
            @Param("workshopTemplateId") UUID workshopTemplateId,
            @Param("status") ReviewStatus status,
            Pageable pageable);

    @Query("""
            SELECT r FROM Review r
            JOIN Event e ON r.reviewTypeId = e.id
            WHERE e.id = :eventId AND r.reviewTypeFlg = fpt.project.NeoNHS.enums.ReviewTypeFlg.EVENT AND r.status = :status
            """)
    Page<Review> pageVisibleReviewsForEvent(
            @Param("eventId") UUID eventId,
            @Param("status") ReviewStatus status,
            Pageable pageable);

    @Query("""
            SELECT r FROM Review r
            JOIN Point p ON r.reviewTypeId = p.id
            WHERE p.id = :pointId AND r.reviewTypeFlg = fpt.project.NeoNHS.enums.ReviewTypeFlg.POINT AND r.status = :status
            """)
    Page<Review> pageVisibleReviewsForPoint(
            @Param("pointId") UUID pointId,
            @Param("status") ReviewStatus status,
            Pageable pageable);

    Long countByReviewTypeIdAndReviewTypeFlgAndStatus(UUID reviewTypeId, fpt.project.NeoNHS.enums.ReviewTypeFlg reviewTypeFlg, ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewTypeId = :reviewTypeId AND r.reviewTypeFlg = :reviewTypeFlg AND r.status = :status")
    Double getAverageRatingByReviewType(@Param("reviewTypeId") UUID reviewTypeId, @Param("reviewTypeFlg") fpt.project.NeoNHS.enums.ReviewTypeFlg reviewTypeFlg, @Param("status") ReviewStatus status);

    boolean existsByUser_IdAndReviewTypeIdAndReviewTypeFlgAndDeletedAtIsNull(UUID userId, UUID reviewTypeId, fpt.project.NeoNHS.enums.ReviewTypeFlg reviewTypeFlg);

    @Query("""
        SELECT wt.id, wt.name, COUNT(r), COALESCE(AVG(r.rating), 0),
               SUM(CASE WHEN r.createdAt >= :since THEN 1 ELSE 0 END)
        FROM Review r
        JOIN WorkshopTemplate wt ON r.reviewTypeId = wt.id
        WHERE r.reviewTypeFlg = fpt.project.NeoNHS.enums.ReviewTypeFlg.WORKSHOP
          AND wt.vendor.id = :vendorId
          AND r.status = fpt.project.NeoNHS.enums.ReviewStatus.VISIBLE
        GROUP BY wt.id, wt.name
        ORDER BY COUNT(r) DESC
    """)
    List<Object[]> findWorkshopReviewSummariesByVendorId(
            @Param("vendorId") UUID vendorId,
            @Param("since") LocalDateTime since);
}
