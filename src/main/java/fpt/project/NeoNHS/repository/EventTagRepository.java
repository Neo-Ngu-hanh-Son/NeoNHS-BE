package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.EventTag;
import fpt.project.NeoNHS.entity.EventTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventTagRepository extends JpaRepository<EventTag, EventTagId> {
}
