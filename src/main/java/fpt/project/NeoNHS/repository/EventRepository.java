package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Event;
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
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {
  long countByDeletedAtIsNull();

  @Query(value = "SELECT CAST(e.id AS CHAR) as id, e.name as name, SUM(od.quantity) as totalSales " +
          "FROM events e " +
          "JOIN ticket_catalogs tc ON e.id = tc.event_id " +
          "JOIN order_details od ON tc.id = od.ticket_catalog_id " +
          "JOIN orders o ON od.order_id = o.id " +
          "JOIN transactions t ON o.id = t.order_id " +
          "WHERE t.status = 'SUCCESS' " +
          "GROUP BY e.id, e.name ORDER BY totalSales DESC LIMIT :limit", nativeQuery = true)
  List<Map<String, Object>> findTopEventsBySales(@Param("limit") Integer limit);

  @Query("""
        SELECT e
        FROM Event e
        ORDER BY e.createdAt DESC
    """)
  List<Event> findRecentCreated(Pageable pageable);
}
