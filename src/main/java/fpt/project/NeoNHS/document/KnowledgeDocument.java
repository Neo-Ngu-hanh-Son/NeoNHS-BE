package fpt.project.NeoNHS.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "knowledge_base")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {

    @Id
    private String id;

    private String title;

    private String content; // Plain text or markdown

    // In a real production system, this would hold the embeddings float[]
    // private double[] contentVector;

    @org.springframework.data.mongodb.core.mapping.Field("isActive")
    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private String knowledgeType = "INFORMATION"; // INFORMATION, SYSTEM_PROMPT

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
