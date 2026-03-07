# Chính sách Edit/Delete Workshop Template & Session

## Bối cảnh

Hiện tại:
- **Workshop Template:** Chỉ cho edit khi status **không phải** `ACTIVE` hoặc `PENDING`
- **Workshop Session:** Chỉ cho edit/delete khi status là `SCHEDULED`
- Session có enrollment → chặn delete, phải cancel

Câu hỏi: Các chính sách này đã hợp lý chưa, hay cần điều chỉnh?

---

## A. Edit Workshop Template khi ACTIVE

### A1: Cho phép edit một số field ⭐ Khuyến nghị

Phân loại fields thành 2 nhóm:

**Fields "nhẹ" - Cho phép edit tự do:**
| Field | Lý do |
|-------|-------|
| `shortDescription` | Sửa typo, cải thiện mô tả |
| `fullDescription` | Bổ sung thông tin chi tiết |
| `images` | Thêm/đổi hình ảnh |

**Fields "nặng" - Chặn edit khi ACTIVE:**
| Field | Lý do chặn |
|-------|------------|
| `name` | Ảnh hưởng SEO, link share, nhận diện |
| `defaultPrice` | Ảnh hưởng sessions mới tạo |
| `minParticipants` | Ảnh hưởng sessions đã tạo |
| `maxParticipants` | Ảnh hưởng sessions đã tạo |
| `estimatedDuration` | Tourist đã plan thời gian |
| `tags` | Ảnh hưởng search/filter |

```java
// Logic đề xuất
if (template.getStatus() == WorkshopStatus.ACTIVE) {
    // Chỉ cho edit fields nhẹ
    if (request.getName() != null || request.getDefaultPrice() != null 
        || request.getMinParticipants() != null || request.getMaxParticipants() != null
        || request.getEstimatedDuration() != null || request.getTagIds() != null) {
        throw new BadRequestException(
            "Cannot update core fields of an ACTIVE template. " +
            "Only description and images can be edited. " +
            "To change other fields, please unpublish the template first."
        );
    }
    // Cho phép update description và images
}
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Linh hoạt — vendor sửa typo dễ dàng | Logic phức tạp hơn |
| Không ảnh hưởng trải nghiệm tourist | Cần phân biệt field nào cho edit |
| Không cần re-approval cho thay đổi nhỏ | - |

### A2: Giữ nguyên như hiện tại

- Chặn hoàn toàn edit khi `ACTIVE`
- Vendor muốn sửa phải: Unpublish → chuyển về `DRAFT` (?) → edit → submit lại

| Ưu điểm | Nhược điểm |
|----------|------------|
| An toàn, đơn giản | Quá cứng nhắc |
| Dễ implement | Vendor sửa lỗi chính tả phải qua cả flow approval |
| - | Ảnh hưởng trải nghiệm vendor |

### A3: Edit tự do + Versioning

- Cho edit mọi field khi ACTIVE
- Khi edit field quan trọng → tạo **version mới** của template
- Sessions cũ vẫn link template version cũ
- Sessions mới link template version mới

```
Template v1 (price: 100k) → Session A, Session B
Template v2 (price: 120k) → Session C, Session D (mới tạo)
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Rất linh hoạt | Phức tạp, cần refactor database lớn |
| Giữ được lịch sử thay đổi | Tốn thời gian phát triển |
| Session cũ không bị ảnh hưởng | Over-engineering cho MVP |

### A4: Edit tự do + Re-approval cho field quan trọng

- Cho edit mọi field khi ACTIVE
- Thay đổi field nhẹ → apply ngay
- Thay đổi field nặng → status chuyển sang `PENDING` (cần admin re-approve)

```
Edit description → Apply ngay, vẫn ACTIVE
Edit price → Status → PENDING → Admin review → ACTIVE
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Cân bằng giữa linh hoạt và kiểm soát | Cần logic xác định field nào cần re-approve |
| Admin vẫn kiểm soát nội dung | Template bị "down" trong thời gian chờ review |
| - | Phức tạp cho cả vendor và admin |

---

## B. Edit Workshop Session khi đã có Enrollment

### B1: Giữ nguyên + Thêm notification ⭐ Khuyến nghị

Hiện tại đã xử lý tốt:
- Cho edit `startTime`, `endTime`, `price`, `maxParticipants`
- Chặn giảm `maxParticipants` dưới `currentEnrolled`
- Chặn edit khi status không phải `SCHEDULED`

**Bổ sung nên có:**

```java
// Khi đổi thời gian và session có enrollment
if (session.getCurrentEnrolled() > 0) {
    boolean timeChanged = false;
    if (request.getStartTime() != null && !request.getStartTime().equals(session.getStartTime())) {
        timeChanged = true;
    }
    if (request.getEndTime() != null && !request.getEndTime().equals(session.getEndTime())) {
        timeChanged = true;
    }
    if (timeChanged) {
        // TODO: Gửi notification cho tất cả tourist đã book
        // notificationService.notifySessionTimeChanged(session);
    }
}
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Tourist được thông báo thay đổi | Cần notification system |
| Vendor vẫn linh hoạt edit | - |

### B2: Chặn edit thời gian nếu có enrollment

- Nếu `currentEnrolled > 0`:
  - **Cho phép:** Tăng `maxParticipants`, thay đổi `price` (chỉ áp dụng cho booking mới)
  - **Chặn:** Đổi `startTime`, `endTime`

```java
if (session.getCurrentEnrolled() > 0) {
    if (request.getStartTime() != null || request.getEndTime() != null) {
        throw new BadRequestException(
            "Cannot change session time when there are enrollments. " + 
            "Please cancel the session and create a new one instead."
        );
    }
}
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Tourist yên tâm về thời gian | Vendor không linh hoạt |
| Đơn giản, rõ ràng | Phải cancel + tạo mới nếu cần đổi giờ |

### B3: Edit + Tourist xác nhận

- Nếu đổi thời gian, tourist được thông báo
- Tourist có **thời hạn** (ví dụ 48h) để xác nhận hoặc hủy
- Nếu tourist hủy → auto refund

```
Vendor đổi giờ → Notification gửi tourist
  → Tourist confirm → Giữ booking
  → Tourist reject → Auto refund + giảm currentEnrolled
  → Hết hạn → Auto confirm
```

| Ưu điểm | Nhược điểm |
|----------|------------|
| Fair cho tourist | Rất phức tạp |
| Transparent | Cần workflow engine |
| - | Quá mức cho MVP |

---

## C. Delete Workshop Session khi đã có Enrollment

### Hiện tại (đã tốt):
- Có enrollment → ❌ Không cho delete → Phải **cancel** thay vì delete
- Cancel = soft delete (giữ record, status → `CANCELLED`)
- Không enrollment → ✅ Cho delete (hard delete)

### Bổ sung đề xuất:

### C1: Cancel + Auto Refund ⭐ Nên có

```java
@Transactional
public WorkshopSessionResponse cancelWorkshopSession(String email, UUID id) {
    // ... existing logic ...
    session.setStatus(SessionStatus.CANCELLED);
    
    // Thêm: Auto refund cho tất cả tourist
    if (session.getCurrentEnrolled() > 0) {
        // refundService.processRefundForCancelledSession(session);
        // ticketService.expireAllTicketsForSession(session);
    }
    
    return mapToResponse(workshopSessionRepository.save(session));
}
```

### C2: Cancel + Grace Period

- Cho thời gian ân hạn (24-48h) trước khi cancel thực sự
- Tourist được thông báo và có thể chuyển sang session khác

```
Vendor cancel → Status = PENDING_CANCELLATION → 48h → Status = CANCELLED
                ↓
          Notification → Tourist → Chuyển session khác hoặc refund
```

### C3: Cancel + Offer chuyển session

- Khi cancel, suggest tourist chuyển sang session khác cùng template (nếu có)

```
Session A (cancelled) → Tourist nhận notification
  → "Session B (cùng template, 15/03) có sẵn, bạn muốn chuyển không?"
  → Đồng ý → Chuyển ticket sang Session B
  → Từ chối → Refund
```

---

## D. Delete Workshop Template khi ACTIVE

### Hiện tại:
- Chặn hoàn toàn delete khi `ACTIVE`
- Chỉ cho delete khi `DRAFT`, `PENDING`, `REJECTED`

### D1: Cho phép nếu không có session nào

```java
if (template.getStatus() == WorkshopStatus.ACTIVE) {
    long sessionCount = workshopSessionRepository.countByWorkshopTemplateId(template.getId());
    if (sessionCount > 0) {
        throw new BadRequestException("Cannot delete ACTIVE template with existing sessions. Please deactivate or archive instead.");
    }
    // Cho phép delete nếu chưa có session nào
}
```

### D2: Soft Delete — Deactivate/Archive ⭐ Khuyến nghị

Thêm status mới hoặc sử dụng `isPublished = false` kết hợp mechanism khác:

```
ACTIVE template → "Deactivate" → Status = INACTIVE (hoặc ARCHIVED)
  ├── SCHEDULED sessions (no enrollment) → Auto CANCELLED
  ├── SCHEDULED sessions (has enrollment) → Giữ nguyên, chạy xong mới archive
  └── Template ẩn khỏi mọi view (kể cả vendor tạo session mới)
```

---

## Tổng kết đề xuất theo Phase

### Phase 1 (MVP) — Nên làm ngay:
- **Template edit:** A1 (cho edit description/images khi ACTIVE)
- **Session edit:** B1 (giữ nguyên + log thay đổi)
- **Session delete:** Giữ nguyên (cancel thay vì delete)
- **Template delete:** Giữ nguyên + D1

### Phase 2 — Sau khi có notification:
- **Session edit:** B1 + notification khi đổi giờ
- **Session cancel:** C1 (auto refund)
- **Template:** D2 (deactivate/archive)

### Phase 3 — Nâng cao:
- **Session cancel:** C3 (offer chuyển session)
- **Template edit:** A4 (re-approval cho field quan trọng)
