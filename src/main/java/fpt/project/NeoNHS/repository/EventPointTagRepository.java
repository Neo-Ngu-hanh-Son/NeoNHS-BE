package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.EventPointTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventPointTagRepository extends JpaRepository<EventPointTag, UUID>, JpaSpecificationExecutor<EventPointTag> {
}
