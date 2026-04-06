package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.EventTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventTimelineRepository extends JpaRepository<EventTimeline, UUID>, JpaSpecificationExecutor<EventTimeline> {
    List<EventTimeline> findByEventId(UUID eventId);
}
