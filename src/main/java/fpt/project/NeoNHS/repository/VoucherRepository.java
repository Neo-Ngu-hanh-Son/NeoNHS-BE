package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Voucher;
import fpt.project.NeoNHS.enums.VoucherScope;
import fpt.project.NeoNHS.enums.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, UUID>, JpaSpecificationExecutor<Voucher> {

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Voucher> findByIdAndDeletedAtIsNull(UUID id);

    // Tourist: active, available vouchers (PLATFORM scope)
    @Query("SELECT v FROM Voucher v WHERE v.scope = :scope " +
            "AND v.status = :status " +
            "AND v.deletedAt IS NULL " +
            "AND (v.startDate IS NULL OR v.startDate <= :now) " +
            "AND (v.endDate IS NULL OR v.endDate >= :now) " +
            "AND (v.usageLimit IS NULL OR v.usageCount < v.usageLimit)")
    Page<Voucher> findAvailableVouchers(
            @Param("scope") VoucherScope scope,
            @Param("status") VoucherStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    // Tourist: active, available vouchers by vendor
    @Query("SELECT v FROM Voucher v WHERE v.vendor.id = :vendorId " +
            "AND v.status = :status " +
            "AND v.deletedAt IS NULL " +
            "AND (v.startDate IS NULL OR v.startDate <= :now) " +
            "AND (v.endDate IS NULL OR v.endDate >= :now) " +
            "AND (v.usageLimit IS NULL OR v.usageCount < v.usageLimit)")
    Page<Voucher> findAvailableVouchersByVendor(
            @Param("vendorId") UUID vendorId,
            @Param("status") VoucherStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
