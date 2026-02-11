package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BlogRepository extends JpaRepository<Blog, UUID>, JpaSpecificationExecutor<Blog> {
  long countByBlogCategoryId(UUID blogCategoryId);
}
