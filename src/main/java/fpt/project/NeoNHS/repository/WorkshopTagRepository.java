package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.WorkshopTag;
import fpt.project.NeoNHS.entity.WorkshopTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkshopTagRepository extends JpaRepository<WorkshopTag, WorkshopTagId> {
}
