package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.document.KnowledgeDocument;
import fpt.project.NeoNHS.enums.KnowledgeTypeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface KnowledgeService {
    KnowledgeDocument createDocument(String title, String content, KnowledgeTypeStatus knowledgeType);

    KnowledgeDocument updateDocument(String id, String title, String content, KnowledgeTypeStatus knowledgeType);

    void deleteDocument(String id);

    void toggleVisibility(String id, boolean isActive);

    Page<KnowledgeDocument> getDocuments(KnowledgeTypeStatus knowledgeType, Pageable pageable);

    KnowledgeDocument getDocument(String id);

    KnowledgeDocument uploadDocument(org.springframework.web.multipart.MultipartFile file);

    List<KnowledgeDocument> searchSimilar(String query, int limit);


    KnowledgeDocument reEmbedDocument(String id);

    int reEmbedAll();
}
