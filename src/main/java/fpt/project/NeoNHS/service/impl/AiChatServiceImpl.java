package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fpt.project.NeoNHS.config.OpenAiConfig;
import fpt.project.NeoNHS.document.ChatMessage;
import fpt.project.NeoNHS.dto.chat.ChatMessageDTO;
import fpt.project.NeoNHS.dto.chat.ChatMessageRequest;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.enums.SessionStatus;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.TicketCatalogRepository;
import fpt.project.NeoNHS.repository.WorkshopSessionRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.repository.mongo.ChatMessageRepository;
import fpt.project.NeoNHS.repository.mongo.ChatRoomRepository;
import fpt.project.NeoNHS.repository.BlogRepository;
import fpt.project.NeoNHS.repository.mongo.KnowledgeRepository;
import fpt.project.NeoNHS.document.KnowledgeDocument;
import fpt.project.NeoNHS.entity.Blog;
import fpt.project.NeoNHS.enums.BlogStatus;
import fpt.project.NeoNHS.service.AiChatService;
import fpt.project.NeoNHS.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private final OpenAiConfig openAiConfig;
    private final RestClient openAiRestClient;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Repositories for Function Calling
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final EventRepository eventRepository;
    private final TicketCatalogRepository ticketCatalogRepository;
    private final KnowledgeRepository knowledgeRepository;
    private final BlogRepository blogRepository;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    // ═══════════════════════════════════════════════════════════════════
    // System Prompt
    // ═══════════════════════════════════════════════════════════════════
    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý du lịch ảo của Khu di tích Ngũ Hành Sơn (NeoNHS), Đà Nẵng, Việt Nam.

            Vai trò:
            - Hỗ trợ du khách với thông tin về khu di tích, các điểm tham quan, sự kiện, workshop và các bài viết tin tức/văn hóa (blog).
            - Trả lời bằng tiếng Việt, thân thiện, ngắn gọn và chính xác.
            - Khi được hỏi về thông tin cụ thể (giá vé, lịch workshop, sự kiện, bài viết), hãy sử dụng các công cụ (tools/functions) được cung cấp để tra cứu dữ liệu thực tế.

            Quy tắc quan trọng:
            1. Tuyệt đối KHÔNG sử dụng các công cụ (tools) nếu người dùng chỉ đang chào hỏi hoặc hỏi những câu giao tiếp thông thường.
            2. Khi người dùng hỏi về giá vé, hãy LIỆT KÊ TẤT CẢ các loại vé bạn tìm thấy được từ công cụ, không được bỏ sót loại nào (ví dụ: vé người lớn, trẻ em, người nước ngoài, v.v.).
            3. Trình bày danh sách vé một cách đẹp mắt bằng Markdown (ví dụ: dùng bảng hoặc danh sách có gạch đầu dòng).
            4. **HỖ TRỢ HÌNH ẢNH**: Khi cung cấp thông tin về Workshop, Sự kiện hoặc Bài viết, hãy LUÔN LUÔN đính kèm hình ảnh (imageUrl) nếu công cụ trả về. Sử dụng cú pháp Markdown: ![Tên](url).
            5. **HỖ TRỢ ĐẶT VÉ**: Nếu người dùng muốn đặt vé/workshop, hãy hướng dẫn họ chọn buổi/loại vé và nhắc họ có thể hoàn tất thanh toán trong ứng dụng. Bạn có thể cung cấp thông tin chi tiết để họ dễ dành lựa chọn.
            6. KHÔNG bịa đặt thông tin. Nếu không có dữ liệu, hãy báo là chưa cập nhật.
            7. Nếu câu hỏi nằm ngoài phạm vi hoặc cần hỗ trợ trực tiếp từ nhân viên, hãy trả lời CHÍNH XÁC bằng chuỗi: [TRANSFER_TO_HUMAN].
            8. Giữ câu trả lời thân thiện, sử dụng emoji phù hợp 🌸.
            9. **NGÔN NGỮ**: Trả lời bằng ngôn ngữ mà người dùng đang sử dụng. Nếu người dùng hỏi bằng tiếng Anh, hãy trả lời bằng tiếng Anh. Nếu hỏi bằng tiếng Nhật, hãy trả lời bằng tiếng Nhật. Mặc định là tiếng Việt.
            """;

    private String getSystemPrompt() {
        StringBuilder prompt = new StringBuilder(SYSTEM_PROMPT);
        List<KnowledgeDocument> knowledgeBase = knowledgeRepository.findByIsActiveTrue();
        if (!knowledgeBase.isEmpty()) {
            prompt.append("\n\n---\n")
                    .append("DỮ LIỆU KIẾN THỨC NỘI BỘ (Chỉ sử dụng khi cần trả lời các câu hỏi cụ thể):\n");
            for (int i = 0; i < knowledgeBase.size(); i++) {
                KnowledgeDocument doc = knowledgeBase.get(i);
                prompt.append(String.format("Bài viết %d: %s\n%s\n\n", i + 1, doc.getTitle(), doc.getContent()));
            }
        }
        return prompt.toString();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Tool Declarations (OpenAI Function Calling)
    // ═══════════════════════════════════════════════════════════════════
    private JsonNode buildToolDeclarations() {
        try {
            String toolsJson = """
                    [
                      {
                        "type": "function",
                        "function": {
                          "name": "searchWorkshops",
                          "description": "Tìm kiếm các workshop hiện có tại Ngũ Hành Sơn. Trả về danh sách workshop với tên, mô tả, giá và trạng thái.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "keyword": {
                                "type": "string",
                                "description": "Từ khóa tìm kiếm (tên workshop). Để trống nếu muốn lấy tất cả."
                              }
                            }
                          }
                        }
                      },
                      {
                        "type": "function",
                        "function": {
                          "name": "getWorkshopSessions",
                          "description": "Lấy danh sách các buổi (session) sắp diễn ra của một workshop cụ thể, bao gồm thời gian, giá và số chỗ còn trống.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "workshopName": {
                                "type": "string",
                                "description": "Tên của workshop cần tra cứu"
                              }
                            },
                            "required": ["workshopName"]
                          }
                        }
                      },
                      {
                        "type": "function",
                        "function": {
                          "name": "searchEvents",
                          "description": "Tìm kiếm các sự kiện đang hoặc sắp diễn ra tại Ngũ Hành Sơn.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "keyword": {
                                "type": "string",
                                "description": "Từ khóa tìm kiếm (tên sự kiện). Để trống nếu muốn lấy tất cả."
                              }
                            }
                          }
                        }
                      },
                      {
                        "type": "function",
                        "function": {
                          "name": "getTicketPrices",
                          "description": "Lấy thông tin giá vé tham quan khu di tích Ngũ Hành Sơn (vé vào cổng) hoặc vé sự kiện.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "eventName": {
                                "type": "string",
                                "description": "Tên sự kiện cần tra giá vé. Để trống nếu muốn tra giá vé tham quan chung."
                              }
                            }
                          }
                        }
                      },
                      {
                        "type": "function",
                        "function": {
                          "name": "searchBlogs",
                          "description": "Tìm kiếm các bài viết, tin tức, blog về văn hóa, lịch sử và sự kiện tại Ngũ Hành Sơn.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "keyword": {
                                "type": "string",
                                "description": "Từ khóa tìm kiếm (tiêu đề bài viết). Để trống nếu muốn lấy các bài mới nhất."
                              }
                            }
                          }
                        }
                      }
                    ]
                    """;
            return objectMapper.readTree(toolsJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse tool declarations", e);
            return objectMapper.createArrayNode();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Function Calling Execution
    // ═══════════════════════════════════════════════════════════════════
    private String executeFunctionCall(String functionName, JsonNode args) {
        log.info("[AI] Executing function: {} with args: {}", functionName, args);
        try {
            return switch (functionName) {
                case "searchWorkshops" -> executeSearchWorkshops(args);
                case "getWorkshopSessions" -> executeGetWorkshopSessions(args);
                case "searchEvents" -> executeSearchEvents(args);
                case "getTicketPrices" -> executeGetTicketPrices(args);
                case "searchBlogs" -> executeSearchBlogs(args);
                default -> "Không tìm thấy công cụ: " + functionName;
            };
        } catch (Exception e) {
            log.error("[AI] Function execution error for {}: {}", functionName, e.getMessage());
            return "Lỗi khi tra cứu dữ liệu: " + e.getMessage();
        }
    }

    private String executeSearchWorkshops(JsonNode args) {
        String keyword = args.has("keyword") ? args.get("keyword").asText("") : "";
        var workshops = workshopTemplateRepository.findAll().stream()
                .filter(w -> w.getIsPublished() && keyword.isEmpty() ||
                        w.getName().toLowerCase().contains(keyword.toLowerCase()))
                .limit(5)
                .map(w -> Map.of(
                        "id", w.getId().toString(),
                        "name", w.getName(),
                        "description", w.getShortDescription() != null ? w.getShortDescription() : "",
                        "price", w.getDefaultPrice() != null ? w.getDefaultPrice().toString() + " VND" : "Liên hệ",
                        "status", w.getStatus().name(),
                        "rating", w.getAverageRating().toString(),
                        "imageUrl", (w.getWorkshopImages() != null && !w.getWorkshopImages().isEmpty())
                                ? w.getWorkshopImages().get(0).getImageUrl()
                                : ""))
                .toList();
        if (workshops.isEmpty())
            return "Không tìm thấy workshop nào" + (keyword.isEmpty() ? "." : " với từ khóa '" + keyword + "'.");
        try {
            return objectMapper.writeValueAsString(workshops);
        } catch (JsonProcessingException e) {
            return workshops.toString();
        }
    }

    private String executeGetWorkshopSessions(JsonNode args) {
        String workshopName = args.get("workshopName").asText();
        var templates = workshopTemplateRepository.findAll().stream()
                .filter(w -> w.getName().toLowerCase().contains(workshopName.toLowerCase()))
                .toList();
        if (templates.isEmpty())
            return "Không tìm thấy workshop có tên '" + workshopName + "'.";

        var template = templates.getFirst();
        var sessions = workshopSessionRepository.findAll().stream()
                .filter(s -> s.getWorkshopTemplate().getId().equals(template.getId())
                        && s.getStartTime().isAfter(LocalDateTime.now())
                        && s.getStatus() == SessionStatus.SCHEDULED)
                .limit(5)
                .map(s -> Map.of(
                        "startTime", s.getStartTime().toString(),
                        "endTime", s.getEndTime().toString(),
                        "price",
                        s.getPrice() != null ? s.getPrice().toString() + " VND" : template.getDefaultPrice() + " VND",
                        "maxParticipants", s.getMaxParticipants() != null ? s.getMaxParticipants() : 0,
                        "currentEnrolled", s.getCurrentEnrolled() != null ? s.getCurrentEnrolled() : 0,
                        "availableSlots", (s.getMaxParticipants() != null ? s.getMaxParticipants() : 0) -
                                (s.getCurrentEnrolled() != null ? s.getCurrentEnrolled() : 0)))
                .toList();
        if (sessions.isEmpty())
            return "Workshop '" + template.getName() + "' hiện không có buổi nào sắp diễn ra.";
        try {
            return objectMapper
                    .writeValueAsString(Map.of("workshopName", template.getName(), "upcomingSessions", sessions));
        } catch (JsonProcessingException e) {
            return sessions.toString();
        }
    }

    private String executeSearchEvents(JsonNode args) {
        String keyword = args.has("keyword") ? args.get("keyword").asText("") : "";
        var events = eventRepository.findAll().stream()
                .filter(e -> (e.getStatus() == EventStatus.UPCOMING || e.getStatus() == EventStatus.ONGOING)
                        && (keyword.isEmpty() || e.getName().toLowerCase().contains(keyword.toLowerCase())))
                .limit(5)
                .map(e -> Map.of(
                        "id", e.getId().toString(),
                        "name", e.getName(),
                        "description", e.getShortDescription() != null ? e.getShortDescription() : "",
                        "location", e.getLocationName() != null ? e.getLocationName() : "",
                        "startTime", e.getStartTime().toString(),
                        "endTime", e.getEndTime().toString(),
                        "status", e.getStatus().name(),
                        "ticketRequired", e.getIsTicketRequired(),
                        "price", e.getPrice() != null ? e.getPrice().toString() + " VND" : "Miễn phí",
                        "imageUrl", (e.getEventImages() != null && !e.getEventImages().isEmpty())
                                ? e.getEventImages().get(0).getImageUrl()
                                : ""))
                .toList();
        if (events.isEmpty())
            return "Không tìm thấy sự kiện nào"
                    + (keyword.isEmpty() ? " sắp diễn ra." : " với từ khóa '" + keyword + "'.");
        try {
            return objectMapper.writeValueAsString(events);
        } catch (JsonProcessingException e) {
            return events.toString();
        }
    }

    private String executeGetTicketPrices(JsonNode args) {
        String eventName = args.has("eventName") ? args.get("eventName").asText("") : "";

        if (eventName.isEmpty()) {
            // General attraction tickets
            var catalogs = ticketCatalogRepository.findAll().stream()
                    .filter(tc -> tc.getAttraction() != null)
                    .limit(20)
                    .map(tc -> Map.of(
                            "name", tc.getName(),
                            "customerType", tc.getCustomerType() != null ? tc.getCustomerType() : "Chung",
                            "price", tc.getPrice().toString() + " VND",
                            "description", tc.getDescription() != null ? tc.getDescription() : ""))
                    .toList();
            if (catalogs.isEmpty())
                return "Chưa có thông tin giá vé tham quan.";
            try {
                return objectMapper.writeValueAsString(catalogs);
            } catch (JsonProcessingException e) {
                return catalogs.toString();
            }
        } else {
            // Event-specific tickets
            var events = eventRepository.findAll().stream()
                    .filter(e -> e.getName().toLowerCase().contains(eventName.toLowerCase()))
                    .toList();
            if (events.isEmpty())
                return "Không tìm thấy sự kiện '" + eventName + "'.";

            var event = events.getFirst();
            var catalogs = ticketCatalogRepository.findAll().stream()
                    .filter(tc -> tc.getEvent() != null && tc.getEvent().getId().equals(event.getId()))
                    .map(tc -> Map.of(
                            "name", tc.getName(),
                            "price", tc.getPrice().toString() + " VND",
                            "description", tc.getDescription() != null ? tc.getDescription() : "",
                            "available",
                            tc.getTotalQuota() != null
                                    ? (tc.getTotalQuota() - (tc.getSoldQuantity() != null ? tc.getSoldQuantity() : 0))
                                    : "Không giới hạn"))
                    .toList();
            if (catalogs.isEmpty())
                return "Sự kiện '" + event.getName() + "' hiện không yêu cầu vé hoặc chưa có thông tin giá.";
            try {
                return objectMapper.writeValueAsString(Map.of("eventName", event.getName(), "tickets", catalogs));
            } catch (JsonProcessingException e) {
                return catalogs.toString();
            }
        }
    }

    private String executeSearchBlogs(JsonNode args) {
        String keyword = args.has("keyword") ? args.get("keyword").asText("") : "";
        var blogs = blogRepository.findAll().stream()
                .filter(b -> b.getStatus() == BlogStatus.PUBLISHED &&
                        (keyword.isEmpty() || b.getTitle().toLowerCase().contains(keyword.toLowerCase())))
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .limit(5)
                .map(b -> Map.of(
                        "title", b.getTitle(),
                        "summary", b.getSummary() != null ? b.getSummary() : "",
                        "imageUrl", b.getThumbnailUrl() != null ? b.getThumbnailUrl() : "",
                        "author", b.getUser() != null ? b.getUser().getFullname() : "Admin",
                        "publishedAt", b.getPublishedAt() != null ? b.getPublishedAt().toString() : ""))
                .toList();
        if (blogs.isEmpty())
            return "Không tìm thấy bài viết nào" + (keyword.isEmpty() ? "." : " với từ khóa '" + keyword + "'.");
        try {
            return objectMapper.writeValueAsString(blogs);
        } catch (JsonProcessingException e) {
            return blogs.toString();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Conversation History Builder
    // ═══════════════════════════════════════════════════════════════════
    private ArrayNode buildConversationHistory(String roomId, String userMessage) {
        ArrayNode messages = objectMapper.createArrayNode();

        // System prompt first for OpenAI
        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", getSystemPrompt());
        messages.add(systemMsg);

        // Fetch last 10 messages from MongoDB
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
            msgNode.put("content", msg.getContent());
            messages.add(msgNode);
        }

        // Add current user message
        ObjectNode userNode = objectMapper.createObjectNode();
        userNode.put("role", "user");
        userNode.put("content", userMessage);
        messages.add(userNode);

        return messages;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Build OpenAI Request Body
    // ═══════════════════════════════════════════════════════════════════
    private ObjectNode buildOpenAiRequest(ArrayNode messages) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", openAiConfig.getModel());
        request.set("messages", messages);
        request.set("tools", buildToolDeclarations());
        request.put("tool_choice", "auto");
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

                // 3. Cache Lookup (Sử dụng lại câu trả lời cũ cho câu hỏi giống hệt)
                String normalizedMsg = message.trim().toLowerCase();
                String cacheKey = "ai_cache:" + normalizedMsg;
                String cachedResponse = (String) redisTemplate.opsForValue().get(cacheKey);

                if (cachedResponse != null) {
                    log.info("[AI] Cache hit for message: {}", normalizedMsg);
                    streamTextToClient(emitter, cachedResponse);
                    saveAndBroadcastAiMessage(roomId, cachedResponse);
                    emitter.complete();
                    return;
                }

                // 4. Build conversation context
                ArrayNode messages = buildConversationHistory(roomId, message);
                ObjectNode requestBody = buildOpenAiRequest(messages);

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
                String aiReplyText = processOpenAiResponse(response, messages);

                // 5. Check for handover signal
                if (aiReplyText.contains("[TRANSFER_TO_HUMAN]")) {
                    handleTransferToHuman(roomId, senderId, emitter);
                    return;
                }

                // 6. Stream the response to client
                streamTextToClient(emitter, aiReplyText);

                // 8. Save AI response to MongoDB and broadcast via WebSocket
                saveAndBroadcastAiMessage(roomId, aiReplyText);

                // 9. Cache the response (Lưu cache cho các câu hỏi sau)
                if (!aiReplyText.contains("[TRANSFER_TO_HUMAN]") && aiReplyText.length() > 20) {
                    redisTemplate.opsForValue().set(cacheKey, aiReplyText, 1, TimeUnit.DAYS);
                }

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
    private String processOpenAiResponse(JsonNode response, ArrayNode messages) throws Exception {
        JsonNode choice = response.path("choices").path(0);
        JsonNode message = choice.path("message");

        if (message.has("tool_calls")) {
            JsonNode toolCalls = message.get("tool_calls");

            // Add assistant's tool call message to history
            messages.add(message.deepCopy());

            for (JsonNode toolCall : toolCalls) {
                String functionName = toolCall.path("function").path("name").asText();
                String toolCallId = toolCall.path("id").asText();
                JsonNode functionArgs = objectMapper.readTree(toolCall.path("function").path("arguments").asText("{}"));

                // Execute the function
                String functionResult = executeFunctionCall(functionName, functionArgs);

                // Add tool result to messages
                ObjectNode toolResultNode = objectMapper.createObjectNode();
                toolResultNode.put("role", "tool");
                toolResultNode.put("tool_call_id", toolCallId);
                toolResultNode.put("content", functionResult);
                messages.add(toolResultNode);
            }

            // Call OpenAI again with tool results
            ObjectNode followUpRequest = buildOpenAiRequest(messages);
            followUpRequest.remove("tools"); // Optional: prevent recursive tool calls

            String followUpJson = openAiRestClient.post()
                    .uri(openAiConfig.getChatCompletionUrl())
                    .body(followUpRequest.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode followUpResponse = objectMapper.readTree(followUpJson);
            return followUpResponse.path("choices").path(0).path("message").path("content").asText();
        }

        // Direct text response
        return message.path("content").asText("Xin lỗi, tôi gặp trục trặc khi xử lý câu hỏi.");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    private void saveAndBroadcastUserMessage(String roomId, String senderId, String message) {
        ChatMessageRequest request = ChatMessageRequest.builder()
                .chatRoomId(roomId)
                .content(message)
                .messageType("TEXT")
                .build();
        ChatMessageDTO savedMsg = chatService.sendMessage(senderId, request);

        // Broadcast to all participants via WebSocket
        List<String> participants = chatService.getRoomParticipants(roomId);
        for (String pid : participants) {
            messagingTemplate.convertAndSendToUser(pid, "/queue/messages", savedMsg);
        }
    }

    private void saveAndBroadcastAiMessage(String roomId, String aiText) {
        ChatMessageRequest aiRequest = ChatMessageRequest.builder()
                .chatRoomId(roomId)
                .content(aiText)
                .messageType("TEXT")
                .build();
        ChatMessageDTO savedMsg = chatService.sendMessage("AI_ASSISTANT", aiRequest);

        // Broadcast to all participants via WebSocket
        List<String> participants = chatService.getRoomParticipants(roomId);
        for (String pid : participants) {
            messagingTemplate.convertAndSendToUser(pid, "/queue/messages", savedMsg);
        }
    }

    private void streamTextToClient(SseEmitter emitter, String text) throws IOException {
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
        emitter.send(SseEmitter.event()
                .name("done")
                .data("{\"fullText\": " + objectMapper.writeValueAsString(text) + "}"));
    }

    private void handleTransferToHuman(String roomId, String senderId, SseEmitter emitter) throws IOException {
        String transferMessage = "Câu hỏi vượt quá khả năng trả lời của tôi bạn có muốn trò chuyện với Admin để được giải đáp không";

        // Save transfer system message
        saveAndBroadcastAiMessage(roomId, transferMessage);

        // Stream to client
        emitter.send(SseEmitter.event()
                .name("transfer")
                .data("{\"message\": " + objectMapper.writeValueAsString(transferMessage) + "}"));
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
