package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface BlogCategoryRepository
        extends JpaRepository<BlogCategory, UUID>, JpaSpecificationExecutor<BlogCategory> {
    boolean existsBySlugAndIdNot(String slug, UUID id);

    boolean existsBySlug(String slug);

    boolean existsByNameContaining(String name);

    boolean existsByNameContainingAndDeletedAtIsNull(String name);
}
