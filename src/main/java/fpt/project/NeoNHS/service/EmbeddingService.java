package fpt.project.NeoNHS.service;

import java.util.List;

/**
 * Service for generating text embeddings using OpenAI's embedding models.
 * Centralizes embedding logic to avoid duplication across services.
 */
public interface EmbeddingService {

    /**
     * Generate an embedding vector for a single text input.
     *
     * @param text the text to embed
     * @return a list of doubles representing the embedding vector, or empty list on failure
     */
    List<Double> getEmbedding(String text);

    /**
     * Generate embedding vectors for multiple text inputs in a single API call.
     * More efficient than calling getEmbedding() in a loop.
     *
     * @param texts the list of texts to embed
     * @return a list of embedding vectors (one per input text)
     */
    List<List<Double>> getEmbeddings(List<String> texts);
}
