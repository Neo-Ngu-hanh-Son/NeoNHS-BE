package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Order;
import fpt.project.NeoNHS.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Tìm tất cả Orders liên quan đến một workshopSession cụ thể
     * và đã có Transaction SUCCESS (tức user đã thanh toán).
     * Dùng để hoàn tiền vào balance khi vendor cancel session.
     */
    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN o.orderDetails od
            JOIN o.transactions t
            WHERE od.workshopSession.id = :sessionId
              AND t.status = :txStatus
            """)
    List<Order> findOrdersByWorkshopSessionIdAndTxStatus(
            @Param("sessionId") UUID sessionId,
            @Param("txStatus") TransactionStatus txStatus);
}
