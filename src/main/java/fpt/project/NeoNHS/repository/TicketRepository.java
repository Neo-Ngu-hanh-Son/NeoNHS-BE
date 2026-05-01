package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Ticket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByTicketCode(String ticketCode);

    @Query("""
        SELECT t
        FROM Ticket t
        LEFT JOIN FETCH t.workshopSession ws
        LEFT JOIN FETCH ws.workshopTemplate wt
        LEFT JOIN FETCH wt.vendor v
        LEFT JOIN FETCH t.ticketCatalog tc
        LEFT JOIN FETCH tc.event e
        ORDER BY t.createdAt DESC
    """)
    List<Ticket> findRecentSold(Pageable pageable);

    @Query("""
        SELECT COUNT(t) > 0 FROM Ticket t
        JOIN t.orderDetail od
        JOIN od.order o
        JOIN t.workshopSession ws
        WHERE o.user.id = :userId
        AND ws.workshopTemplate.id = :workshopId
        AND t.status = 'USED'
    """)
    boolean hasUserUsedTicketForWorkshop(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("workshopId") UUID workshopId);
}
