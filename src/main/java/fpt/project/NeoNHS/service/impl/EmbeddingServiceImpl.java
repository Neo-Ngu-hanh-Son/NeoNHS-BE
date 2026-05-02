package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fpt.project.NeoNHS.config.OpenAiConfig;
import fpt.project.NeoNHS.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of EmbeddingService using OpenAI's text-embedding-3-small model.
 * Supports both single and batch embedding generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    private final OpenAiConfig openAiConfig;
    private final RestClient openAiRestClient;
    private final ObjectMapper objectMapper; // Injected Spring bean — DO NOT use new ObjectMapper()

    @Override
    public List<Double> getEmbedding(String text) {
        if (text == null || text.isBlank())
            return Collections.emptyList();

        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", openAiConfig.getEmbeddingModel());
        request.put("input", text.replace("\n", " "));

        try {
            String responseJson = openAiRestClient.post()
                    .uri(openAiConfig.getEmbeddingsUrl())
                    .body(request.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode response = objectMapper.readTree(responseJson);
            JsonNode embeddingNode = response.path("data").path(0).path("embedding");

            List<Double> vector = new ArrayList<>();
            for (JsonNode val : embeddingNode) {
                vector.add(val.asDouble());
            }
            return vector;
        } catch (Exception e) {
            log.error("[Embedding] Failed to get embedding: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<List<Double>> getEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty())
            return Collections.emptyList();

        // OpenAI embedding API supports batch input natively
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", openAiConfig.getEmbeddingModel());

        ArrayNode inputArray = objectMapper.createArrayNode();
        for (String text : texts) {
            inputArray.add(text != null ? text.replace("\n", " ") : "");
        }
        request.set("input", inputArray);

        try {
            String responseJson = openAiRestClient.post()
                    .uri(openAiConfig.getEmbeddingsUrl())
                    .body(request.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode response = objectMapper.readTree(responseJson);
            JsonNode dataArray = response.path("data");

            List<List<Double>> results = new ArrayList<>();
            for (JsonNode item : dataArray) {
                JsonNode embeddingNode = item.path("embedding");
                List<Double> vector = new ArrayList<>();
                for (JsonNode val : embeddingNode) {
                    vector.add(val.asDouble());
                }
                results.add(vector);
            }
            return results;
        } catch (Exception e) {
            log.error("[Embedding] Failed to get batch embeddings: {}", e.getMessage());
            // Fallback: try one by one
            List<List<Double>> results = new ArrayList<>();
            for (String text : texts) {
                results.add(getEmbedding(text));
            }
            return results;
        }
    }
}
