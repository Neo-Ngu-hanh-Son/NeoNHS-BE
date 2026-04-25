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

    // Improved search using MongoDB Text Index
    @Query("{ 'isActive': true, 'knowledgeType': { $ne: 'SYSTEM_PROMPT' }, $text: { $search: ?0 } }")
    List<KnowledgeDocument> searchByText(String text);

    // Fallback simple search via regex if text search is not preferred or for
    // simple keywords
    @Query("{ 'isActive': true, 'knowledgeType': { $ne: 'SYSTEM_PROMPT' }, $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } } ] }")
    List<KnowledgeDocument> searchByKeyword(String keyword);

    List<KnowledgeDocument> findByIsActiveTrue();

    List<KnowledgeDocument> findByKnowledgeType(String knowledgeType);

    Page<KnowledgeDocument> findByKnowledgeType(String knowledgeType, Pageable pageable);
}
