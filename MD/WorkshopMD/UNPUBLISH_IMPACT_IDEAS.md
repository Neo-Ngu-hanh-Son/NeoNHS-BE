# Xử lý khi chuyển Workshop Template từ Published → Unpublished (Private)

## Bối cảnh

Khi vendor **unpublish** một workshop template (toggle `isPublished` từ `true` → `false`), cần xác định cách xử lý đối với:

- Các **Workshop Sessions** đã được tạo từ template đó
- Các **Tourist đã đăng ký** (có Ticket/Order) vào các session đó
- Các **Tourist mới** muốn book session

---

## A. Xử lý các Workshop Sessions đã tồn tại

### A1: Giữ nguyên sessions ⭐ Khuyến nghị

- Sessions đã tạo **vẫn hoạt động bình thường**
- Chỉ ẩn template khỏi catalog public
- Sessions đang `SCHEDULED` vẫn diễn ra theo lịch
- Vendor vẫn quản lý sessions bình thường

| Ưu điểm | Nhược điểm |
|----------|------------|
| Không ảnh hưởng vendor/tourist đã book | Tourist có thể thấy session qua direct link (tùy phương án C) |
| Đơn giản, ít rủi ro | - |
| Giống cách các platform lớn xử lý | - |

### A2: Chặn tạo session mới

- Khi unpublish, vendor **không thể tạo thêm session mới** từ template đó
- Chỉ cho phép tạo session khi template đã publish lại
- Cần thêm check `isPublished` trong `createWorkshopSession()`

```java
// Thêm vào WorkshopSessionServiceImpl.createWorkshopSession()
if (!Boolean.TRUE.equals(template.getIsPublished())) {
    throw new BadRequestException("Cannot create sessions from unpublished templates. Please publish the template first.");
}
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Logic rõ ràng, nhất quán | Hạn chế vendor (có thể muốn tạo session trước rồi publish sau) |

### A3: Auto-cancel sessions chưa có enrollment

- Tự động cancel các `SCHEDULED` sessions có `currentEnrolled = 0`
- Giữ nguyên sessions đã có người đăng ký

```java
// Logic khi unpublish
List<WorkshopSession> emptyScheduledSessions = workshopSessionRepository
    .findByWorkshopTemplateIdAndStatusAndCurrentEnrolled(templateId, SessionStatus.SCHEDULED, 0);
emptyScheduledSessions.forEach(s -> s.setStatus(SessionStatus.CANCELLED));
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Clean up gọn gàng | Vendor có thể không muốn cancel |
| Tránh sessions "treo" | Cần confirm từ vendor trước khi cancel |

---

## B. Xử lý Tourist đã đăng ký (có Ticket/Order)

### B1: Giữ nguyên booking ⭐ Khuyến nghị

- Ticket status `ACTIVE` vẫn giữ nguyên
- Tourist vẫn tham gia session bình thường
- Order/Payment không bị ảnh hưởng

> **Tham khảo:** Đây là cách Airbnb, Eventbrite, Udemy xử lý — unlisting không ảnh hưởng existing bookings.

| Ưu điểm | Nhược điểm |
|----------|------------|
| Tourist không bị ảnh hưởng | - |
| Không cần refund logic | - |
| Tránh tranh chấp | - |

### B2: Thông báo nhưng không hủy

- Gửi notification cho tourist rằng template đã unpublish
- Session/ticket vẫn valid
- Tourist biết rằng template không còn public, nhưng booking vẫn được đảm bảo

| Ưu điểm | Nhược điểm |
|----------|------------|
| Tourist được thông báo minh bạch | Cần hệ thống notification |
| Booking vẫn giữ nguyên | Có thể gây lo lắng cho tourist |

---

## C. Xử lý Tourist mới muốn book

### C1: Ẩn khỏi catalog, giữ direct link ⭐ Khuyến nghị

- Template **không hiển thị** trên public search/catalog
- Nếu tourist có **link trực tiếp** đến session → vẫn có thể xem và book
- Hiện tại đã được xử lý qua tourist query filter `isPublished=true`

**Đã implement:**
- `getActiveWorkshopTemplates()` → filter `isPublished=true`
- `searchAndFilterActiveTemplates()` → filter `isPublished=true`
- `getActiveWorkshopTemplateById()` → check `isPublished`
- `getUpcomingSessionsByTemplateId()` → check template `isPublished`

| Ưu điểm | Nhược điểm |
|----------|------------|
| Linh hoạt — vendor có thể share link riêng | Tourist vẫn book được qua direct link |
| Giống "unlisted" của YouTube | Không hoàn toàn private |

### C2: Chặn hoàn toàn booking mới

- Ngoài ẩn khỏi catalog, **cũng chặn booking** qua direct link
- Cần thêm check `isPublished` ở booking/order flow

```java
// Thêm vào booking/order service
WorkshopTemplate template = session.getWorkshopTemplate();
if (!Boolean.TRUE.equals(template.getIsPublished())) {
    throw new BadRequestException("This workshop is not currently available for booking");
}
```

**Cần sửa thêm ở:**
- Order creation flow
- Cart add flow
- Session detail public endpoint (trả 404 nếu template unpublished)

| Ưu điểm | Nhược điểm |
|----------|------------|
| Kiểm soát chặt chẽ nhất | Phức tạp hơn |
| Vendor chắc chắn không có booking mới | Mất tính năng "unlisted" |

---

## Đề xuất tổng thể

Áp dụng kết hợp: **A1 + B1 + C1** (hoặc **C2** nếu muốn strict hơn)

```
Unpublish template:
├── Sessions đã tạo → GIỮ NGUYÊN (A1)
├── Tourist đã book → GIỮ NGUYÊN booking (B1)
├── Public catalog → ẨN template (C1 - đã implement)
└── Direct link → Tùy chọn C1 (cho phép) hoặc C2 (chặn)
```

### Giai đoạn triển khai đề xuất

1. **Phase 1 (MVP):** A1 + B1 + C1 — Đơn giản, đã implement phần lớn
2. **Phase 2:** Thêm A2 (chặn tạo session mới khi unpublish)
3. **Phase 3:** Thêm C2 + notification system
