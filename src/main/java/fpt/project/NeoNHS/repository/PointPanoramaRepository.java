package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.entity.PointPanorama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PointPanoramaRepository extends JpaRepository<PointPanorama, UUID> {
    List<PointPanorama> findByPointId(UUID pointId);

    Optional<PointPanorama> findByIsDefaultAndPoint_Id(Boolean isDefault, UUID pointId);

    @Query("""
                SELECT DISTINCT p
                FROM Point p
                LEFT JOIN FETCH p.panoramas
                WHERE SIZE(p.panoramas) > 0
            """)
    List<Point> getAllPanoramaForLinking();
}
