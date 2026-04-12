package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.EventPointTag;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Repository
public interface EventPointTagRepository extends JpaRepository<EventPointTag, UUID>, JpaSpecificationExecutor<EventPointTag> {
    Optional<EventPointTag> findByName(String tagName);
}
