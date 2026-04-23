package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Voucher;
import fpt.project.NeoNHS.enums.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, UUID>, JpaSpecificationExecutor<Voucher> {

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Voucher> findByIdAndDeletedAtIsNull(UUID id);

    long countByVendorIdAndDeletedAtIsNull(UUID vendorId);

    long countByVendorIdAndDeletedAtIsNullAndCreatedAtAfter(UUID vendorId, LocalDateTime since);

    java.util.List<Voucher> findAllByStatusAndDeletedAtIsNull(VoucherStatus status);
}
