package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.document.KnowledgeDocument;
import fpt.project.NeoNHS.repository.mongo.KnowledgeRepository;
import fpt.project.NeoNHS.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;

    @Override
    public KnowledgeDocument createDocument(String title, String content) {
        KnowledgeDocument doc = KnowledgeDocument.builder()
                .title(title)
                .content(content)
                .build();
        return knowledgeRepository.save(doc);
    }

    @Override
    public KnowledgeDocument updateDocument(String id, String title, String content) {
        KnowledgeDocument doc = getDocument(id);
        doc.setTitle(title);
        doc.setContent(content);
        doc.setUpdatedAt(LocalDateTime.now());
        return knowledgeRepository.save(doc);
    }

    @Override
    public void deleteDocument(String id) {
        knowledgeRepository.deleteById(id);
    }

    @Override
    public void toggleVisibility(String id, boolean isActive) {
        KnowledgeDocument doc = getDocument(id);
        doc.setActive(isActive);
        doc.setUpdatedAt(LocalDateTime.now());
        knowledgeRepository.save(doc);
    }

    @Override
    public Page<KnowledgeDocument> getDocuments(String knowledgeType, Pageable pageable) {
        if (knowledgeType != null && !knowledgeType.isEmpty()) {
            return knowledgeRepository.findByKnowledgeType(knowledgeType, pageable);
        }
        return knowledgeRepository.findAll(pageable); // Admin sees all
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

            return createDocument(title, content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process file: " + e.getMessage());
        }
    }
}
