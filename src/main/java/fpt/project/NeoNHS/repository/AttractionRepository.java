package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Attraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, UUID> {

    /**
     * Find all active attractions with pagination
     */
    Page<Attraction> findByIsActiveTrue(Pageable pageable);

    /**
     * Find all active attractions with optional search by name
     */
    @Query("SELECT a FROM Attraction a WHERE a.isActive = true " +
           "AND (:keyword IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Attraction> findActiveAttractions(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find active attraction by ID
     */
    Optional<Attraction> findByIdAndIsActiveTrue(UUID id);

    /**
     * Count active attractions
     */
    long countByIsActiveTrue();
}

