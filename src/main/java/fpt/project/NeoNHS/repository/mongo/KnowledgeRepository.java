package fpt.project.NeoNHS.repository.mongo;

import fpt.project.NeoNHS.document.KnowledgeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeRepository extends MongoRepository<KnowledgeDocument, String> {
    Page<KnowledgeDocument> findByIsActiveOrderByCreatedAtDesc(boolean isActive, Pageable pageable);

    // Fallback simple search via regex if vector search is not configured
    @Query("{ 'isActive': true, $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } } ] }")
    List<KnowledgeDocument> searchByKeyword(String keyword);

    List<KnowledgeDocument> findByIsActiveTrue();
}
