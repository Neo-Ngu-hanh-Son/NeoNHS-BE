package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.TicketCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketCatalogRepository extends JpaRepository<TicketCatalog, UUID> {

    List<TicketCatalog> findByEventId(UUID eventId);

    List<TicketCatalog> findByEventIdAndDeletedAtIsNull(UUID eventId);

    Optional<TicketCatalog> findByEventIdAndId(UUID eventId, UUID id);
}
