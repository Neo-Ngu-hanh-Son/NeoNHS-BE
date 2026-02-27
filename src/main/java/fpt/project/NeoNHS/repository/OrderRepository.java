package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT SUM(o.finalAmount) FROM Order o JOIN o.transactions t " +
            "WHERE t.status = fpt.project.NeoNHS.enums.TransactionStatus.SUCCESS")
    Double sumTotalRevenue();

    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-%m') as period, SUM(o.final_amount) as amount " +
            "FROM orders o " +
            "JOIN transactions t ON o.id = t.order_id " +
            "WHERE t.status = 'SUCCESS' " +
            "GROUP BY period ORDER BY period DESC LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> getMonthlyRevenueTrends(@Param("limit") Integer limit);

    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-Week %u') as period, SUM(o.final_amount) as amount " +
            "FROM orders o " +
            "JOIN transactions t ON o.id = t.order_id " +
            "WHERE t.status = 'SUCCESS' " +
            "GROUP BY period ORDER BY period DESC LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> getWeeklyRevenueTrends(@Param("limit") Integer limit);

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