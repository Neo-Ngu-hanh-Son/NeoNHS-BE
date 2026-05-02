package fpt.project.NeoNHS.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.project.NeoNHS.dto.request.cart.AddToCartRequest;
import fpt.project.NeoNHS.enums.BlogStatus;
import fpt.project.NeoNHS.enums.EventStatus;
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
                           "description": "Lấy danh sách các buổi học sắp tới của một workshop để hiển thị cho người dùng chọn. Trả về thông tin thời gian và số chỗ trống.",
                           "parameters": {
                             "type": "object",
                             "properties": {
                               "workshopTemplateId": {
                                 "type": "string",
                                 "description": "UUID của workshop template (lấy từ bảng tra cứu hoặc searchWorkshops)."
                               }
                             },
                             "required": ["workshopTemplateId"]
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
                          "description": "Lấy thông tin giá vé tham quan (vé vào cổng) hoặc vé của một sự kiện cụ thể. Trả về 'BOOKING_ID_UUID_ONLY' để dùng cho addEventTicketToCart.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "eventId": {
                                "type": "string",
                                "description": "UUID của sự kiện lấy từ searchEvents. Ưu tiên dùng ID này."
                              },
                              "eventName": {
                                "type": "string",
                                "description": "Tên sự kiện (tiếng Việt). Chỉ dùng nếu không có eventId."
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
                           "name": "addWorkshopToCart",
                           "description": "PHASE 2: Thêm workshop vào giỏ hàng sau khi user đã chọn một buổi cụ thể. Sử dụng ID của workshop gốc và số thứ tự của buổi học.",
                           "parameters": {
                             "type": "object",
                             "properties": {
                               "workshopTemplateId": {
                                 "type": "string",
                                 "description": "UUID của workshop gốc (ví dụ: 53762505-...). Lấy từ ID Table hoặc kết quả searchWorkshops."
                               },
                               "sessionIndex": {
                                 "type": "integer",
                                 "description": "Chỉ số dựa trên 0 (zero-based index) của buổi học mà user chọn. Ví dụ: User chọn 'Buổi 1' thì truyền 0, 'Buổi 2' thì truyền 1."
                               },
                               "quantity": {
                                 "type": "integer",
                                 "description": "Số lượng chỗ cần đặt. Mặc định là 1.",
                                 "default": 1
                               }
                             },
                             "required": ["workshopTemplateId", "sessionIndex"]
                           }
                         }
                       },
                      {
                        "type": "function",
                        "function": {
                          "name": "addEventTicketToCart",
                          "description": "Thêm vé vào giỏ hàng. CHỈ gọi ở PHASE 2 — sau khi user đã chọn loại vé cụ thể.",
                          "parameters": {
                            "type": "object",
                            "properties": {
                              "eventId": {
                                "type": "string",
                                "description": "ID của sự kiện (Root ID) lấy từ bảng tra cứu hoặc searchEvents. Để trống nếu là vé tham quan chung."
                              },
                              "ticketIndex": {
                                "type": "integer",
                                "description": "Số thứ tự của loại vé (bắt đầu từ 0) trong danh sách vừa hiển thị ở Phase 1."
                              },
                              "quantity": {
                                "type": "integer",
                                "description": "Số lượng vé. Mặc định là 1.",
                                "default": 1
                              }
                            },
                            "required": ["ticketIndex"]
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
                case "addWorkshopToCart" -> executeAddWorkshopToCart(args, senderId);
                case "addEventTicketToCart" -> executeAddEventTicketToCart(args, senderId);
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
        String workshopIdStr = args.has("workshopTemplateId") ? args.get("workshopTemplateId").asText()
                : (args.has("workshopId") ? args.get("workshopId").asText() : "");

        if (workshopIdStr.isEmpty()) {
            return "ERROR: Thiếu workshopTemplateId.";
        }

        // ── Try to parse as UUID first ────────────────────────────────────────
        UUID workshopId = null;
        try {
            workshopId = UUID.fromString(workshopIdStr);
        } catch (IllegalArgumentException e) {
            // Not a UUID — AI passed a number index or workshop name from conversation
            // history. Attempt a name-based fallback so the user still gets results.
            log.warn("[AI] getWorkshopSessions received non-UUID workshopId='{}'. "
                    + "Falling back to keyword search by name.", workshopIdStr);
            return executeGetWorkshopSessionsByName(workshopIdStr);
        }

        var templateOpt = workshopTemplateRepository.findById(workshopId);
        if (templateOpt.isEmpty())
            return "Không tìm thấy workshop có ID '" + workshopIdStr + "'."
                    + " Hãy gọi searchWorkshops để lấy danh sách và UUID chính xác.";

        return buildSessionsResponse(templateOpt.get());
    }

    /**
     * Fallback: find workshop by name keyword when the AI passed a non-UUID workshopId
     * (e.g., "1", "2", or the workshop name itself from conversation history).
     */
    private String executeGetWorkshopSessionsByName(String keyword) {
        // If the keyword looks like a small integer (e.g. "1", "2"), it's an index —
        // we cannot reliably map it to a workshop, so instruct AI to re-search.
        if (keyword.matches("\\d{1,3}")) {
            return "ERROR: workshopId '" + keyword + "' không hợp lệ — đây là số thứ tự, không phải UUID. "
                    + "Bạn PHẢI gọi searchWorkshops trước để lấy workshopTemplateId (UUID) của workshop.";
        }

        // Otherwise treat it as a name/keyword search
        var matched = workshopTemplateRepository.findAll().stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsPublished())
                        && w.getDeletedAt() == null
                        && w.getName().toLowerCase().contains(keyword.toLowerCase()))
                .findFirst();

        if (matched.isEmpty()) {
            return "Không tìm thấy workshop nào với từ khóa '" + keyword + "'. "
                    + "Hãy gọi searchWorkshops để lấy danh sách workshop và workshopTemplateId chính xác.";
        }

        log.info("[AI] getWorkshopSessions name-fallback: '{}' → matched workshop '{}' (id={})",
                keyword, matched.get().getName(), matched.get().getId());
        return buildSessionsResponse(matched.get());
    }

    /** Shared helper: build the sessions JSON response for a resolved WorkshopTemplate. */
    private String buildSessionsResponse(fpt.project.NeoNHS.entity.WorkshopTemplate template) {
        var rawSessions = workshopSessionRepository
                .findUpcomingByTemplateId(template.getId(), LocalDateTime.now());

        log.info("[AI] findUpcomingByTemplateId for template='{}' (id={}) returned {} sessions.",
                template.getName(), template.getId(), rawSessions.size());

        var sessions = rawSessions.stream()
                .limit(5)
                .map(s -> {
                    log.info("[AI] Session available: id={}, startTime={}, status={}, deletedAt={}",
                            s.getId(), s.getStartTime(), s.getStatus(), s.getDeletedAt());
                    Map<String, Object> sessionMap = new java.util.HashMap<>();
                    sessionMap.put("SESSION_ID_UUID_ONLY", s.getId().toString());
                    sessionMap.put("workshopName", template.getName());
                    sessionMap.put("startTime", s.getStartTime().toString());
                    sessionMap.put("endTime", s.getEndTime().toString());
                    sessionMap.put("price", s.getPrice() != null
                            ? s.getPrice().toString() + " VND"
                            : template.getDefaultPrice() + " VND");
                    sessionMap.put("availableSlots",
                            (s.getMaxParticipants() != null ? s.getMaxParticipants() : 0)
                            - (s.getCurrentEnrolled() != null ? s.getCurrentEnrolled() : 0));
                    return sessionMap;
                })
                .toList();

        if (sessions.isEmpty())
            return "Workshop '" + template.getName() + "' hiện không có buổi nào sắp diễn ra.";

        try {
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
                        "eventId", e.getId().toString(),
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
                card.put("id", e.get("eventId"));
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
        String eventIdStr = args.has("eventId") ? args.get("eventId").asText("") : "";
        String eventName = args.has("eventName") ? args.get("eventName").asText("") : "";

        fpt.project.NeoNHS.entity.Event event = null;

        if (!eventIdStr.isEmpty()) {
            try {
                UUID eventId = UUID.fromString(eventIdStr);
                event = eventRepository.findById(eventId).orElse(null);
            } catch (Exception ignored) {
            }
        }

        if (event == null && !eventName.isEmpty()) {
            event = eventRepository.findAll().stream()
                    .filter(e -> e.getDeletedAt() == null
                            && e.getName().toLowerCase().contains(eventName.toLowerCase()))
                    .findFirst()
                    .orElse(null);
        }

        if (event == null && eventIdStr.isEmpty() && eventName.isEmpty()) {
            // General attraction tickets
            var catalogs = ticketCatalogRepository.findAll().stream()
                    .filter(tc -> tc.getAttraction() != null)
                    .limit(20)
                    .map(tc -> {
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("BOOKING_ID_UUID_ONLY", tc.getId().toString());
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
        } else if (event != null) {
            final UUID finalEventId = event.getId();
            var catalogs = ticketCatalogRepository.findAll().stream()
                    .filter(tc -> tc.getEvent() != null && tc.getEvent().getId().equals(finalEventId))
                    .map(tc -> Map.of(
                            "BOOKING_ID_UUID_ONLY", tc.getId().toString(),
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
        } else {
            return "Không tìm thấy sự kiện '" + (eventIdStr.isEmpty() ? eventName : eventIdStr) + "'.";
        }
    }

    private String executeAddWorkshopToCart(JsonNode args, String senderId) {
        log.info("[AI] Processing addWorkshopToCart for user: {}", senderId);
        try {
            String templateIdStr = args.has("workshopTemplateId") ? args.get("workshopTemplateId").asText() : "";
            int index = args.has("sessionIndex") ? args.get("sessionIndex").asInt() : 0;
            int quantity = args.has("quantity") ? args.get("quantity").asInt() : 1;

            if (templateIdStr.isEmpty()) return "ERROR: Thiếu workshopTemplateId.";

            UUID templateId = UUID.fromString(templateIdStr);
            var sessions = workshopSessionRepository.findUpcomingByTemplateId(templateId, LocalDateTime.now());

            if (index < 0 || index >= sessions.size()) {
                return "ERROR: Số thứ tự buổi học '" + index + "' không hợp lệ. Workshop này chỉ có " + sessions.size() + " buổi sắp tới.";
            }

            UUID sessionId = sessions.get(index).getId();
            AddToCartRequest request = new AddToCartRequest();
            request.setWorkshopSessionId(sessionId);
            request.setQuantity(quantity);

            UUID userId = UUID.fromString(senderId);
            var user = userRepository.findById(userId).orElseThrow();
            cartService.addToCart(user.getEmail(), request);

            return "SUCCESS: Đã thêm workshop vào giỏ hàng thành công.";
        } catch (Exception e) {
            log.error("[AI] addWorkshopToCart failed: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    private String executeAddEventTicketToCart(JsonNode args, String senderId) {
        log.info("[AI] Processing addEventTicketToCart for user: {}", senderId);
        try {
            String eventIdStr = args.has("eventId") ? args.get("eventId").asText() : "";
            int index = args.has("ticketIndex") ? args.get("ticketIndex").asInt() : 0;
            int quantity = args.has("quantity") ? args.get("quantity").asInt() : 1;

            List<fpt.project.NeoNHS.entity.TicketCatalog> catalogs;
            if (eventIdStr.isEmpty()) {
                catalogs = ticketCatalogRepository.findAll().stream()
                        .filter(tc -> tc.getAttraction() != null)
                        .toList();
            } else {
                UUID eventId = UUID.fromString(eventIdStr);
                catalogs = ticketCatalogRepository.findAll().stream()
                        .filter(tc -> tc.getEvent() != null && tc.getEvent().getId().equals(eventId))
                        .toList();
            }

            if (index < 0 || index >= catalogs.size()) {
                return "ERROR: Số thứ tự loại vé '" + index + "' không hợp lệ.";
            }

            UUID ticketCatalogId = catalogs.get(index).getId();
            AddToCartRequest request = new AddToCartRequest();
            request.setTicketCatalogId(ticketCatalogId);
            request.setQuantity(quantity);

            UUID userId = UUID.fromString(senderId);
            var user = userRepository.findById(userId).orElseThrow();
            cartService.addToCart(user.getEmail(), request);

            return "SUCCESS: Đã thêm vé vào giỏ hàng thành công.";
        } catch (Exception e) {
            log.error("[AI] addEventTicketToCart failed: {}", e.getMessage());
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
