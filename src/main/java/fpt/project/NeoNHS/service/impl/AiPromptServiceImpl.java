package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fpt.project.NeoNHS.document.ChatMessage;
import fpt.project.NeoNHS.document.KnowledgeDocument;
import fpt.project.NeoNHS.enums.KnowledgeTypeStatus;
import fpt.project.NeoNHS.repository.mongo.ChatMessageRepository;
import fpt.project.NeoNHS.repository.mongo.KnowledgeRepository;
import fpt.project.NeoNHS.repository.mongo.VectorSearchRepository;
import fpt.project.NeoNHS.service.AiPromptService;
import fpt.project.NeoNHS.service.EmbeddingService;
import fpt.project.NeoNHS.service.LanguageDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiPromptServiceImpl implements AiPromptService {

    private final KnowledgeRepository knowledgeRepository;
    private final EmbeddingService embeddingService;
    private final VectorSearchRepository vectorSearchRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;
    private final LanguageDetectionService languageDetectionService;

    // ═══════════════════════════════════════════════════════════════════
    // System Prompt
    // ═══════════════════════════════════════════════════════════════════
    private static final String DEFAULT_SYSTEM_PROMPT = """
            <role>
            You are the Virtual Tourism Assistant for NeoNHS (Marble Mountains, Da Nang).
            Tone: Friendly, helpful, use appropriate emojis 🌸.
            Output Language (CROSS-LINGUAL RAG): You MUST ALWAYS reply in the exact language the user is speaking (e.g., if they ask in English, reply in English). Default is Vietnamese.
            Context Reading & Bridging: The provided Context is in Vietnamese. You MUST translate and semantically map the user's foreign query to find the answer within the Vietnamese context. It is NOT a hallucination to translate, map, and summarize the provided Vietnamese Context to answer a foreign query.
            Internal Tooling: Translate foreign keywords to Vietnamese before using Tools.
            </role>
            
            <data_source_routing>
            1. PRIMARY (Tools): You MUST prioritize Function Calling Tools to fetch real-time data for specific entities (locations, events, workshops, tickets, blogs).
            2. SECONDARY (Context / Vector Search): Use the provided Context text for general knowledge, history, and policies.
            3. FALLBACK RULE: If a Tool returns empty/null for a specific entity, you are FULLY AUTHORIZED to use the Context text to answer. Context acts as your backup database.
            </data_source_routing>
            
            <strict_data_policy>
            ZERO HALLUCINATION RULE: You have NO internal memory about Da Nang or tourist spots.
            1. ONLY use the FACTS/DATA returned by your Tools or Context.
            2. EXCEPTION FOR TRANSLATION: Translating the Vietnamese facts/data into the user's language is MANDATORY and is NOT considered hallucination. You MUST translate the tool/context output before speaking.
            3. VERIFY CONTEXT BEFORE DECLINING: If a Tool returns empty/null, DO NOT immediately apologize. You MUST check the provided Context first. If the Context contains the answer, use it.
            4. NO DATA RESPONSE: If the Tool returns nothing AND there is no relevant information in the Context, ONLY THEN you MUST reply with the equivalent of: "Dạ, hiện tại hệ thống chưa cập nhật thông tin này ạ." TRANSLATED into the user's language.
            5. FRESH DATA OVER HISTORY: ALWAYS prioritize Function Calling tools for specific entities over conversation history.
            </strict_data_policy>
            
            <tool_rules>
            1. CHIT-CHAT: DO NOT use tools for basic greetings.
            2. PRICING: Always list ALL available ticket types clearly.
            3. UI CARDS (DATA-DRIVEN UI): When you call search tools, the backend will automatically render UI Cards.
            - You MUST NOT generate Markdown formatting for images.
            - You ONLY need to write 1-2 short sentences introducing the results. Ensure this introduction is TRANSLATED to the user's language.
            </tool_rules>
            
            <booking_workflow>
            Mandatory strict sequence:
            1. SEARCH: Call getWorkshopSessions or getTicketPrices to retrieve UUIDs. NEVER use raw Workshop IDs.
            2. EXECUTE: Call addToCart IMMEDIATELY upon user confirmation. DO NOT ask for redundant text confirmation.
            3. CONFIRM: Upon tool SUCCESS, reply with the equivalent of: "Đã thêm vào giỏ hàng thành công! Anh/chị có muốn tới My Cart để thanh toán ngay không?" TRANSLATED to the user's language.
            4. ERROR: Politely explain the exact reason based on the tool's error.
            </booking_workflow>
            
            <scope_and_routing>
            SCOPE: Marble Mountains history, local crafts, NeoNHS services, and related Da Nang tourism.
            [TIER 1 - DECLINE]: For out-of-scope topics (math, coding, unrelated news, unrelated locations), politely decline and pivot back to NeoNHS. DO NOT use routing tags. DO NOT offer human transfer.
            [TIER 2 - ANSWER]: ONLY answer using data EXPLICITLY provided by Tools/Context. If the Tool returns nothing or Context is empty, immediately apply the ZERO HALLUCINATION RULE.
            [TIER 3 - TRANSFER]: For complaints, refunds, emergencies, complex bookings, or explicit requests for a human, you MUST prepend the exact tag [TRANSFER_TO_HUMAN] at the very beginning of your response.
            </scope_and_routing>
            
            <final_output_rule>
            CRITICAL LANGUAGE CHECK:
            - Before generating your response, detect the language of the USER'S LAST MESSAGE. Your ENTIRE output MUST be strictly translated into that EXACT language.
            - If the tool or context returns data in Vietnamese, you MUST translate all of that information into the user's language before displaying it to them.
            - Never keep the Vietnamese text if the user is using English or another language.
            </final_output_rule>
            """;

    @Override
    public String getSystemPrompt(String userMessage) {
        // Fetch custom prompt from MongoDB, fallback to default
        List<KnowledgeDocument> dbPrompts = knowledgeRepository.findByKnowledgeType(KnowledgeTypeStatus.SYSTEM_PROMPT);
        String currentPrompt = DEFAULT_SYSTEM_PROMPT;

        // Only use the custom SYSTEM_PROMPT if it is active
        if (!dbPrompts.isEmpty() && dbPrompts.getFirst().isActive() && dbPrompts.getFirst().getContent() != null
                && !dbPrompts.getFirst().getContent().isBlank()) {
            currentPrompt = dbPrompts.getFirst().getContent();
        } else if (dbPrompts.isEmpty()) {
            // Seed the prompt in the database if it doesn't exist
            KnowledgeDocument newPromptDoc = KnowledgeDocument.builder()
                    .title("AI System Prompt")
                    .content(DEFAULT_SYSTEM_PROMPT)
                    .knowledgeType(KnowledgeTypeStatus.SYSTEM_PROMPT)
                    .isActive(true)
                    .build();
            try {
                knowledgeRepository.save(newPromptDoc);
            } catch (Exception e) {
                log.error("Failed to seed system prompt to DB: {}", e.getMessage());
            }
        }

        StringBuilder prompt = new StringBuilder(currentPrompt);

        // ── Cross-lingual RAG: detect language & normalise query to Vietnamese ──
        // The knowledge base is stored entirely in Vietnamese, so we must translate
        // the user's query before computing the embedding for vector search.
        String detectedLang = languageDetectionService.detectLanguage(userMessage);
        String queryForSearch = languageDetectionService.translateToVietnamese(userMessage, detectedLang);

        if (!"vi".equals(detectedLang) && detectedLang != null) {
            queryForSearch = languageDetectionService.translateToVietnamese(userMessage, detectedLang);
        }
        // ── Vector Search (Cross-lingual Single-Phase RAG) ──────────────────────
        List<Double> queryVector = embeddingService.getEmbedding(queryForSearch);
        List<KnowledgeDocument> knowledgeBase;

        if (queryVector == null || queryVector.isEmpty()) {
            // Fallback to keyword search using the Vietnamese translation
            knowledgeBase = knowledgeRepository.searchByKeyword(queryForSearch).stream()
                    .filter(KnowledgeDocument::isActive)
                    .limit(3)
                    .toList();
        } else {
            // Perform global semantic vector search across all knowledge types
            knowledgeBase = vectorSearchRepository.vectorSearch(queryVector);

            // Fallback to keyword search if vector search returns no results
            if (knowledgeBase == null || knowledgeBase.isEmpty()) {
                log.info("[AI Strategy] Vector Search found no results. Falling back to keyword search for: '{}'", queryForSearch);
                knowledgeBase = knowledgeRepository.searchByKeyword(queryForSearch).stream()
                        .filter(KnowledgeDocument::isActive)
                        .limit(3)
                        .toList();
            }
        }

        if (!knowledgeBase.isEmpty()) {
            log.info("[AI Strategy] Vector Search (MongoDB): Found {} relevant knowledge documents (lang='{}', vi-query='{}').",
                    knowledgeBase.size(), detectedLang, queryForSearch);
            prompt.append("\n\n---\n")
                    .append("DỮ LIỆU KIẾN THỨC NỘI BỘ (Chỉ sử dụng khi cần trả lời các câu hỏi cụ thể):\n");
            for (int i = 0; i < knowledgeBase.size(); i++) {
                KnowledgeDocument doc = knowledgeBase.get(i);
                prompt.append(String.format("[%s] Bài viết %d: %s\n%s\n\n",
                        doc.getKnowledgeType().name(), i + 1, doc.getTitle(), doc.getContent()));
            }
            // Check the knowledge search results
            System.out.println("--- DEBUG: Knowledge Search Results ---");
            for (KnowledgeDocument doc : knowledgeBase) {
                System.out.println(String.format("ID: %s | Title: %s | Type: %s | Active: %s",
                        doc.getId(), doc.getTitle(), doc.getKnowledgeType(), doc.isActive()));
            }
        } else {
            log.info("[AI Strategy] Vector Search (MongoDB): No relevant content found (lang='{}', vi-query='{}').",
                    detectedLang, queryForSearch);
        }

        // ── Inject user language directive so GPT-4o always replies correctly ───
        // Even when context is found via cross-lingual search, GPT needs an explicit
        // reminder of the target output language, because context docs are in Vietnamese.
        prompt.append("\n---\n");
        prompt.append("*** FINAL CRITICAL INSTRUCTIONS (MUST OBEY) ***\n");

        if (!"vi".equals(detectedLang)) {
            prompt.append("1. STRICT LANGUAGE OVERRIDE: The user is speaking '").append(detectedLang).append("'. ");
            prompt.append("You MUST translate ALL factual content from Context/Tools into '").append(detectedLang).append("'. ");
            prompt.append("Do NOT output any Vietnamese text.\n");
        }

        prompt.append("2. TOOL FAILURE OVERRIDE: If a Tool returns empty ([], null), DO NOT APOLOGIZE IMMEDIATELY. ");
        prompt.append("You MUST read the Vietnamese Context above. If the Context has the answer, translate and use it. ");
        prompt.append("ONLY apologize if BOTH Tool and Context are empty.\n");

        return prompt.toString();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Conversation History Builder
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public ArrayNode buildConversationHistory(String roomId, String userMessage) {
        ArrayNode messages = objectMapper.createArrayNode();

        // System prompt first for OpenAI
        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", getSystemPrompt(userMessage));
        messages.add(systemMsg);

        // Fetch last 10 messages from MongoDB for deeper context
        var recentMessages = chatMessageRepository.findByChatRoomIdOrderByTimestampDesc(
                roomId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"))).getContent();

        // Reverse to chronological order
        List<ChatMessage> chronological = new ArrayList<>(recentMessages);
        Collections.reverse(chronological);

        for (ChatMessage msg : chronological) {
            ObjectNode msgNode = objectMapper.createObjectNode();
            if ("AI_ASSISTANT".equals(msg.getSenderId())) {
                msgNode.put("role", "assistant");
            } else {
                msgNode.put("role", "user");
            }
            String content = msg.getContent();
            if (content.length() > 2000) {
                content = content.substring(0, 2000);
            }

            // Re-inject the [TRANSFER_TO_HUMAN] tag for context so the model remembers its
            // behavior
            if ("AI_ASSISTANT".equals(msg.getSenderId()) && msg.getMetadata() != null
                    && Boolean.TRUE.equals(msg.getMetadata().get("transferToHuman"))) {
                content += " [TRANSFER_TO_HUMAN]";
            }

            msgNode.put("content", content);
            messages.add(msgNode);
        }

        return messages;
    }

    @Override
    public String searchKnowledgeBaseContext(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "";
        }

        List<Double> queryVector = embeddingService.getEmbedding(keyword);
        List<KnowledgeDocument> knowledgeBase;

        if (queryVector == null || queryVector.isEmpty()) {
            knowledgeBase = knowledgeRepository.searchByKeyword(keyword).stream()
                    .filter(KnowledgeDocument::isActive)
                    .limit(3)
                    .toList();
        } else {
            knowledgeBase = vectorSearchRepository.vectorSearch(queryVector);
            if (knowledgeBase == null || knowledgeBase.isEmpty()) {
                knowledgeBase = knowledgeRepository.searchByKeyword(keyword).stream()
                        .filter(KnowledgeDocument::isActive)
                        .limit(3)
                        .toList();
            }
        }

        if (knowledgeBase != null && !knowledgeBase.isEmpty()) {
            StringBuilder forcedContext = new StringBuilder();
            forcedContext.append("\n\n---\n*** HỆ THỐNG PHÁT HIỆN DỮ LIỆU TỪ KNOWLEDGE BASE (CONTEXT) ***\n");
            forcedContext.append("Bỏ qua lỗi 'Không tìm thấy' ở trên. Dùng các thông tin ngữ cảnh sau để trả lời:\n");
            for (int i = 0; i < knowledgeBase.size(); i++) {
                KnowledgeDocument doc = knowledgeBase.get(i);
                forcedContext.append(String.format("Bài viết %s: %s\n%s\n\n",
                        doc.getKnowledgeType().name(), doc.getTitle(), doc.getContent()));
            }
            return forcedContext.toString();
        }
        return "";
    }
}
