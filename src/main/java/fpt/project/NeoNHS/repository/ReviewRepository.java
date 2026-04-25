package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.dto.response.review.ReviewMetadata;
import fpt.project.NeoNHS.entity.Review;
import fpt.project.NeoNHS.entity.ReviewImage;
import fpt.project.NeoNHS.enums.ReviewStatus;
import fpt.project.NeoNHS.enums.ReviewTypeFlagEnum;
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

    Page<Review> findByReviewTypeIdAndReviewTypeFlgAndStatus(UUID reviewTypeId, Integer reviewTypeFlg, ReviewStatus status, Pageable pageable);

    @Query("""
            SELECT r FROM Review r
            JOIN WorkshopTemplate wt ON r.reviewTypeId = wt.id
            WHERE wt.id = :workshopTemplateId AND r.reviewTypeFlg = :type AND r.status = :status
            """)
    Page<Review> pageVisibleReviewsForWorkshopTemplate(
            @Param("workshopTemplateId") UUID workshopTemplateId,
            @Param("status") ReviewStatus status,
            @Param("type") ReviewTypeFlagEnum type,
            Pageable pageable);

    @Query("""
            SELECT r FROM Review r
            JOIN Event e ON r.reviewTypeId = e.id
            WHERE e.id = :eventId AND r.reviewTypeFlg = :type AND r.status = :status
            """)
    Page<Review> pageVisibleReviewsForEvent(
            @Param("eventId") UUID eventId,
            @Param("status") ReviewStatus status,
            @Param("type") ReviewTypeFlagEnum type,
            Pageable pageable);


    @Query(
            value = """
                        SELECT r FROM Review r
                        WHERE r.reviewTypeId = :pointId
                          AND r.reviewTypeFlg = :type
                          AND r.status = :status
                    """,
            countQuery = """
                        SELECT count(r) FROM Review r
                        WHERE r.reviewTypeId = :pointId
                          AND r.reviewTypeFlg = :type
                          AND r.status = :status
                    """
    )
    Page<Review> getPageVisibleReview(
            @Param("pointId") UUID pointId,
            @Param("status") ReviewStatus status,
            @Param("type") ReviewTypeFlagEnum type,
            Pageable pageable);

    @Query("""
                SELECT new  fpt.project.NeoNHS.dto.response.review.ReviewMetadata (AVG(r.rating), COUNT(r))
                FROM Review r
                WHERE r.reviewTypeId = :reviewId
                  AND r.reviewTypeFlg = :type
                  AND r.status = :status
            """)
    ReviewMetadata getReviewStats(@Param("reviewId") UUID reviewId,
                                  @Param("type") ReviewTypeFlagEnum type,
                                  @Param("status") ReviewStatus status);

    Long countByReviewTypeIdAndReviewTypeFlgAndStatus(UUID reviewTypeId, ReviewTypeFlagEnum reviewTypeFlg, ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewTypeId = :reviewTypeId AND r.reviewTypeFlg = :reviewTypeFlg AND r.status = :status")
    Double getAverageRatingByReviewType(@Param("reviewTypeId") UUID reviewTypeId, @Param("reviewTypeFlg") ReviewTypeFlagEnum reviewTypeFlg,
                                        @Param("status") ReviewStatus status);

    boolean existsByUser_IdAndReviewTypeIdAndReviewTypeFlgAndDeletedAtIsNull(UUID userId, UUID reviewTypeId, ReviewTypeFlagEnum reviewTypeFlg);

    @Query("""
                SELECT wt.id, wt.name, COUNT(r), COALESCE(AVG(r.rating), 0),
                       SUM(CASE WHEN r.createdAt >= :since THEN 1 ELSE 0 END)
                FROM Review r
                JOIN WorkshopTemplate wt ON r.reviewTypeId = wt.id
                WHERE r.reviewTypeFlg = :type
                  AND wt.vendor.id = :vendorId
                  AND r.status = fpt.project.NeoNHS.enums.ReviewStatus.VISIBLE
                GROUP BY wt.id, wt.name
                ORDER BY COUNT(r) DESC
            """)
    List<Object[]> findWorkshopReviewSummariesByVendorId(
            @Param("vendorId") UUID vendorId,
            @Param("since") LocalDateTime since, @Param("type") ReviewTypeFlagEnum type);


    @Query("""
        SELECT ri FROM ReviewImage ri
        JOIN ri.review r
        WHERE r.reviewTypeId = :reviewTypeId
    """)
    Page<ReviewImage> findAllImagesByTarget(@Param("reviewTypeId") UUID reviewTypeId, Pageable pageable);
}
