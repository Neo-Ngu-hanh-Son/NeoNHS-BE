package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Order;
import fpt.project.NeoNHS.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.UUID;

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

        @Query("SELECT SUM(o.finalAmount) FROM Order o JOIN o.transactions t " +
                        "WHERE t.status = fpt.project.NeoNHS.enums.TransactionStatus.SUCCESS")
        Double sumTotalRevenue();

        @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-%m') as period, SUM(o.final_amount) as amount " +
                        "FROM orders o " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE t.status = 'SUCCESS' " +
                        "GROUP BY period ORDER BY period DESC LIMIT :limit", nativeQuery = true)
        List<Map<String, Object>> getMonthlyRevenueTrends(@Param("limit") Integer limit);

        @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-%m') as period, " +
                        "SUM(o.final_amount) as amount, " +
                        "COUNT(DISTINCT o.id) as transactionCount " +
                        "FROM orders o " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE t.status = 'SUCCESS' " +
                        "  AND o.created_at >= :start " +
                        "  AND o.created_at < :end " +
                        "GROUP BY period", nativeQuery = true)
        List<Map<String, Object>> getMonthlyRevenueTrendsBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end
        );

        @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-Week %u') as period, SUM(o.final_amount) as amount " +
                        "FROM orders o " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE t.status = 'SUCCESS' " +
                        "GROUP BY period ORDER BY period DESC LIMIT :limit", nativeQuery = true)
        List<Map<String, Object>> getWeeklyRevenueTrends(@Param("limit") Integer limit);

        @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-Week %u') as period, " +
                        "SUM(o.final_amount) as amount, " +
                        "COUNT(DISTINCT o.id) as transactionCount " +
                        "FROM orders o " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE t.status = 'SUCCESS' " +
                        "  AND o.created_at >= :start " +
                        "  AND o.created_at < :end " +
                        "GROUP BY period", nativeQuery = true)
        List<Map<String, Object>> getWeeklyRevenueTrendsBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end
        );

        @Query(value = "SELECT CONCAT(DATE_FORMAT(o.created_at, '%Y-%m'), '-W', (FLOOR((DAYOFMONTH(o.created_at) - 1) / 7) + 1)) as period, " +
                        "SUM(o.final_amount) as amount, " +
                        "COUNT(DISTINCT o.id) as transactionCount " +
                        "FROM orders o " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE t.status = 'SUCCESS' " +
                        "  AND o.created_at >= :start " +
                        "  AND o.created_at < :end " +
                        "GROUP BY period", nativeQuery = true)
        List<Map<String, Object>> getMonthWeekRevenueTrendsBetween(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end
        );

        @Query(value = "SELECT " +
                        "CASE WHEN od.ticket_catalog_id IS NOT NULL THEN 'EVENT' ELSE 'WORKSHOP' END as type, " +
                        "SUM(od.quantity) as totalQuantity, " +
                        "SUM(od.unit_price * od.quantity) as totalRevenue " +
                        "FROM order_details od " +
                        "JOIN orders o ON od.order_id = o.id " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE t.status = 'SUCCESS' " +
                        "GROUP BY type", nativeQuery = true)
        List<Map<String, Object>> getRevenueByTicketType();
}
