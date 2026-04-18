package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.project.NeoNHS.config.TranslationConfig;
import fpt.project.NeoNHS.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final TranslationConfig translationConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate translationRestTemplate; // @Bean name = "translationRestTemplate"

    @Value("${translation.cache-ttl:86400}")
    private long cacheTtlSeconds;

    // ── Public methods (không thay đổi so với trước) ──────────────

    @Override
    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank())
            return text;
        if ("vi".equals(targetLang))
            return text;

        String cacheKey = buildCacheKey(text, targetLang);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Translation cache HIT: {}", cacheKey);
            return cached.toString();
        }

        String translated = callNllbSingle(text, targetLang);
        redisTemplate.opsForValue().set(cacheKey, translated, Duration.ofSeconds(cacheTtlSeconds));
        return translated;
    }

    @Override
    public Map<String, String> translateFields(Map<String, String> fields, String targetLang) {
        if ("vi".equals(targetLang))
            return fields;

        Map<String, String> result = new HashMap<>();
        Map<String, String> toTranslate = new HashMap<>();

        // Tách cache hit và miss
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                result.put(entry.getKey(), entry.getValue());
                continue;
            }
            String cacheKey = buildCacheKey(entry.getValue(), targetLang);
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                result.put(entry.getKey(), cached.toString());
            } else {
                toTranslate.put(entry.getKey(), entry.getValue());
            }
        }

        // Batch translate những field chưa có cache — 1 lần gọi NLLB
        if (!toTranslate.isEmpty()) {
            Map<String, String> translated = callNllbBatch(toTranslate, targetLang);
            translated.forEach((key, value) -> {
                result.put(key, value);
                String originalText = toTranslate.get(key);
                if (originalText != null) {
                    String cacheKey = buildCacheKey(originalText, targetLang);
                    redisTemplate.opsForValue().set(cacheKey, value, Duration.ofSeconds(cacheTtlSeconds));
                }
            });
        }

        return result;
    }

    // ── Private: gọi NLLB-200 FastAPI ─────────────────────────────

    /**
     * Gọi POST /translate/batch trên NLLB FastAPI service.
     * Request format: { "fields": {...}, "targetLang": "ja" }
     * Response format: { "targetLang": "ja", "translations": {...} }
     */
    private Map<String, String> callNllbBatch(Map<String, String> fields, String targetLang) {
        String url = translationConfig.getEndpoint() + "/translate/batch";

        Map<String, Object> body = Map.of(
                "fields", fields,
                "targetLang", targetLang);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = translationRestTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode translationsNode = root.path("translations");

            Map<String, String> result = new HashMap<>();
            translationsNode.fields().forEachRemaining(e -> result.put(e.getKey(), e.getValue().asText()));

            log.debug("NLLB batch translated {} fields to {}", result.size(), targetLang);
            return result;

        } catch (Exception e) {
            log.error("NLLB batch translation failed for lang={}: {}", targetLang, e.getMessage());
            return fields; // fallback: trả về tiếng Việt gốc
        }
    }

    /**
     * Gọi POST /translate/text trên NLLB FastAPI service.
     * Request format: { "text": "...", "targetLang": "en" }
     * Response format: { "result": "..." }
     */
    private String callNllbSingle(String text, String targetLang) {
        String url = translationConfig.getEndpoint() + "/translate/text";

        Map<String, Object> body = Map.of(
                "text", text,
                "targetLang", targetLang);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = translationRestTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("result").asText(text); // fallback về text gốc nếu parse lỗi

        } catch (Exception e) {
            log.error("NLLB single translation failed for lang={}: {}", targetLang, e.getMessage());
            return text; // fallback: trả về tiếng Việt gốc
        }
    }

    // ── Helper ────────────────────────────────────────────────────

    private String buildCacheKey(String text, String lang) {
        return String.format("i18n:%s:%d", lang, text.hashCode());
    }
}
