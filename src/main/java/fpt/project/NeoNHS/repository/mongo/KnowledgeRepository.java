package fpt.project.NeoNHS.repository.mongo;

import fpt.project.NeoNHS.document.KnowledgeDocument;
import fpt.project.NeoNHS.enums.KnowledgeTypeStatus;
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

    List<KnowledgeDocument> findByKnowledgeType(KnowledgeTypeStatus knowledgeType);

    Page<KnowledgeDocument> findByKnowledgeType(KnowledgeTypeStatus knowledgeType, Pageable pageable);

    // Chunking support: find all chunks belonging to a parent document
    List<KnowledgeDocument> findByParentDocumentId(String parentDocumentId);

    // Chunking support: delete all chunks when parent is updated/deleted
    void deleteByParentDocumentId(String parentDocumentId);

    // Blog sync: find documents synced from a specific blog
    List<KnowledgeDocument> findBySourceTypeAndSourceId(String sourceType, String sourceId);

    // Blog sync: delete all docs (parent + chunks) synced from a specific blog
    void deleteBySourceTypeAndSourceId(String sourceType, String sourceId);

    // Find only parent documents (not chunks) for admin listing
    Page<KnowledgeDocument> findByParentDocumentIdIsNull(Pageable pageable);

    // Find only parent documents of a specific type (not chunks)
    Page<KnowledgeDocument> findByKnowledgeTypeAndParentDocumentIdIsNull(KnowledgeTypeStatus knowledgeType, Pageable pageable);

    // Find only parent documents excluding a specific type (not chunks)
    Page<KnowledgeDocument> findByKnowledgeTypeNotAndParentDocumentIdIsNull(KnowledgeTypeStatus knowledgeType, Pageable pageable);

}
