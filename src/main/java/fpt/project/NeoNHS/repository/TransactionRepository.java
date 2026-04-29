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

  List<Transaction> findByStatusAndTransactionDateBefore(fpt.project.NeoNHS.enums.TransactionStatus status, java.time.LocalDateTime dateTime);

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

  @Query(value = "SELECT t.id as transaction_id, " +
          "t.status as transaction_status, " +
          "wt.name as workshop_name, " +
          "u.fullname as customer_name, " +
          "(od.unit_price * od.quantity) as amount, " +
          "t.currency as currency, " +
          "t.transaction_date as paid_at, " +
          "(SELECT GROUP_CONCAT(tk.ticket_code) FROM tickets tk WHERE tk.order_detail_id = od.id) as ticket_codes " +
          "FROM transactions t " +
          "JOIN orders o ON t.order_id = o.id " +
          "JOIN users u ON o.user_id = u.id " +
          "JOIN order_details od ON o.id = od.order_id " +
          "JOIN workshop_sessions ws ON od.workshop_session_id = ws.id " +
          "JOIN workshop_templates wt ON ws.workshop_id = wt.id " +
          "WHERE wt.vendor_id = :vendorId AND t.status = 'SUCCESS' " +
          "ORDER BY t.created_at DESC", nativeQuery = true)
  List<Object[]> findRecentTransactionsWithTicketsByVendorIdNative(@Param("vendorId") UUID vendorId, Pageable pageable);
}
