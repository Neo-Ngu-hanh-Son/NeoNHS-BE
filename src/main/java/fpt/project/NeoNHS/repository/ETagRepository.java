package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.ETag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ETagRepository extends JpaRepository<ETag, UUID> {
}
