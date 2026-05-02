package fpt.project.NeoNHS.document;

import fpt.project.NeoNHS.enums.KnowledgeTypeStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "knowledge_base")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {

    @Id
    private String id;

    @org.springframework.data.mongodb.core.index.TextIndexed(weight = 2)
    private String title;

    @org.springframework.data.mongodb.core.index.TextIndexed
    private String content; // Plain text or markdown

    // Holds the vector embedding for vector search
    private List<Double> embedding;

    // Chunking support: null for parent docs, set for chunks
    private String parentDocumentId;

    // Order of chunk within parent document (null for parent docs)
    private Integer chunkIndex;

    // Source tracking: MANUAL, BLOG_SYNC, FILE_UPLOAD
    @Builder.Default
    private String sourceType = "MANUAL";

    // Reference ID (e.g., Blog UUID if synced from blog)
    private String sourceId;

    @org.springframework.data.mongodb.core.mapping.Field("isActive")
    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private KnowledgeTypeStatus knowledgeType = KnowledgeTypeStatus.INFORMATION;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
