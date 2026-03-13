package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.UserVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, UUID> {

    List<UserVoucher> findByUser_IdAndIsUsedFalse(UUID userId);

    List<UserVoucher> findByUser_IdAndVoucher_Id(UUID userId, UUID voucherId);

    Page<UserVoucher> findByUser_Id(UUID userId, Pageable pageable);

    Page<UserVoucher> findByUser_IdAndIsUsed(UUID userId, Boolean isUsed, Pageable pageable);

    Optional<UserVoucher> findByIdAndUser_Id(UUID id, UUID userId);

    long countByUser_IdAndVoucher_IdAndIsUsedTrue(UUID userId, UUID voucherId);

    boolean existsByUser_IdAndVoucher_Id(UUID userId, UUID voucherId);

    boolean existsByVoucher_IdAndIsUsedTrue(UUID voucherId);

    void deleteByVoucher_Id(UUID voucherId);
}
