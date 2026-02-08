package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.WTag;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WTagRepository extends JpaRepository<WTag, UUID> {

    List<WTag> findAllByIdIn(List<UUID> ids);
}
