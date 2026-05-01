package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Ticket;
import fpt.project.NeoNHS.enums.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Modifying
    @Query("UPDATE Ticket t SET t.status = :expiredStatus WHERE t.status = :activeStatus AND t.expiryDate < :now")
    int updateExpiredTickets(
            @Param("expiredStatus") TicketStatus expiredStatus,
            @Param("activeStatus") TicketStatus activeStatus,
            @Param("now") LocalDateTime now
    );
}
