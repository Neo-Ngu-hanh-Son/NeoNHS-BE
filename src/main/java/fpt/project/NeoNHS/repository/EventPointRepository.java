package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.EventPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventPointRepository extends JpaRepository<EventPoint, UUID>, JpaSpecificationExecutor<EventPoint> {
}
