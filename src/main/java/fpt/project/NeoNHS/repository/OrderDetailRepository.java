package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {

        boolean existsByTicketCatalogId(UUID ticketCatalogId);

        boolean existsByTicketCatalog_EventId(UUID eventId);

        // Lấy danh sách giao dịch chi tiết cho bảng
        @Query("SELECT od FROM OrderDetail od " +
                        "JOIN FETCH od.order o " +
                        "LEFT JOIN FETCH od.workshopSession ws " +
                        "LEFT JOIN FETCH ws.workshopTemplate wt " +
                        "LEFT JOIN FETCH wt.vendor v " +
                        "WHERE od.createdAt BETWEEN :start AND :end " +
                        "ORDER BY od.createdAt DESC")
        List<OrderDetail> findRevenueDetails(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        // Thống kê doanh thu theo từng Vendor (cho biểu đồ thanh)
        @Query("SELECT COALESCE(v.businessName, 'Admin'), SUM(od.unitPrice * od.quantity) " +
                        "FROM OrderDetail od " +
                        "LEFT JOIN od.workshopSession ws " + // Dùng LEFT JOIN để không mất record Admin Event
                        "LEFT JOIN ws.workshopTemplate wt " +
                        "LEFT JOIN wt.vendor v " +
                        "WHERE od.createdAt BETWEEN :start AND :end " +
                        "GROUP BY v.businessName")
        List<Object[]> getRevenueByVendor(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        @Query(value = "SELECT COALESCE(SUM(od.unit_price * od.quantity), 0) " +
                        "FROM order_details od " +
                        "JOIN workshop_sessions ws ON od.workshop_session_id = ws.id " +
                        "JOIN workshop_templates wt ON ws.workshop_id = wt.id " +
                        "JOIN orders o ON od.order_id = o.id " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE wt.vendor_id = :vendorId " +
                        "AND t.status = 'SUCCESS' " +
                        "AND od.created_at >= :start " +
                        "AND od.created_at < :end", nativeQuery = true)
        java.math.BigDecimal sumRevenueByVendorIdBetween(
                        @Param("vendorId") UUID vendorId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query(value = "SELECT COALESCE(SUM(od.unit_price * od.quantity), 0) " +
                        "FROM order_details od " +
                        "JOIN workshop_sessions ws ON od.workshop_session_id = ws.id " +
                        "JOIN workshop_templates wt ON ws.workshop_id = wt.id " +
                        "JOIN orders o ON od.order_id = o.id " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE wt.vendor_id = :vendorId " +
                        "AND t.status = 'SUCCESS'", nativeQuery = true)
        java.math.BigDecimal sumTotalRevenueByVendorId(@Param("vendorId") UUID vendorId);

        @Query(value = "SELECT DATE(od.created_at) as day, COALESCE(SUM(od.unit_price * od.quantity), 0) as revenue " +
                        "FROM order_details od " +
                        "JOIN workshop_sessions ws ON od.workshop_session_id = ws.id " +
                        "JOIN workshop_templates wt ON ws.workshop_id = wt.id " +
                        "JOIN orders o ON od.order_id = o.id " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE wt.vendor_id = :vendorId " +
                        "AND t.status = 'SUCCESS' " +
                        "AND od.created_at >= :start " +
                        "AND od.created_at < :end " +
                        "GROUP BY DATE(od.created_at) " +
                        "ORDER BY day", nativeQuery = true)
        List<Object[]> getDailyRevenueByVendorId(
                        @Param("vendorId") UUID vendorId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query(value = "SELECT DATE_FORMAT(od.created_at, '%Y-%m') as month, COALESCE(SUM(od.unit_price * od.quantity), 0) as revenue "
                        +
                        "FROM order_details od " +
                        "JOIN workshop_sessions ws ON od.workshop_session_id = ws.id " +
                        "JOIN workshop_templates wt ON ws.workshop_id = wt.id " +
                        "JOIN orders o ON od.order_id = o.id " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE wt.vendor_id = :vendorId " +
                        "AND t.status = 'SUCCESS' " +
                        "AND od.created_at >= :start " +
                        "AND od.created_at < :end " +
                        "GROUP BY DATE_FORMAT(od.created_at, '%Y-%m') " +
                        "ORDER BY month", nativeQuery = true)
        List<Object[]> getMonthlyRevenueByVendorId(
                        @Param("vendorId") UUID vendorId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query(value = "SELECT COALESCE(SUM(od.unit_price * od.quantity), 0) " +
                        "FROM order_details od " +
                        "WHERE od.created_at BETWEEN :start AND :end", nativeQuery = true)
        BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        @Query(value = "SELECT DATE_FORMAT(od.created_at, '%Y-%m-%d') as day, SUM(od.unit_price * od.quantity) as revenue, COUNT(*) as count "
                        +
                        "FROM order_details od " +
                        "WHERE od.created_at BETWEEN :start AND :end " +
                        "GROUP BY day " +
                        "ORDER BY day", nativeQuery = true)
        List<Object[]> getGlobalDailyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        /**
         * Lấy tất cả OrderDetail của một WorkshopSession
         * mà order đó đã có Transaction SUCCESS (tiền đã về Admin).
         * Dùng cho giai đoạn 3: session COMPLETED → cộng netAmount vào vendor.balance
         */
        @Query("SELECT od FROM OrderDetail od " +
                        "JOIN FETCH od.order o " +
                        "JOIN o.transactions t " +
                        "WHERE od.workshopSession.id = :sessionId " +
                        "AND t.status = fpt.project.NeoNHS.enums.TransactionStatus.SUCCESS")
        List<OrderDetail> findPaidDetailsByWorkshopSessionId(@Param("sessionId") UUID sessionId);

        /**
         * Tính tổng netAmount cho vendor (tiền vendor thực nhận sau commission)
         * Chỉ tính từ workshop sessions (có netAmount)
         */
        @Query(value = "SELECT COALESCE(SUM(od.net_amount), 0) " +
                        "FROM order_details od " +
                        "JOIN workshop_sessions ws ON od.workshop_session_id = ws.id " +
                        "JOIN workshop_templates wt ON ws.workshop_id = wt.id " +
                        "JOIN orders o ON od.order_id = o.id " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE wt.vendor_id = :vendorId " +
                        "AND t.status = 'SUCCESS' " +
                        "AND od.net_amount IS NOT NULL", nativeQuery = true)
        java.math.BigDecimal sumTotalNetAmountByVendorId(@Param("vendorId") UUID vendorId);

        /**
         * Lấy daily netAmount (tiền vendor nhận) cho vendor
         */
        @Query(value = "SELECT DATE(od.created_at) as day, COALESCE(SUM(od.net_amount), 0) as netAmount " +
                        "FROM order_details od " +
                        "JOIN workshop_sessions ws ON od.workshop_session_id = ws.id " +
                        "JOIN workshop_templates wt ON ws.workshop_id = wt.id " +
                        "JOIN orders o ON od.order_id = o.id " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE wt.vendor_id = :vendorId " +
                        "AND t.status = 'SUCCESS' " +
                        "AND od.created_at >= :start " +
                        "AND od.created_at < :end " +
                        "AND od.net_amount IS NOT NULL " +
                        "GROUP BY DATE(od.created_at) " +
                        "ORDER BY day", nativeQuery = true)
        List<Object[]> getDailyNetAmountByVendorId(
                        @Param("vendorId") UUID vendorId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        /**
         * Lấy monthly netAmount (tiền vendor nhận) cho vendor
         */
        @Query(value = "SELECT DATE_FORMAT(od.created_at, '%Y-%m') as month, COALESCE(SUM(od.net_amount), 0) as netAmount "
                        +
                        "FROM order_details od " +
                        "JOIN workshop_sessions ws ON od.workshop_session_id = ws.id " +
                        "JOIN workshop_templates wt ON ws.workshop_id = wt.id " +
                        "JOIN orders o ON od.order_id = o.id " +
                        "JOIN transactions t ON o.id = t.order_id " +
                        "WHERE wt.vendor_id = :vendorId " +
                        "AND t.status = 'SUCCESS' " +
                        "AND od.created_at >= :start " +
                        "AND od.created_at < :end " +
                        "AND od.net_amount IS NOT NULL " +
                        "GROUP BY DATE_FORMAT(od.created_at, '%Y-%m') " +
                        "ORDER BY month", nativeQuery = true)
        List<Object[]> getMonthlyNetAmountByVendorId(
                        @Param("vendorId") UUID vendorId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);
}
