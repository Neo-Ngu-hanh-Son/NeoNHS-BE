package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fpt.project.NeoNHS.config.OpenAiConfig;
import fpt.project.NeoNHS.service.LanguageDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Cross-lingual RAG helper:
 *
 * <p>Uses a single GPT-4o-mini call to both detect the language and produce
 * a Vietnamese translation of the user query in one round-trip.
 * Results are cached in Redis to avoid redundant API calls for identical
 * (or very similar) queries (e.g. the same tourist asking the same question).
 *
 * <p>Cost note: GPT-4o-mini is ~30x cheaper than GPT-4o. A typical query is
 * &lt;50 tokens → the detect+translate step costs &lt;$0.00003 per call.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LanguageDetectionServiceImpl implements LanguageDetectionService {

    // Use the same OpenAI RestClient already configured in the application
    private final RestClient openAiRestClient;
    private final OpenAiConfig openAiConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /** Hard-coded cheap model – never use the main (expensive) chat model here. */
    private static final String MINI_MODEL = "gpt-4o-mini";

    /** Redis TTL for cached translations (24 h). Tourists repeat common questions. */
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    // ─── Public API ───────────────────────────────────────────────────────────

    @Override
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) return "vi";

        // Check Redis cache first
        String langCacheKey = "lang_detect:" + text.hashCode();
        Object cached = redisTemplate.opsForValue().get(langCacheKey);
        if (cached != null) {
            log.debug("[LangDetect] Cache HIT (lang): {}", cached);
            return cached.toString();
        }

        DetectAndTranslateResult result = callMiniModel(text);
        String lang = result.lang();

        redisTemplate.opsForValue().set(langCacheKey, lang, CACHE_TTL);
        // Persist translation cache while we're at it
        if (result.translatedVi() != null && !result.translatedVi().isBlank()) {
            String transCacheKey = "query_vi:" + text.hashCode();
            redisTemplate.opsForValue().set(transCacheKey, result.translatedVi(), CACHE_TTL);
        }

        return lang;
    }

    @Override
    public String translateToVietnamese(String text, String detectedLang) {
        if (text == null || text.isBlank()) return text;

        // Already Vietnamese → skip
        if ("vi".equals(detectedLang)) return text;

        // Check translation cache first (populated by detectLanguage if called first)
        String transCacheKey = "query_vi:" + text.hashCode();
        Object cached = redisTemplate.opsForValue().get(transCacheKey);
        if (cached != null) {
            log.debug("[LangDetect] Cache HIT (translation): {}", cached);
            return cached.toString();
        }

        // Fallback: call mini model again (e.g. if detectLanguage was not called)
        DetectAndTranslateResult result = callMiniModel(text);
        String translated = (result.translatedVi() != null && !result.translatedVi().isBlank())
                ? result.translatedVi()
                : text; // safeguard: return original if translation fails

        redisTemplate.opsForValue().set(transCacheKey, translated, CACHE_TTL);
        return translated;
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Single GPT-4o-mini call that performs BOTH detection and translation.
     * The model is instructed to return a tiny JSON object so parsing is cheap.
     *
     * <pre>
     * Prompt example output:
     * { "lang": "ja", "vi": "入場料はいくらですか を Vietnamese に翻訳した: vé vào cổng giá bao nhiêu?" }
     * → { "lang": "ja", "vi": "vé vào cổng giá bao nhiêu?" }
     * </pre>
     */
    private DetectAndTranslateResult callMiniModel(String userText) {
        // Build a minimal chat completion request targeting gpt-4o-mini
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", MINI_MODEL);
        requestBody.put("temperature", 0.0); // deterministic for detection tasks
        requestBody.put("max_tokens", 120);  // JSON is tiny

        ArrayNode messages = objectMapper.createArrayNode();

        // System instruction
        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "You are a language detection and translation microservice. "
                + "Given a user message, respond with ONLY a valid JSON object (no markdown, no explanation) "
                + "in the following format:\n"
                + "{\"lang\": \"<ISO 639-1 code>\", \"vi\": \"<Vietnamese translation of the message>\"}\n"
                + "Rules:\n"
                + "- 'lang' must be an ISO 639-1 two-letter code (e.g. 'en', 'ja', 'ko', 'zh', 'fr', 'vi').\n"
                + "- If the message is already Vietnamese, set lang='vi' and vi=<original text unchanged>.\n"
                + "- 'vi' must be a natural, fluent Vietnamese translation suitable for a semantic search query. "
                + "Preserve the core meaning and key nouns (places, names, prices, tickets).\n"
                + "- Do NOT include any text outside the JSON object.");
        messages.add(systemMsg);

        // User message to detect/translate
        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userText);
        messages.add(userMsg);

        requestBody.set("messages", messages);

        try {
            String responseJson = openAiRestClient.post()
                    .uri(openAiConfig.getChatCompletionUrl())
                    .body(requestBody.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            String content = root.path("choices").path(0).path("message").path("content").asText("{}");

            // Strip potential markdown fences just in case
            content = content.strip();
            if (content.startsWith("```")) {
                content = content.replaceAll("(?s)```[a-z]*\\n?", "").replace("```", "").strip();
            }

            JsonNode parsed = objectMapper.readTree(content);
            String lang = parsed.path("lang").asText("vi");
            String vi   = parsed.path("vi").asText(userText);

            log.info("[LangDetect] Detected lang='{}' for query: '{}' → vi: '{}'", lang, userText, vi);
            return new DetectAndTranslateResult(lang, vi);

        } catch (Exception e) {
            log.warn("[LangDetect] GPT-4o-mini call failed, falling back to 'vi': {}", e.getMessage());
            // On any error: treat as Vietnamese so the rest of the pipeline works normally
            return new DetectAndTranslateResult("vi", userText);
        }
    }

    /** Immutable value object holding the detect+translate result. */
    private record DetectAndTranslateResult(String lang, String translatedVi) {}
}
