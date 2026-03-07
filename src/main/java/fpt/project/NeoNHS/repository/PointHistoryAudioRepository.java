package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.PointHistoryAudio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PointHistoryAudioRepository extends JpaRepository<PointHistoryAudio, UUID> {

    List<PointHistoryAudio> findByPointId(UUID pointId);

    Optional<PointHistoryAudio> findByIdAndDeletedAtIsNull(UUID id);

    // Find multiple PointHistoryAudio by pointId where deletedAt is null
    List<PointHistoryAudio> findByPointIdAndDeletedAtIsNull(UUID id);

}
