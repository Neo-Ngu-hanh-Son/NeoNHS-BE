package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, UUID> {
    List<UserVoucher> findByUser_IdAndIsUsedFalse(UUID userId);

    List<UserVoucher> findByUser_IdAndVoucher_Id(UUID userId, UUID voucherId);
}
