package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.PanoramaHotSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PanoramaHotSpotRepository extends JpaRepository<PanoramaHotSpot, UUID> {

  List<PanoramaHotSpot> findByPointIdOrderByOrderIndexAsc(UUID pointId);

  List<PanoramaHotSpot> findByCheckinPointIdOrderByOrderIndexAsc(UUID checkinPointId);

  void deleteAllByPointId(UUID pointId);

  void deleteAllByCheckinPointId(UUID checkinPointId);
}
