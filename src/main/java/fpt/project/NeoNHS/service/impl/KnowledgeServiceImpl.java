package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.document.KnowledgeDocument;
import fpt.project.NeoNHS.repository.mongo.KnowledgeRepository;
import fpt.project.NeoNHS.repository.mongo.VectorSearchRepository;
import fpt.project.NeoNHS.service.EmbeddingService;
import fpt.project.NeoNHS.service.KnowledgeService;
import fpt.project.NeoNHS.service.TextChunkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;
    private final EmbeddingService embeddingService;
    private final TextChunkingService textChunkingService;
    private final VectorSearchRepository vectorSearchRepository;

    @Override
    public KnowledgeDocument createDocument(String title, String content, String knowledgeType) {
        KnowledgeDocument parent = KnowledgeDocument.builder()
                .title(title)
                .content(content)
                .build();
        parent = knowledgeRepository.save(parent);

        // SYSTEM_PROMPT docs are not vectorized — they're used as-is for AI behavior
        // if knowledgeType is SYSTEM_PROMPT, don't have it generate embeddings or chunks, just save the parent document
        if (!"SYSTEM_PROMPT".equals(knowledgeType)) {
            parent.setEmbedding(embeddingService.getEmbedding(title + " " + content));
            parent.setKnowledgeType(knowledgeType);
            parent = knowledgeRepository.save(parent);
            createChunksForDocument(parent);
        } else {
            parent.setKnowledgeType(knowledgeType);
            parent.setEmbedding(null);
            parent = knowledgeRepository.save(parent);
            log.info("[Knowledge] Created SYSTEM_PROMPT document '{}'", title);
        }
        return parent;
    }

    @Override
    public KnowledgeDocument updateDocument(String id, String title, String content) {
        KnowledgeDocument doc = getDocument(id);

        doc.setTitle(title);
        doc.setContent(content);
        doc.setUpdatedAt(LocalDateTime.now());

        // SYSTEM_PROMPT docs are not vectorized — skip embedding and chunking
        if (!"SYSTEM_PROMPT".equals(doc.getKnowledgeType())) {
            knowledgeRepository.deleteByParentDocumentId(id);
            doc.setEmbedding(embeddingService.getEmbedding(title + " " + content));
            doc = knowledgeRepository.save(doc);
            createChunksForDocument(doc);
        } else {
            doc.setEmbedding(null);
            doc = knowledgeRepository.save(doc);
        }

        return doc;
    }

    @Override
    public void deleteDocument(String id) {
        // Delete all child chunks first
        knowledgeRepository.deleteByParentDocumentId(id);
        // Delete the parent document
        knowledgeRepository.deleteById(id);
    }

    @Override
    public void toggleVisibility(String id, boolean isActive) {
        KnowledgeDocument doc = getDocument(id);
        doc.setActive(isActive);
        doc.setUpdatedAt(LocalDateTime.now());
        knowledgeRepository.save(doc);

        // Also toggle visibility on chunks
        List<KnowledgeDocument> chunks = knowledgeRepository.findByParentDocumentId(id);
        for (KnowledgeDocument chunk : chunks) {
            chunk.setActive(isActive);
            chunk.setUpdatedAt(LocalDateTime.now());
        }
        if (!chunks.isEmpty()) {
            knowledgeRepository.saveAll(chunks);
        }
    }

    @Override
    public Page<KnowledgeDocument> getDocuments(String knowledgeType, Pageable pageable) {
        if (knowledgeType != null && !knowledgeType.isEmpty()) {
            return knowledgeRepository.findByKnowledgeType(knowledgeType, pageable);
        }
        // Show only parent documents (not chunks) for admin listing
        return knowledgeRepository.findByParentDocumentIdIsNull(pageable);
    }

    @Override
    public KnowledgeDocument getDocument(String id) {
        return knowledgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Knowledge document not found"));
    }

    @Override
    public KnowledgeDocument uploadDocument(org.springframework.web.multipart.MultipartFile file) {
        try {
            org.apache.tika.Tika tika = new org.apache.tika.Tika();
            String content = tika.parseToString(file.getInputStream());
            String title = file.getOriginalFilename();

            KnowledgeDocument doc = KnowledgeDocument.builder()
                    .title(title)
                    .content(content)
                    .sourceType("FILE_UPLOAD")
                    .embedding(embeddingService.getEmbedding(title + " " + content))
                    .build();
            doc = knowledgeRepository.save(doc);

            createChunksForDocument(doc);
            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process file: " + e.getMessage());
        }
    }

    @Override
    public List<KnowledgeDocument> searchSimilar(String query, int limit) {
        List<Double> queryVector = embeddingService.getEmbedding(query);
        if (queryVector == null || queryVector.isEmpty()) {
            return knowledgeRepository.searchByKeyword(query); // Fallback to keyword if embedding fails
        }
        return vectorSearchRepository.vectorSearch(queryVector, limit, 0.5); // Lower threshold for search testing
    }

    @Override
    public KnowledgeDocument syncBlogToKnowledge(String blogId, String title, String content) {
        // Remove existing sync for this blog (if re-syncing)
        removeBlogFromKnowledge(blogId);

        // Create a new knowledge document from the blog
        KnowledgeDocument doc = KnowledgeDocument.builder()
                .title(title)
                .content(content)
                .knowledgeType("BLOG")
                .sourceType("BLOG_SYNC")
                .sourceId(blogId)
                .embedding(embeddingService.getEmbedding(title + " " + content))
                .build();
        doc = knowledgeRepository.save(doc);

        createChunksForDocument(doc);
        log.info("[Knowledge] Blog '{}' synced to AI knowledge base (docId: {})", title, doc.getId());
        return doc;
    }

    @Override
    public void removeBlogFromKnowledge(String blogId) {
        // Delete all documents (parent + chunks) synced from this blog
        List<KnowledgeDocument> existing = knowledgeRepository.findBySourceTypeAndSourceId("BLOG_SYNC", blogId);
        for (KnowledgeDocument doc : existing) {
            knowledgeRepository.deleteByParentDocumentId(doc.getId());
        }
        knowledgeRepository.deleteBySourceTypeAndSourceId("BLOG_SYNC", blogId);
        if (!existing.isEmpty()) {
            log.info("[Knowledge] Removed blog {} from AI knowledge base", blogId);
        }
    }

    @Override
    public KnowledgeDocument reEmbedDocument(String id) {
        KnowledgeDocument doc = getDocument(id);

        // SYSTEM_PROMPT docs are not vectorized — nothing to re-embed
        if ("SYSTEM_PROMPT".equals(doc.getKnowledgeType())) {
            log.info("[Knowledge] Skipped re-embed for SYSTEM_PROMPT document '{}'", doc.getTitle());
            return doc;
        }

        // Delete old chunks
        knowledgeRepository.deleteByParentDocumentId(id);

        // Re-generate embedding for parent
        doc.setEmbedding(embeddingService.getEmbedding(doc.getTitle() + " " + doc.getContent()));
        doc.setUpdatedAt(LocalDateTime.now());
        doc = knowledgeRepository.save(doc);

        // Re-create chunks
        createChunksForDocument(doc);

        log.info("[Knowledge] Re-embedded document '{}' (id: {})", doc.getTitle(), doc.getId());
        return doc;
    }

    @Override
    public int reEmbedAll() {
        // Find all parent documents (not chunks, not system prompts)
        List<KnowledgeDocument> allParents = knowledgeRepository.findByIsActiveTrue().stream()
                .filter(doc -> doc.getParentDocumentId() == null)
                .filter(doc -> !"SYSTEM_PROMPT".equals(doc.getKnowledgeType()))
                .toList();

        int count = 0;
        for (KnowledgeDocument doc : allParents) {
            try {
                reEmbedDocument(doc.getId());
                count++;
            } catch (Exception e) {
                log.error("[Knowledge] Failed to re-embed document '{}': {}", doc.getTitle(), e.getMessage());
            }
        }

        log.info("[Knowledge] Re-embedded {} documents", count);
        return count;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Private Helpers
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Create text chunks for a parent document if the content is long enough.
     * Each chunk gets its own embedding for finer-grained vector search.
     */
    private void createChunksForDocument(KnowledgeDocument parent) {
        if (parent.getContent() == null || parent.getContent().isBlank()) {
            return;
        }

        List<String> chunks = textChunkingService.chunkText(parent.getContent());

        // Only create chunks if there's more than 1 (otherwise the parent embedding is sufficient)
        if (chunks.size() <= 1) {
            return;
        }

        // Generate embeddings for all chunks in batch (more efficient)
        List<String> chunkTexts = chunks.stream()
                .map(chunk -> parent.getTitle() + " " + chunk)
                .toList();
        List<List<Double>> embeddings = embeddingService.getEmbeddings(chunkTexts);

        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeDocument chunk = KnowledgeDocument.builder()
                    .title(parent.getTitle() + " [Chunk " + (i + 1) + "/" + chunks.size() + "]")
                    .content(chunks.get(i))
                    .parentDocumentId(parent.getId())
                    .chunkIndex(i)
                    .knowledgeType(parent.getKnowledgeType())
                    .sourceType(parent.getSourceType())
                    .sourceId(parent.getSourceId())
                    .isActive(parent.isActive())
                    .embedding(i < embeddings.size() ? embeddings.get(i) : embeddingService.getEmbedding(chunkTexts.get(i)))
                    .build();
            knowledgeRepository.save(chunk);
        }

        log.debug("[Knowledge] Created {} chunks for document '{}'", chunks.size(), parent.getTitle());
    }
}
