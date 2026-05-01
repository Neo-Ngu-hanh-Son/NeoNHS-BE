package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.project.NeoNHS.dto.request.cart.AddToCartRequest;
import fpt.project.NeoNHS.enums.BlogStatus;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.enums.SessionStatus;
import fpt.project.NeoNHS.repository.BlogRepository;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.repository.TicketCatalogRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.WorkshopSessionRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.service.AiFunctionCallingService;
import fpt.project.NeoNHS.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiFunctionCallingServiceImpl implements AiFunctionCallingService {

    private final ObjectMapper objectMapper;
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final EventRepository eventRepository;
    private final TicketCatalogRepository ticketCatalogRepository;
    private final BlogRepository blogRepository;
    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    // ═══════════════════════════════════════════════════════════════════
    // Tool Declarations (OpenAI Function Calling)
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public JsonNode buildToolDeclarations() {
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
                                "description": "Từ khóa tìm kiếm (tên workshop). Bạn PHẢI dịch sang tiếng Việt trước khi tìm. Để trống nếu muốn lấy tất cả."
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
                                "description": "Từ khóa tìm kiếm (tên sự kiện). Bạn PHẢI dịch sang tiếng Việt trước khi tìm. Để trống nếu muốn lấy tất cả."
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
                                "description": "Tên sự kiện cần tra giá vé (bằng tiếng Việt). Để trống nếu muốn tra giá vé tham quan chung."
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
                                "description": "Từ khóa tìm kiếm (tiêu đề). Bạn PHẢI dịch sang tiếng Việt trước khi tìm. Để trống lấy bài mới nhất."
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
                          "description": "BẮT BUỘC SỬ DỤNG hàm này để lấy thông tin chi tiết khi người dùng hỏi về một địa điểm tham quan, hang động, chùa chiền cụ thể (ví dụ: Động Huyền Không, Tháp Xá Lợi, Vọng Giang Đài...). Hàm này chứa dữ liệu mô tả và hình ảnh.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "keyword": {
                                "type": "string",
                                "description": "Tên địa điểm cần tìm. Bạn PHẢI dịch sang tiếng Việt trước khi tìm (ví dụ: Động Huyền Không, Vọng Giang Đài)."
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
    @Override
    public String executeFunctionCall(String functionName, JsonNode args, String senderId,
            Map<String, Object> metadata) {
        log.info("[AI] Executing function: {} with args: {} for user: {}", functionName, args, senderId);
        try {
            return switch (functionName) {
                case "searchWorkshops" -> executeSearchWorkshops(args, metadata);
                case "getWorkshopSessions" -> executeGetWorkshopSessions(args);
                case "searchEvents" -> executeSearchEvents(args, metadata);
                case "getTicketPrices" -> executeGetTicketPrices(args);
                case "searchBlogs" -> executeSearchBlogs(args, metadata);
                case "addToCart" -> executeAddToCart(args, senderId);
                case "searchMapPoints" -> executeSearchMapPoints(args, metadata);
                case "navigateToLocation" -> executeNavigateToLocation(args, metadata);
                default -> "Không tìm thấy công cụ: " + functionName;
            };
        } catch (Exception e) {
            log.error("[AI] Function execution error for {}: {}", functionName, e.getMessage());
            return "Lỗi khi tra cứu dữ liệu: " + e.getMessage();
        }
    }

    private String executeSearchWorkshops(JsonNode args, Map<String, Object> metadata) {
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

        if (!workshops.isEmpty()) {
            List<Map<String, Object>> attachedCards = (List<Map<String, Object>>) metadata
                    .computeIfAbsent("attachedCards", k -> new java.util.ArrayList<>());
            for (var w : workshops) {
                Map<String, Object> card = new java.util.HashMap<>();
                card.put("id", w.get("workshopTemplateId"));
                card.put("type", "workshop");
                card.put("title", w.get("name"));
                card.put("imageUrl", w.get("imageUrl"));
                card.put("price", w.get("price"));
                card.put("rating", w.get("rating"));
                attachedCards.add(card);
            }
        }

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

    private String executeSearchEvents(JsonNode args, Map<String, Object> metadata) {
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
                        "ticketRequired", e.getIsTicketRequired() != null ? e.getIsTicketRequired() : false,
                        "price", e.getPrice() != null ? e.getPrice().toString() + " VND" : "Miễn phí",
                        "imageUrl", (e.getEventImages() != null && !e.getEventImages().isEmpty())
                                ? e.getEventImages().get(0).getImageUrl()
                                : ""))
                .toList();

        if (!events.isEmpty()) {
            List<Map<String, Object>> attachedCards = (List<Map<String, Object>>) metadata
                    .computeIfAbsent("attachedCards", k -> new java.util.ArrayList<>());
            for (var e : events) {
                Map<String, Object> card = new java.util.HashMap<>();
                card.put("id", e.get("id"));
                card.put("type", "event");
                card.put("title", e.get("name"));
                card.put("imageUrl", e.get("imageUrl"));
                card.put("price", e.get("price"));
                card.put("startTime", e.get("startTime"));
                attachedCards.add(card);
            }
        }

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

    private String executeSearchBlogs(JsonNode args, Map<String, Object> metadata) {
        String keyword = args.has("keyword") ? args.get("keyword").asText("") : "";
        var blogs = blogRepository.findAll().stream()
                .filter(b -> b.getStatus() == BlogStatus.PUBLISHED &&
                        (keyword.isEmpty() || b.getTitle().toLowerCase().contains(keyword.toLowerCase())))
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .limit(5)
                .map(b -> Map.of(
                        "id", b.getId().toString(),
                        "title", b.getTitle(),
                        "summary", b.getSummary() != null ? b.getSummary() : "",
                        "imageUrl", b.getThumbnailUrl() != null ? b.getThumbnailUrl() : "",
                        "author", b.getUser() != null ? b.getUser().getFullname() : "Admin",
                        "publishedAt", b.getPublishedAt() != null ? b.getPublishedAt().toString() : ""))
                .toList();

        if (!blogs.isEmpty()) {
            List<Map<String, Object>> attachedCards = (List<Map<String, Object>>) metadata
                    .computeIfAbsent("attachedCards", k -> new java.util.ArrayList<>());
            for (var b : blogs) {
                Map<String, Object> card = new java.util.HashMap<>();
                card.put("id", b.get("id"));
                card.put("type", "blog");
                card.put("title", b.get("title"));
                card.put("imageUrl", b.get("imageUrl"));
                card.put("author", b.get("author"));
                attachedCards.add(card);
            }
        }

        if (blogs.isEmpty())
            return "Không tìm thấy bài viết nào" + (keyword.isEmpty() ? "." : " với từ khóa '" + keyword + "'.");
        try {
            return objectMapper.writeValueAsString(blogs);
        } catch (JsonProcessingException e) {
            return blogs.toString();
        }
    }

    private String executeSearchMapPoints(JsonNode args, Map<String, Object> metadata) {
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

        // Intentionally do NOT attach point cards for generic map-point queries.
        // Point cards are only attached in explicit navigateToLocation flow.

        if (points.isEmpty())
            return "Không tìm thấy địa điểm nào" + (keyword.isEmpty() ? "." : " với từ khóa '" + keyword + "'.");
        try {
            return objectMapper.writeValueAsString(points);
        } catch (JsonProcessingException e) {
            return points.toString();
        }
    }

    private String executeNavigateToLocation(JsonNode args, Map<String, Object> metadata) {
        String pointName = args.get("pointName").asText();
        String pointId = args.get("pointId").asText();

        // Attach a single deterministic point card only for explicit direction
        // requests.
        try {
            UUID pid = UUID.fromString(pointId);
            pointRepository.findById(pid).ifPresent(point -> {
                List<Map<String, Object>> attachedCards = (List<Map<String, Object>>) metadata
                        .computeIfAbsent("attachedCards", k -> new java.util.ArrayList<>());
                Map<String, Object> card = new java.util.HashMap<>();
                card.put("id", point.getId().toString());
                card.put("type", "point");
                card.put("title", point.getName());
                card.put("imageUrl", point.getThumbnailUrl() != null ? point.getThumbnailUrl() : "");
                attachedCards.add(card);
            });
        } catch (Exception ignored) {
        }

        return "SUCCESS: Đã kích hoạt chỉ đường tới " + pointName
                + ". Hệ thống sẽ hiển thị nút chỉ đường ngay bây giờ.";
    }
}
