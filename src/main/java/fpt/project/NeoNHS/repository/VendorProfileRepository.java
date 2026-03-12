package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.VendorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, UUID> {

        Optional<VendorProfile> findByUserEmail(String email);

        Optional<VendorProfile> findByUserId(UUID userId);

        // Filter by verification status
        Page<VendorProfile> findByIsVerified(Boolean isVerified, Pageable pageable);

        // Filter by user banned status
        Page<VendorProfile> findByUserIsBanned(Boolean isBanned, Pageable pageable);

        // Filter by user active status
        Page<VendorProfile> findByUserIsActive(Boolean isActive, Pageable pageable);

        // Search by keyword (business name, user fullname, user email)
        @Query("SELECT vp FROM VendorProfile vp WHERE " +
                        "LOWER(vp.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(vp.user.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(vp.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<VendorProfile> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

        // Advanced search and filter
        @Query("SELECT vp FROM VendorProfile vp WHERE " +
                        "(:keyword IS NULL OR " +
                        "LOWER(vp.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(vp.user.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(vp.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:isVerified IS NULL OR vp.isVerified = :isVerified) AND " +
                        "(:isBanned IS NULL OR vp.user.isBanned = :isBanned) AND " +
                        "(:isActive IS NULL OR vp.user.isActive = :isActive)")
        Page<VendorProfile> advancedSearchAndFilter(
                        @Param("keyword") String keyword,
                        @Param("isVerified") Boolean isVerified,
                        @Param("isBanned") Boolean isBanned,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as period, COUNT(*) as count " +
                        "FROM vendor_profiles GROUP BY period ORDER BY period DESC LIMIT :limit", nativeQuery = true)
        List<Map<String, Object>> getMonthlyRegistrationStats(@Param("limit") Integer limit);

        @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as period, COUNT(*) as count " +
                        "FROM vendor_profiles " +
                        "WHERE created_at >= :start " +
                        "  AND created_at < :end " +
                        "GROUP BY period", nativeQuery = true)
        List<Map<String, Object>> getMonthlyRegistrationStatsBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end
        );

        @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-Week %u') as period, COUNT(*) as count " +
                        "FROM vendor_profiles " +
                        "WHERE created_at >= :start " +
                        "  AND created_at < :end " +
                        "GROUP BY period", nativeQuery = true)
        List<Map<String, Object>> getWeeklyRegistrationStatsBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end
        );

        @Query(value = "SELECT CONCAT(DATE_FORMAT(created_at, '%Y-%m'), '-W', (FLOOR((DAYOFMONTH(created_at) - 1) / 7) + 1)) as period, " +
                        "COUNT(*) as count " +
                        "FROM vendor_profiles " +
                        "WHERE created_at >= :start " +
                        "  AND created_at < :end " +
                        "GROUP BY period", nativeQuery = true)
        List<Map<String, Object>> getMonthWeekRegistrationStatsBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end
        );

        long countByUserIsActiveTrueAndUserIsBannedFalse();
}
