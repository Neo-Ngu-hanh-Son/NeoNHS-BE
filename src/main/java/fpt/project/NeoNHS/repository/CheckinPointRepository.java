package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.CheckinPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CheckinPointRepository extends JpaRepository<CheckinPoint, UUID> {
}
