package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    @Query("SELECT COALESCE(v.businessName, 'Hệ thống Admin'), SUM(od.unitPrice * od.quantity) " +
            "FROM OrderDetail od " +
            "LEFT JOIN od.workshopSession ws " + // Dùng LEFT JOIN để không mất record Admin Event
            "LEFT JOIN ws.workshopTemplate wt " +
            "LEFT JOIN wt.vendor v " +
            "WHERE od.createdAt BETWEEN :start AND :end " +
            "GROUP BY v.businessName")
    List<Object[]> getRevenueByVendor(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
