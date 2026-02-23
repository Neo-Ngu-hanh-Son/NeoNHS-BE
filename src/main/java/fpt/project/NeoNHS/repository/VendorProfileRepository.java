package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.VendorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, UUID> {

    Optional<VendorProfile> findByUserEmail(String email);

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
            Pageable pageable
    );
}
