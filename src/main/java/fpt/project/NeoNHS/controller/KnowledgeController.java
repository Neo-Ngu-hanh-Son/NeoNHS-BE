package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.document.KnowledgeDocument;
import fpt.project.NeoNHS.enums.KnowledgeTypeStatus;
import fpt.project.NeoNHS.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping
    public ResponseEntity<KnowledgeDocument> createDocument(@RequestBody Map<String, String> request) {
        KnowledgeTypeStatus type = KnowledgeTypeStatus.INFORMATION; // default
        if (request.get("knowledgeType") != null) {
            type = KnowledgeTypeStatus.valueOf(request.get("knowledgeType"));
        }
        return ResponseEntity.ok(knowledgeService.createDocument(request.get("title"), request.get("content"), type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeDocument> updateDocument(@PathVariable String id,
            @RequestBody Map<String, String> request) {
        KnowledgeTypeStatus type = null;
        if (request.get("knowledgeType") != null) {
            type = KnowledgeTypeStatus.valueOf(request.get("knowledgeType"));
        }
        return ResponseEntity.ok(knowledgeService.updateDocument(id, request.get("title"), request.get("content"), type));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        knowledgeService.deleteDocument(id);
        return ResponseEntity.ok().build();
    }

    public record VisibilityRequest(
            @com.fasterxml.jackson.annotation.JsonProperty("isActive") Boolean isActive) {
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Void> toggleVisibility(@PathVariable String id, @RequestBody VisibilityRequest request) {
        knowledgeService.toggleVisibility(id, request.isActive());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<KnowledgeDocument>> getDocuments(
            @RequestParam(required = false) KnowledgeTypeStatus knowledgeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                knowledgeService.getDocuments(knowledgeType,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeDocument> getDocument(@PathVariable String id) {
        return ResponseEntity.ok(knowledgeService.getDocument(id));
    }

    @PostMapping("/upload")
    public ResponseEntity<KnowledgeDocument> uploadDocument(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(knowledgeService.uploadDocument(file));
    }

    // ═══════════════════════════════════════════════════════════════════
    // New RAG Endpoints
    // ═══════════════════════════════════════════════════════════════════


    /**
     * Force re-generate embeddings and chunks for a single document.
     */
    @PostMapping("/{id}/re-embed")
    public ResponseEntity<KnowledgeDocument> reEmbed(@PathVariable String id) {
        return ResponseEntity.ok(knowledgeService.reEmbedDocument(id));
    }

    /**
     * Test vector search — admin tool to preview what the AI would find.
     */
    @GetMapping("/search")
    public ResponseEntity<List<KnowledgeDocument>> searchSimilar(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(knowledgeService.searchSimilar(query, limit));
    }

    /**
     * One-time migration: re-chunk and re-embed ALL existing documents.
     * Use this after upgrading to the new vector search system.
     */
    @PostMapping("/re-embed-all")
    public ResponseEntity<Map<String, Object>> reEmbedAll() {
        int count = knowledgeService.reEmbedAll();
        return ResponseEntity.ok(Map.of(
                "message", "Re-embedded " + count + " documents",
                "documentsProcessed", count));
    }
}
