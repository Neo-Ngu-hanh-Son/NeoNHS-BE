package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.document.KnowledgeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KnowledgeService {
    KnowledgeDocument createDocument(String title, String content);

    KnowledgeDocument updateDocument(String id, String title, String content);

    void deleteDocument(String id);

    void toggleVisibility(String id, boolean isActive);

    Page<KnowledgeDocument> getDocuments(String knowledgeType, Pageable pageable);

    KnowledgeDocument getDocument(String id);

    KnowledgeDocument uploadDocument(org.springframework.web.multipart.MultipartFile file);
}
