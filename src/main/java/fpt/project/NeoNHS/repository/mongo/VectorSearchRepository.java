package fpt.project.NeoNHS.repository.mongo;

import fpt.project.NeoNHS.document.KnowledgeDocument;
import fpt.project.NeoNHS.enums.KnowledgeTypeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Custom repository for MongoDB Atlas Vector Search using the $vectorSearch aggregation stage.
 * Replaces the in-memory cosine similarity calculation with a native MongoDB vector index query.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class VectorSearchRepository {

    private final MongoTemplate mongoTemplate;

    @Value("${vector-search.index-name:vector_index}")
    private String indexName;

    @Value("${vector-search.min-similarity-score:0.75}")
    private double defaultMinScore;

    @Value("${vector-search.max-results:3}")
    private int defaultMaxResults;

    /**
     * Perform a vector search using MongoDB Atlas $vectorSearch aggregation.
     *
     * @param queryVector the embedding vector of the user's query
     * @param limit       maximum number of results to return
     * @param minScore    minimum similarity score threshold (0.0 to 1.0)
     * @return list of KnowledgeDocuments sorted by relevance
     */
    public List<KnowledgeDocument> vectorSearch(List<Double> queryVector, int limit, double minScore) {
        if (queryVector == null || queryVector.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // Build the $vectorSearch aggregation stage
            Document vectorSearchStage = new Document("$vectorSearch", new Document()
                    .append("index", indexName)
                    .append("path", "embedding")
                    .append("queryVector", queryVector)
                    .append("numCandidates", limit * 10) // Search wider for better quality
                    .append("limit", limit)
                    .append("filter", new Document()
                            .append("isActive", true)
                            .append("knowledgeType", new Document("$ne", KnowledgeTypeStatus.SYSTEM_PROMPT.name()))
                    )
            );

            // Add score field
            Document addFieldsStage = new Document("$addFields", new Document()
                    .append("searchScore", new Document("$meta", "vectorSearchScore"))
            );

            // Filter by minimum score
            Document matchStage = new Document("$match", new Document()
                    .append("searchScore", new Document("$gte", minScore))
            );

            AggregationOperation vectorSearchOp = context -> vectorSearchStage;
            AggregationOperation addScoreOp = context -> addFieldsStage;
            AggregationOperation matchScoreOp = context -> matchStage;

            Aggregation aggregation = Aggregation.newAggregation(vectorSearchOp, addScoreOp, matchScoreOp);

            AggregationResults<KnowledgeDocument> results = mongoTemplate.aggregate(
                    aggregation, "knowledge_base", KnowledgeDocument.class);

            return results.getMappedResults();
        } catch (Exception e) {
            log.error("[VectorSearch] Atlas Vector Search failed: {}. Ensure the '{}' index exists on the knowledge_base collection.",
                    e.getMessage(), indexName);
            return Collections.emptyList();
        }
    }

    /**
     * Perform a vector search with default parameters from configuration.
     */
    public List<KnowledgeDocument> vectorSearch(List<Double> queryVector) {
        return vectorSearch(queryVector, defaultMaxResults, defaultMinScore);
    }
}
