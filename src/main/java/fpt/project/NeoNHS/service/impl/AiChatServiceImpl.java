package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fpt.project.NeoNHS.config.OpenAiConfig;
import fpt.project.NeoNHS.dto.chat.ChatMessageDTO;
import fpt.project.NeoNHS.dto.chat.ChatMessageRequest;
import fpt.project.NeoNHS.service.AiChatService;
import fpt.project.NeoNHS.service.AiFunctionCallingService;
import fpt.project.NeoNHS.service.AiPromptService;
import fpt.project.NeoNHS.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {
    private static final Pattern CARD_MARKDOWN_PATTERN = Pattern.compile(
            "!\\[[^\\]]*\\bID\\s*:[^\\]]*\\bType\\s*:\\s*(workshop|event|blog|point)\\b[^\\]]*\\]\\([^\\)]*\\)",
            Pattern.CASE_INSENSITIVE);

    private final OpenAiConfig openAiConfig;
    private final RestClient openAiRestClient;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TransactionTemplate transactionTemplate;

    private final AiPromptService aiPromptService;
    private final AiFunctionCallingService aiFunctionCallingService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // ═══════════════════════════════════════════════════════════════════
    // Build OpenAI Request Body
    // ═══════════════════════════════════════════════════════════════════
    private ObjectNode buildOpenAiRequest(ArrayNode messages) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", openAiConfig.getModel());
        request.set("messages", messages);
        request.set("tools", aiFunctionCallingService.buildToolDeclarations());
        request.put("temperature", 0.7);
        request.put("max_tokens", 1024);
        return request;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Main Streaming Logic
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public SseEmitter streamAiReply(String roomId, String senderId, String message) {
        SseEmitter emitter = new SseEmitter(60_000L); // 60s timeout

        executor.submit(() -> {
            try {
                // 1. Rate Limiting (Hạn chế spam)
                if (isRateLimited(senderId)) {
                    sendErrorMessage(emitter, "Bạn đang gửi tin nhắn quá nhanh. Vui lòng đợi một lát.");
                    return;
                }

                // 2. Save user message to MongoDB and broadcast via WebSocket for real-time
                saveAndBroadcastUserMessage(roomId, senderId, message);

                ArrayNode messages = aiPromptService.buildConversationHistory(roomId, message);

                // SANDWICH PROMPTING: Ép AI dịch thuật
                String languageOverride = """
                        ***CRITICAL INSTRUCTION ***:
                        *** FINAL CRITICAL INSTRUCTIONS ***:
                        1. STRICT LANGUAGE OVERRIDE: Identify the exact language of the user's LAST message. Your ENTIRE reply MUST be strictly generated in that same language. NO EXCEPTIONS.
                        2. ZERO HALLUCINATION: If the <context> is empty and no Tools provided data, you are FORBIDDEN from answering based on pre-trained knowledge. You MUST reply with the localized translation of: 'Xin lỗi, hiện tại mình chưa có thông tin về cái này.'
                        """;

                for (JsonNode node : messages) {
                    if (node.isObject() && "system".equals(node.path("role").asText())) {
                        ObjectNode systemNode = (ObjectNode) node;
                        String currentSystemContent = systemNode.path("content").asText();
                        systemNode.put("content", currentSystemContent + languageOverride);
                        break;
                    }
                }

                ObjectNode requestBody = buildOpenAiRequest(messages);

                System.out.println("Request to OpenAI: " + requestBody.toString());
                // 3. Call OpenAI
                log.info("[AI] Attempting to call OpenAI with model: {}", openAiConfig.getModel());
                String responseJson = openAiRestClient.post()
                        .uri(openAiConfig.getChatCompletionUrl())
                        .body(requestBody.toString())
                        .retrieve()
                        .body(String.class);

                JsonNode response = objectMapper.readTree(responseJson);
                log.debug("[AI] OpenAI raw response: {}", responseJson);

                // 4. Check for function calls
                Map<String, Object> result = processOpenAiResponse(response, messages, senderId);
                String aiReplyText = (String) result.get("text");
                String messageType = (String) result.get("messageType");
                @SuppressWarnings("unchecked")
                Map<String, Object> aiMetadata = (Map<String, Object>) result.get("metadata");
                boolean usedFunction = (aiMetadata != null && aiMetadata.containsKey("usedFunctionCalling"));
                log.info("[AI Output Source] AI responded using data from: {}",
                        usedFunction ? "MySQL (Function Calling)" : "MongoDB (Vector Search / System Prompt)");
                log.info("[AI Output Text] {}", aiReplyText);

                aiReplyText = sanitizeAiTextForAttachedCards(aiReplyText, aiMetadata);

                System.out.println("AI Reply after processing: " + aiReplyText);

                // 5. Check for handover signal
                if (aiReplyText.contains("[TRANSFER_TO_HUMAN]")) {
                    String cleanText = aiReplyText.replaceAll("(?i)\\s*\\[TRANSFER_TO_HUMAN\\]\\s*", "").trim();
                    handleTransferToHuman(roomId, senderId, emitter, cleanText);
                    return;
                }

                // 6. Stream the response to client
                streamTextToClient(emitter, aiReplyText, aiMetadata);

                // 8. Save AI response to MongoDB and broadcast via WebSocket
                saveAndBroadcastAiMessage(roomId, aiReplyText, messageType, aiMetadata);

                emitter.complete();

            } catch (Exception e) {
                log.error("[AI] Error processing AI reply for room {}: {}", roomId, e.getMessage());
                String errorMsg = "Hệ thống AI đang quá tải (429/Quota). Hãy thử lại sau ít phút hoặc gặp NV hỗ trợ.";
                if (e.getMessage() != null && e.getMessage().contains("403")) {
                    errorMsg = "Lỗi Access Denied: Hãy kiểm tra vùng địa lý hoặc API key.";
                }
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\": \"" + errorMsg + "\"}"));
                } catch (IOException ignored) {
                }
                emitter.complete();
            }
        });

        return emitter;
    }

    /**
     * Process the OpenAI response, handling tool calls if needed.
     */
    private Map<String, Object> processOpenAiResponse(JsonNode response, ArrayNode messages, String senderId)
            throws Exception {
        Map<String, Object> metadata = new java.util.HashMap<>();
        JsonNode currentResponse = response;

        // Confirm
        int maxIterations = 5;

        for (int i = 0; i < maxIterations; i++) {
            JsonNode choice = currentResponse.path("choices").path(0);
            JsonNode message = choice.path("message");

            if (message.has("tool_calls")) {
                JsonNode toolCalls = message.get("tool_calls");
                log.info("[AI Strategy] Function Calling (MySQL): AI invoked {} tool(s).", toolCalls.size());
                metadata.put("usedFunctionCalling", true); // Flag for the final response log
                messages.add(message.deepCopy());

                for (JsonNode toolCall : toolCalls) {
                    String functionName = toolCall.path("function").path("name").asText();
                    String toolCallId = toolCall.path("id").asText();
                    JsonNode functionArgs = objectMapper
                            .readTree(toolCall.path("function").path("arguments").asText("{}"));

                    String functionResult = "";
                    try {
                        functionResult = transactionTemplate.execute(status -> {
                            try {
                                return aiFunctionCallingService.executeFunctionCall(functionName, functionArgs,
                                        senderId, metadata);
                            } catch (Exception e) {
                                status.setRollbackOnly();
                                return "ERROR: " + e.getMessage();
                            }
                        });
                    } catch (Exception e) {
                        functionResult = "ERROR: Lỗi hệ thống.";
                    }

                    if (functionResult == null) {
                        functionResult = "ERROR: Kết quả trả về rỗng.";
                    }

                    // Kích hoạt Vector Search nếu Function Calling trả về "Không tìm thấy"
                    if (functionResult != null && (functionResult.contains("Không tìm thấy") || functionResult.contains("hiện không có"))) {
                        String keyword = "";
                        if (functionArgs.has("keyword")) {
                            keyword = functionArgs.get("keyword").asText();
                        } else if (functionArgs.has("eventName")) {
                            keyword = functionArgs.get("eventName").asText();
                        }

                        if (!keyword.isEmpty()) {
                            log.info("[AI Strategy] Function {} returned empty. Forcing Vector Search for parsed keyword: {}", functionName, keyword);
                            String additionalContext = aiPromptService.searchKnowledgeBaseContext(keyword);
                            if (!additionalContext.isEmpty()) {
                                functionResult += additionalContext;
                                metadata.remove("usedFunctionCalling"); // Đánh dấu là sử dụng dữ liệu từ Vector Search
                                log.info("[AI Strategy] Forced Vector Search successful. Injected into tool response.");
                            }
                        }
                    }

                    // Lưu kết quả tool
                    ObjectNode toolResultNode = objectMapper.createObjectNode();
                    toolResultNode.put("role", "tool");
                    toolResultNode.put("tool_call_id", toolCallId);
                    toolResultNode.put("content", functionResult);
                    messages.add(toolResultNode);

                    // Đánh dấu metadata nếu thành công
                    if ("addToCart".equals(functionName) && functionResult.contains("SUCCESS")) {
                        metadata.put("redirectToCart", true);
                    }

                    if ("navigateToLocation".equals(functionName) && functionResult.contains("SUCCESS")) {
                        metadata.put("redirectToMap", true);
                        metadata.put("targetPointId", functionArgs.get("pointId").asText());
                        metadata.put("targetPointName", functionArgs.get("pointName").asText());
                    }
                }

                // Gọi lại OpenAI
                ObjectNode followUpRequest = buildOpenAiRequest(messages);
                String followUpJson = openAiRestClient.post()
                        .uri(openAiConfig.getChatCompletionUrl())
                        .body(followUpRequest.toString())
                        .retrieve()
                        .body(String.class);

                currentResponse = objectMapper.readTree(followUpJson);

                // Nếu đây là vòng lặp cuối cùng mà vẫn còn tool_calls, thì mới báo lỗi quá tải
                if (i == maxIterations - 1
                        && currentResponse.path("choices").path(0).path("message").has("tool_calls")) {
                    return createErrorResponse("Hệ thống đang bận xử lý quá nhiều tác vụ, vui lòng thử lại.");
                }

            } else {
                // AI TRẢ VỀ TEXT CUỐI CÙNG (Đây là nơi log của bạn bị ngắt)
                String aiContent = message.path("content").asText();

                // Kiểm tra lại metadata lần cuối trong text
                if (aiContent.toLowerCase().contains("giỏ hàng") || aiContent.toLowerCase().contains("thanh toán")) {
                    metadata.put("redirectToCart", true);
                }

                Map<String, Object> res = new java.util.HashMap<>();
                res.put("text", aiContent);
                res.put("metadata", metadata.isEmpty() ? null : metadata);
                res.put("messageType", "TEXT");
                return res;
            }
        }
        return createErrorResponse("Xin lỗi, tôi không thể hoàn tất yêu cầu này ngay bây giờ.");
    }

    private Map<String, Object> createErrorResponse(String errorMsg) {
        Map<String, Object> res = new java.util.HashMap<>();
        res.put("text", errorMsg);
        res.put("messageType", "TEXT");
        return res;
    }

    private String sanitizeAiTextForAttachedCards(String aiReplyText, Map<String, Object> metadata) {
        String safeText = aiReplyText == null ? "" : aiReplyText.trim();
        if (metadata == null || !metadata.containsKey("attachedCards")) {
            return safeText;
        }
        Object cardsObj = metadata.get("attachedCards");
        if (!(cardsObj instanceof List<?> cards) || cards.isEmpty()) {
            return safeText;
        }
        // Single source of truth: cards come from metadata.attachedCards only.
        // Remove card-markdown from text to avoid duplicate rendering in FE.
        String cleaned = CARD_MARKDOWN_PATTERN.matcher(safeText).replaceAll("");
        return cleaned.replaceAll("\\n{3,}", "\n\n").trim();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    private void saveAndBroadcastUserMessage(String roomId, String senderId, String message) {
        ChatMessageRequest request = ChatMessageRequest.builder()
                .chatRoomId(roomId)
                .content(message)
                .messageType("TEXT")
                // .messageType("PRODUCT_SNIPPET")
                .build();
        ChatMessageDTO savedMsg = chatService.sendMessage(senderId, request);

        // Broadcast to all participants via WebSocket
        List<String> participants = chatService.getRoomParticipants(roomId);
        for (String pid : participants) {
            messagingTemplate.convertAndSendToUser(pid, "/queue/messages", savedMsg);
        }
    }

    private void saveAndBroadcastAiMessageWithMetadata(String roomId, String aiText, Map<String, Object> metadata) {
        // SỬA: Phải truyền biến metadata vào, không được truyền null
        saveAndBroadcastAiMessage(roomId, aiText, "TEXT", metadata);
    }

    private void saveAndBroadcastAiMessage(String roomId, String aiText, String messageType,
            Map<String, Object> metadata) {
        ChatMessageRequest aiRequest = ChatMessageRequest.builder()
                .chatRoomId(roomId)
                .content(aiText)
                .messageType(messageType != null ? messageType : "TEXT")
                .metadata(metadata)
                .build();

        ChatMessageDTO savedMsg = chatService.sendMessage("AI_ASSISTANT", aiRequest);

        // Broadcast to all participants via WebSocket
        List<String> participants = chatService.getRoomParticipants(roomId);
        for (String pid : participants) {
            messagingTemplate.convertAndSendToUser(pid, "/queue/messages", savedMsg);
        }
    }

    private void streamTextToClient(SseEmitter emitter, String text, Map<String, Object> metadata) throws IOException {
        // Simulate streaming by sending chunks of ~3-5 words
        String[] words = text.split("(?<=\\s)");
        StringBuilder chunk = new StringBuilder();
        int wordCount = 0;

        for (String word : words) {
            chunk.append(word);
            wordCount++;

            if (wordCount >= 3) {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data("{\"text\": " + objectMapper.writeValueAsString(chunk.toString()) + "}"));
                chunk.setLength(0);
                wordCount = 0;

                // Small delay for natural streaming effect
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
        }

        // Send remaining text
        if (!chunk.isEmpty()) {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data("{\"text\": " + objectMapper.writeValueAsString(chunk.toString()) + "}"));
        }

        // Send completion event
        ObjectNode doneNode = objectMapper.createObjectNode();
        doneNode.put("fullText", text);
        if (metadata != null && metadata.containsKey("attachedCards")) {
            doneNode.set("attachedCards", objectMapper.valueToTree(metadata.get("attachedCards")));
        }

        emitter.send(SseEmitter.event()
                .name("done")
                .data(doneNode.toString()));
    }

    private void handleTransferToHuman(String roomId, String senderId, SseEmitter emitter, String cleanText)
            throws IOException {
        // Save transfer system message with metadata
        saveAndBroadcastAiMessageWithMetadata(roomId, cleanText, Map.of("transferToHuman", true));

        // Stream to client
        emitter.send(SseEmitter.event()
                .name("transfer")
                .data("{\"message\": " + objectMapper.writeValueAsString(cleanText) + "}"));
        emitter.send(SseEmitter.event()
                .name("done")
                .data("{\"fullText\": " + objectMapper.writeValueAsString(cleanText) + "}"));
        emitter.complete();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Optimization Helpers
    // ═══════════════════════════════════════════════════════════════════

    private boolean isRateLimited(String userId) {
        String key = "ai_rate_limit:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        return count != null && count > 10; // Giới hạn 10 tin nhắn mỗi phút
    }

    private void sendErrorMessage(SseEmitter emitter, String errorMsg) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"error\": \"" + errorMsg + "\"}"));
            emitter.complete();
        } catch (IOException ignored) {
        }
    }
}
