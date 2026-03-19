package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.WorkshopTemplate;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import fpt.project.NeoNHS.repository.projection.VendorCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface WorkshopTemplateRepository
        extends JpaRepository<WorkshopTemplate, UUID>, JpaSpecificationExecutor<WorkshopTemplate> {

    List<WorkshopTemplate> findByVendorId(UUID vendorId);

    Page<WorkshopTemplate> findByVendorId(UUID vendorId, Pageable pageable);

    List<WorkshopTemplate> findByVendorUserEmail(String email);

    long countByDeletedAtIsNull();

    @Query("""
        SELECT wt.vendor.id AS vendorId, COUNT(wt) AS count
        FROM WorkshopTemplate wt
        WHERE wt.deletedAt IS NULL
          AND wt.vendor.id IN :vendorIds
        GROUP BY wt.vendor.id
    """)
    List<VendorCountProjection> countTemplatesByVendorIds(@Param("vendorIds") List<UUID> vendorIds);

    @Query("""
        SELECT wt.vendor.id AS vendorId, COUNT(wt) AS count
        FROM WorkshopTemplate wt
        WHERE wt.deletedAt IS NULL
          AND wt.status = :status
          AND wt.vendor.id IN :vendorIds
        GROUP BY wt.vendor.id
    """)
    List<VendorCountProjection> countTemplatesByVendorIdsAndStatus(
            @Param("vendorIds") List<UUID> vendorIds,
            @Param("status") WorkshopStatus status
    );

    @Query(value = "SELECT wt.id as id, wt.name as name, SUM(od.quantity) as totalSales " +
            "FROM workshop_templates wt " +
            "JOIN workshop_sessions ws ON wt.id = ws.workshop_id " +
            "JOIN order_details od ON ws.id = od.workshop_session_id " +
            "JOIN orders o ON od.order_id = o.id " +
            "JOIN transactions t ON o.id = t.order_id " +
            "WHERE t.status = 'SUCCESS' " +
            "GROUP BY wt.id, wt.name " +
            "ORDER BY totalSales DESC LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> findTopWorkshopsBySales(@Param("limit") Integer limit);

    @Query("""
        SELECT w
        FROM WorkshopTemplate w
        JOIN FETCH w.vendor
        ORDER BY w.createdAt DESC
    """)
    List<WorkshopTemplate> findRecentCreated(Pageable pageable);

    @Query("""
        SELECT w
        FROM WorkshopTemplate w
        JOIN FETCH w.vendor
        WHERE w.reviewedAt IS NOT NULL
        ORDER BY w.reviewedAt DESC
    """)
    List<WorkshopTemplate> findRecentApproved(Pageable pageable);

    @Query("""
        SELECT wt
        FROM WorkshopTemplate wt
        INNER JOIN WorkshopSession ws
            ON wt.id = ws.workshopTemplate.id
        WHERE wt.status = 'ACTIVE'
          AND ws.status = 'SCHEDULED'
          AND ws.startTime >= NOW()
        ORDER BY ws.startTime ASC
""")
    List<WorkshopTemplate> findWorkshopTemplatesWithActiveUpcomingWorkshopSessions();

    long countByVendorIdAndDeletedAtIsNull(UUID vendorId);

    long countByVendorIdAndStatusAndDeletedAtIsNull(UUID vendorId, WorkshopStatus status);

    boolean existsByVendorIdAndName(UUID vendorId, String name);
}
