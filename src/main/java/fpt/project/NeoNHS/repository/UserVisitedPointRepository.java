package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.UserVisitedPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserVisitedPointRepository extends JpaRepository<UserVisitedPoint, UUID> {
}
