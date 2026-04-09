package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
  List<Transaction> findAll(Specification<Transaction> spec, Sort sort);

  org.springframework.data.domain.Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

  Optional<Transaction> findByPaymentGateway(String paymentGateway);

  @Query("""
          SELECT t FROM Transaction t
          JOIN FETCH t.order o
          JOIN FETCH o.user u
          JOIN o.orderDetails od
          JOIN od.workshopSession ws
          JOIN ws.workshopTemplate wt
          WHERE wt.vendor.id = :vendorId
            AND t.status = fpt.project.NeoNHS.enums.TransactionStatus.SUCCESS
          ORDER BY t.transactionDate DESC
      """)
  List<Transaction> findRecentByVendorId(@Param("vendorId") UUID vendorId, Pageable pageable);
}
