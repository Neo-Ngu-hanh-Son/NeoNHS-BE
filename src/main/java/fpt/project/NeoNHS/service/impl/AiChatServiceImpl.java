package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fpt.project.NeoNHS.config.OpenAiConfig;
import fpt.project.NeoNHS.document.ChatMessage;
import fpt.project.NeoNHS.document.KnowledgeDocument;
import fpt.project.NeoNHS.dto.chat.ChatMessageDTO;
import fpt.project.NeoNHS.dto.chat.ChatMessageRequest;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.enums.SessionStatus;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.TicketCatalogRepository;
import fpt.project.NeoNHS.repository.WorkshopSessionRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.repository.mongo.ChatMessageRepository;
import fpt.project.NeoNHS.repository.BlogRepository;
import fpt.project.NeoNHS.repository.mongo.KnowledgeRepository;
import fpt.project.NeoNHS.repository.mongo.VectorSearchRepository;
import fpt.project.NeoNHS.enums.BlogStatus;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.service.AiChatService;
import fpt.project.NeoNHS.service.CartService;
import fpt.project.NeoNHS.service.ChatService;
import fpt.project.NeoNHS.service.EmbeddingService;
import fpt.project.NeoNHS.dto.request.cart.AddToCartRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.data.redis.core.RedisTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private final OpenAiConfig openAiConfig;
    private final RestClient openAiRestClient;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final EmbeddingService embeddingService;
    private final VectorSearchRepository vectorSearchRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Repositories for Function Calling
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final EventRepository eventRepository;
    private final TicketCatalogRepository ticketCatalogRepository;
    private final KnowledgeRepository knowledgeRepository;
    private final BlogRepository blogRepository;
    private final PointRepository pointRepository;
    private final TransactionTemplate transactionTemplate;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    // ═══════════════════════════════════════════════════════════════════
    // System Prompt
    // ═══════════════════════════════════════════════════════════════════
    private static final String DEFAULT_SYSTEM_PROMPT = """
            Bạn là trợ lý du lịch ảo của Khu di tích Ngũ Hành Sơn và khu vực Phường Ngũ Hành Sơn, Đà Nẵng, Việt Nam.

            === PHẠM VI HOẠT ĐỘNG (RẤT QUAN TRỌNG) ===
            Bạn CHỈ trả lời các câu hỏi liên quan đến:
            - Khu di tích Ngũ Hành Sơn (Marble Mountains): lịch sử, hang động, chùa, điểm tham quan
            - Phường Ngũ Hành Sơn, Đà Nẵng: làng nghề đá, văn hóa địa phương
            - Dịch vụ của hệ thống: vé tham quan, workshop, sự kiện, blog/tin tức
            - Hướng dẫn tham quan: đường đi, giờ mở cửa, nội quy, mẹo du lịch
            - Thông tin du lịch Đà Nẵng liên quan trực tiếp đến Ngũ Hành Sơn

            === QUY TẮC XỬ LÝ CÂU HỎI ===

            1. CÂU HỎI NGOÀI PHẠM VI (Từ chối - KHÔNG chuyển cho admin):
               Nếu người dùng hỏi về: dịch thuật, lập trình, toán học, khoa học,
               các địa điểm không liên quan, tin tức thời sự, giải trí, hay bất kỳ
               chủ đề nào KHÔNG liên quan đến Ngũ Hành Sơn/Đà Nẵng:
               → Từ chối lịch sự và gợi ý họ hỏi về Ngũ Hành Sơn.
               → TUYỆT ĐỐI KHÔNG thêm tag [TRANSFER_TO_HUMAN].
               → KHÔNG hỏi "Bạn có muốn gặp người hỗ trợ không?" cho câu hỏi ngoài phạm vi.
               Ví dụ trả lời: "Xin lỗi, tôi chỉ hỗ trợ về du lịch Ngũ Hành Sơn.
               Bạn có muốn tìm hiểu về các hang động, workshop hay sự kiện tại đây không?"

            2. CÂU HỎI TRONG PHẠM VI - TRẢ LỜI ĐƯỢC:
               Sử dụng kiến thức nội bộ (dữ liệu bên dưới) và các công cụ (Function Calling) để trả lời.
               Trả lời thân thiện, ngắn gọn, chính xác.

            3. CÂU HỎI TRONG PHẠM VI - CẦN NHÂN VIÊN (Chuyển tiếp cho admin):
               CHỈ thêm tag [TRANSFER_TO_HUMAN] khi người dùng yêu cầu thuộc các trường hợp sau:
               - Khiếu nại, phản ánh về dịch vụ
               - Yêu cầu đặt tour đoàn lớn hoặc dịch vụ đặc biệt
               - Vấn đề thanh toán, hoàn tiền
               - Mất đồ, tai nạn, tình huống khẩn cấp
               - Yêu cầu hỗ trợ người khuyết tật
               - Câu hỏi nghiệp vụ phức tạp mà bạn không có thông tin
               - Khi người dùng CHÍNH HỌ yêu cầu nói chuyện với nhân viên

            === NGÔN NGỮ ===
            Trả lời bằng ngôn ngữ mà người dùng sử dụng.
            """;

    private String getSystemPrompt(String userMessage) {
        // Fetch custom prompt from MongoDB, fallback to default
        List<KnowledgeDocument> dbPrompts = knowledgeRepository.findByKnowledgeType("SYSTEM_PROMPT");
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
                    .knowledgeType("SYSTEM_PROMPT")
                    .isActive(true)
                    .build();
            try {
                knowledgeRepository.save(newPromptDoc);
            } catch (Exception e) {
                log.error("Failed to seed system prompt to DB: {}", e.getMessage());
            }
        }

        StringBuilder prompt = new StringBuilder(currentPrompt);

        // VECTOR SEARCH: Use MongoDB Atlas Vector Search instead of in-memory
        // similarity
        List<Double> queryVector = embeddingService.getEmbedding(userMessage);
        List<KnowledgeDocument> knowledgeBase;

        if (queryVector == null || queryVector.isEmpty()) {
            // Fallback to keyword search if embedding fails
            knowledgeBase = knowledgeRepository.searchByKeyword(userMessage).stream()
                    .filter(KnowledgeDocument::isActive)
                    .limit(3)
                    .toList();
        } else {
            // Use MongoDB Atlas $vectorSearch (with min score threshold)
            knowledgeBase = vectorSearchRepository.vectorSearch(queryVector);
        }

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
                          "description": "Lấy danh sách các buổi (session) sắp diễn ra của một workshop cụ thể. Hãy dùng workshopTemplateId có được từ searchWorkshops.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "workshopId": {
                                "type": "string",
                                "description": "ID của workshop (workshopTemplateId - chuỗi UUID) lấy từ searchWorkshops."
                              }
                            },
                            "required": ["workshopId"]
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
                      },
                      {
                        "type": "function",
                        "function": {
                          "name": "addToCart",
                          "description": "Thực hiện lệnh thêm một workshop hoặc vé sự kiện vào giỏ hàng thật của người dùng. Hãy gọi lệnh này ngay khi người dùng đồng ý đặt chỗ/vé.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "sessionId": {
                                "type": "string",
                                "description": "ID của buổi workshop (Session ID - chuỗi UUID) được lấy từ getWorkshopSessions. KHÔNG dùng workshopTemplateId từ searchWorkshops."
                              },
                              "ticketCatalogId": {
                                "type": "string",
                                "description": "ID của loại vé sự kiện (Ticket Catalog ID - chuỗi UUID) được lấy từ getTicketPrices. KHÔNG dùng số thứ tự."
                              },
                              "quantity": {
                                "type": "integer",
                                "description": "Số lượng vé/chỗ cần đặt. Mặc định là 1.",
                                "default": 1
                              }
                            }
                          }
                        }
                      },
                      {
                        "type": "function",
                        "function": {
                          "name": "searchMapPoints",
                          "description": "Tìm kiếm các địa điểm tham quan, danh lam thắng cảnh (Point of Interest) trên bản đồ Ngũ Hành Sơn.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "keyword": {
                                "type": "string",
                                "description": "Tên địa điểm cần tìm (ví dụ: Động Huyền Không, Vọng Giang Đài)."
                              }
                            }
                          }
                        }
                      },
                      {
                        "type": "function",
                        "function": {
                          "name": "navigateToLocation",
                          "description": "Kích hoạt chức năng chỉ đường tới một địa điểm cụ thể trên ứng dụng di động.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "pointId": {
                                "type": "string",
                                "description": "ID của địa điểm (chuỗi UUID) lấy từ searchMapPoints."
                              },
                              "pointName": {
                                "type": "string",
                                "description": "Tên thân thiện của địa điểm để hiển thị trên nút bấm."
                              }
                            },
                            "required": ["pointId", "pointName"]
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
    private String executeFunctionCall(String functionName, JsonNode args, String senderId) {
        log.info("[AI] Executing function: {} with args: {} for user: {}", functionName, args, senderId);
        try {
            return switch (functionName) {
                case "searchWorkshops" -> executeSearchWorkshops(args);
                case "getWorkshopSessions" -> executeGetWorkshopSessions(args);
                case "searchEvents" -> executeSearchEvents(args);
                case "getTicketPrices" -> executeGetTicketPrices(args);
                case "searchBlogs" -> executeSearchBlogs(args);
                case "addToCart" -> executeAddToCart(args, senderId);
                case "searchMapPoints" -> executeSearchMapPoints(args);
                case "navigateToLocation" -> executeNavigateToLocation(args);
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
                .filter(w -> Boolean.TRUE.equals(w.getIsPublished()) && w.getDeletedAt() == null &&
                        (keyword.isEmpty() || w.getName().toLowerCase().contains(keyword.toLowerCase())))
                .limit(3)
                .map(w -> Map.of(
                        "workshopTemplateId", w.getId().toString(),
                        "name", w.getName(),
                        "description", w.getShortDescription() != null ? w.getShortDescription() : "",
                        "price", w.getDefaultPrice() != null ? w.getDefaultPrice().toString() + " VND" : "Liên hệ",
                        "status", w.getStatus().name(),
                        "rating", w.getAverageRating() != null ? w.getAverageRating().toString() : "0.0",
                        "imageUrl", (w.getWorkshopImages() != null && !w.getWorkshopImages().isEmpty())
                                ? w.getWorkshopImages().stream()
                                        .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                                        .findFirst()
                                        .map(fpt.project.NeoNHS.entity.WorkshopImage::getImageUrl)
                                        .orElse(w.getWorkshopImages().getFirst().getImageUrl())
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
        String workshopIdStr = args.get("workshopId").asText();
        UUID workshopId;
        try {
            workshopId = UUID.fromString(workshopIdStr);
        } catch (IllegalArgumentException e) {
            return "ERROR: Định dạng workshopId không hợp lệ.";
        }

        var templateOpt = workshopTemplateRepository.findById(workshopId);
        if (templateOpt.isEmpty())
            return "Không tìm thấy workshop có ID '" + workshopIdStr + "'.";

        var template = templateOpt.get();
        var sessions = workshopSessionRepository.findAll().stream()
                .filter(s -> s.getWorkshopTemplate().getId().equals(template.getId())
                        && s.getStartTime().isAfter(LocalDateTime.now())
                        && s.getStatus() == SessionStatus.SCHEDULED
                        && s.getDeletedAt() == null)
                .limit(5)
                .map(s -> {
                    Map<String, Object> sessionMap = new java.util.HashMap<>();
                    sessionMap.put("sessionId", s.getId().toString()); // Use simple 'sessionId' name
                    sessionMap.put("startTime", s.getStartTime().toString());
                    sessionMap.put("endTime", s.getEndTime().toString());
                    sessionMap.put("price", s.getPrice() != null ? s.getPrice().toString() + " VND"
                            : template.getDefaultPrice() + " VND");
                    sessionMap.put("availableSlots", (s.getMaxParticipants() != null ? s.getMaxParticipants() : 0)
                            - (s.getCurrentEnrolled() != null ? s.getCurrentEnrolled() : 0));
                    return sessionMap;
                })
                .toList();

        if (sessions.isEmpty())
            return "Workshop '" + template.getName() + "' hiện không có buổi nào sắp diễn ra.";

        try {
            // Return only the list of sessions for easier AI parsing
            return objectMapper.writeValueAsString(sessions);
        } catch (JsonProcessingException e) {
            return sessions.toString();
        }
    }

    private String executeSearchEvents(JsonNode args) {
        String keyword = args.has("keyword") ? args.get("keyword").asText("") : "";
        var events = eventRepository.findAll().stream()
                .filter(e -> e.getDeletedAt() == null
                        && (e.getStatus() == EventStatus.UPCOMING || e.getStatus() == EventStatus.ONGOING)
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
                    .map(tc -> {
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("ticketCatalogId", tc.getId().toString());
                        map.put("name", tc.getName());
                        map.put("customerType", tc.getCustomerType() != null ? tc.getCustomerType() : "Chung");
                        map.put("price", tc.getPrice().toString() + " VND");
                        map.put("description", tc.getDescription() != null ? tc.getDescription() : "");
                        return map;
                    })
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
                    .filter(e -> e.getDeletedAt() == null
                            && e.getName().toLowerCase().contains(eventName.toLowerCase()))
                    .toList();
            if (events.isEmpty())
                return "Không tìm thấy sự kiện '" + eventName + "'.";

            var event = events.getFirst();
            var catalogs = ticketCatalogRepository.findAll().stream()
                    .filter(tc -> tc.getEvent() != null && tc.getEvent().getId().equals(event.getId()))
                    .map(tc -> Map.of(
                            "ticketCatalogId", tc.getId().toString(),
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

    private String executeAddToCart(JsonNode args, String senderId) {
        log.info("[AI] Processing addToCart logic for user: {}", senderId);
        try {
            UUID userId = UUID.fromString(senderId);
            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng."));

            AddToCartRequest request = new AddToCartRequest();

            // Xử lý Ticket
            if (args.has("ticketCatalogId") && !args.get("ticketCatalogId").asText().isEmpty()) {
                String tcId = args.get("ticketCatalogId").asText();
                request.setTicketCatalogId(UUID.fromString(tcId));
            }

            // Xử lý Workshop
            if (args.has("sessionId") && !args.get("sessionId").asText().isEmpty()) {
                String wsIdStr = args.get("sessionId").asText();
                UUID wsId = UUID.fromString(wsIdStr);

                // KIỂM TRA: Nếu ID này là WorkshopTemplateId thay vì SessionId
                if (workshopTemplateRepository.existsById(wsId)) {
                    return "ERROR: '" + wsIdStr
                            + "' là ID của Workshop (TemplateId). Bạn PHẢI gọi 'getWorkshopSessions' với ID này để lấy danh sách các buổi học cụ thể (sessionId) trước khi đặt chỗ.";
                }

                request.setWorkshopSessionId(wsId);
            } else if (args.has("workshopSessionId") && !args.get("workshopSessionId").asText().isEmpty()) {
                String wsIdStr = args.get("workshopSessionId").asText();
                UUID wsId = UUID.fromString(wsIdStr);

                // Fallback check
                if (workshopTemplateRepository.existsById(wsId)) {
                    return "ERROR: '" + wsIdStr
                            + "' là ID của Workshop (TemplateId). Bạn PHẢI gọi 'getWorkshopSessions' với ID này để lấy danh sách các buổi học cụ thể (sessionId) trước khi đặt chỗ.";
                }
                request.setWorkshopSessionId(wsId);
            }

            request.setQuantity(args.has("quantity") ? args.get("quantity").asInt() : 1);

            // Gọi service thêm vào giỏ hàng
            cartService.addToCart(user.getEmail(), request);

            return "SUCCESS: Đã thêm vào giỏ hàng thành công.";
        } catch (IllegalArgumentException e) {
            return "ERROR: Định dạng ID không hợp lệ. Vui lòng sử dụng mã UUID từ các công cụ tra cứu.";
        } catch (Exception e) {
            log.error("[AI] Add to cart logic failed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
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

    private String executeSearchMapPoints(JsonNode args) {
        String keyword = args.has("keyword") ? args.get("keyword").asText("") : "";
        var points = pointRepository.findAll().stream()
                .filter(p -> p.getDeletedAt() == null &&
                        (keyword.isEmpty() || p.getName().toLowerCase().contains(keyword.toLowerCase())))
                .limit(5)
                .map(p -> Map.of(
                        "pointId", p.getId().toString(),
                        "name", p.getName(),
                        "description", p.getDescription() != null ? p.getDescription() : "",
                        "latitude", p.getLatitude().toString(),
                        "longitude", p.getLongitude().toString(),
                        "imageUrl", p.getThumbnailUrl() != null ? p.getThumbnailUrl() : ""))
                .toList();
        if (points.isEmpty())
            return "Không tìm thấy địa điểm nào" + (keyword.isEmpty() ? "." : " với từ khóa '" + keyword + "'.");
        try {
            return objectMapper.writeValueAsString(points);
        } catch (JsonProcessingException e) {
            return points.toString();
        }
    }

    private String executeNavigateToLocation(JsonNode args) {
        String pointName = args.get("pointName").asText();
        return "SUCCESS: Đã kích hoạt chỉ đường tới " + pointName
                + ". Hệ thống sẽ hiển thị nút chỉ đường ngay bây giờ.";
    }

    // ═══════════════════════════════════════════════════════════════════
    // Conversation History Builder
    // ═══════════════════════════════════════════════════════════════════
    private ArrayNode buildConversationHistory(String roomId, String userMessage) {
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
            if (content.length() > 500) {
                content = content.substring(0, 500);
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

                ArrayNode messages = buildConversationHistory(roomId, message);
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
                Map<String, Object> aiMetadata = (Map<String, Object>) result.get("metadata");

                System.out.println("AI Reply after processing: " + aiReplyText);

                // 5. Check for handover signal
                if (aiReplyText.contains("[TRANSFER_TO_HUMAN]")) {
                    String cleanText = aiReplyText.replaceAll("(?i)\\s*\\[TRANSFER_TO_HUMAN\\]\\s*", "").trim();
                    handleTransferToHuman(roomId, senderId, emitter, cleanText);
                    return;
                }

                // 6. Stream the response to client
                streamTextToClient(emitter, aiReplyText);

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

        // Tăng lên 5 lần để thoải mái cho các bước: Search -> GetDetail -> AddToCart ->
        // Confirm
        int maxIterations = 5;

        for (int i = 0; i < maxIterations; i++) {
            JsonNode choice = currentResponse.path("choices").path(0);
            JsonNode message = choice.path("message");

            if (message.has("tool_calls")) {
                JsonNode toolCalls = message.get("tool_calls");
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
                                return executeFunctionCall(functionName, functionArgs, senderId);
                            } catch (Exception e) {
                                status.setRollbackOnly();
                                return "ERROR: " + e.getMessage();
                            }
                        });
                    } catch (Exception e) {
                        functionResult = "ERROR: Lỗi hệ thống.";
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

    // private void saveAndBroadcastAiMessage(String roomId, String aiText) {
    // saveAndBroadcastAiMessage(roomId, aiText, "TEXT", null);
    // }

    private void saveAndBroadcastAiMessageWithMetadata(String roomId, String aiText, Map<String, Object> metadata) {
        saveAndBroadcastAiMessage(roomId, aiText, "TEXT", null);
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
