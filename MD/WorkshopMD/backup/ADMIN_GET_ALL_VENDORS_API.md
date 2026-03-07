# API Guide: Approve & Reject Workshop Template (Admin)

> **Base URL:** `/api/admin/vendors`  
> **Authentication:** Bearer JWT Token (Admin role required)  
> **Controller:** `AdminVendorManagementController`

---

## Tổng quan luồng duyệt template

```
Vendor tạo template (DRAFT)
        ↓
Vendor submit (PENDING)
        ↓
    Admin review
   ↙           ↘
APPROVE        REJECT
(ACTIVE)     (REJECTED)
                ↓
         Vendor sửa lại
                ↓
         Vendor resubmit (PENDING)
```

---

## 1. Approve Workshop Template

### Endpoint
```
POST /api/admin/vendors/workshop-templates/{id}/approve
```

### Mô tả
Admin duyệt một workshop template đang ở trạng thái **PENDING**, chuyển sang **ACTIVE**.

### Yêu cầu
| Điều kiện | Chi tiết |
|---|---|
| Role | `ADMIN` |
| Trạng thái template | Phải là `PENDING` |
| Request Body | Không cần |

### Request

**Headers:**
```
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json
```

**Path Variable:**
```
id: UUID của workshop template (VD: 550e8400-e29b-41d4-a716-446655440000)
```

**Body:** *(Không cần)*

### Response

**200 OK – Thành công:**
```json
{
  "status": 200,
  "message": "Workshop template approved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Lớp học làm bánh cơ bản",
    "shortDescription": "Học làm bánh từ đầu trong 3 giờ",
    "fullDescription": "...",
    "estimatedDuration": 180,
    "defaultPrice": 500000.00,
    "minParticipants": 5,
    "maxParticipants": 20,
    "status": "ACTIVE",
    "averageRating": 0.00,
    "totalRatings": 0,
    "vendorId": "...",
    "vendorName": "Workshop Pro",
    "createdAt": "2026-02-20T10:00:00",
    "updatedAt": "2026-03-01T09:30:00",
    "adminNote": null,
    "reviewedBy": "admin-uuid-here",
    "reviewedAt": "2026-03-01T09:30:00",
    "images": [...],
    "tags": [...]
  }
}
```

**400 Bad Request – Template không ở trạng thái PENDING:**
```json
{
  "status": 400,
  "message": "Only templates with 'Pending' status can be approved. Current status: DRAFT"
}
```

**403 Forbidden – Không phải Admin:**
```json
{
  "status": 403,
  "message": "Access Denied"
}
```

**404 Not Found – Không tìm thấy template:**
```json
{
  "status": 404,
  "message": "WorkshopTemplate not found with id: 550e8400-..."
}
```

### Ví dụ cURL
```bash
curl -X POST \
  http://localhost:8080/api/admin/vendors/workshop-templates/550e8400-e29b-41d4-a716-446655440000/approve \
  -H "Authorization: Bearer <admin_jwt_token>"
```

---

## 2. Reject Workshop Template

### Endpoint
```
POST /api/admin/vendors/workshop-templates/{id}/reject
```

### Mô tả
Admin từ chối một workshop template đang ở trạng thái **PENDING**, chuyển sang **REJECTED**, kèm ghi chú lý do để vendor có thể chỉnh sửa và nộp lại.

### Yêu cầu
| Điều kiện | Chi tiết |
|---|---|
| Role | `ADMIN` |
| Trạng thái template | Phải là `PENDING` |
| Request Body | Bắt buộc có `adminNote` |

### Request

**Headers:**
```
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json
```

**Path Variable:**
```
id: UUID của workshop template
```

**Body:**
```json
{
  "adminNote": "Mô tả workshop chưa đủ chi tiết. Vui lòng bổ sung syllabus và ảnh bìa chất lượng cao."
}
```

| Field | Type | Required | Mô tả |
|---|---|---|---|
| `adminNote` | `string` | ✅ Bắt buộc | Lý do từ chối / ghi chú từ admin cho vendor |

### Response

**200 OK – Thành công:**
```json
{
  "status": 200,
  "message": "Workshop template rejected",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Lớp học làm bánh cơ bản",
    "status": "REJECTED",
    "adminNote": "Mô tả workshop chưa đủ chi tiết. Vui lòng bổ sung syllabus và ảnh bìa chất lượng cao.",
    "reviewedBy": "admin-uuid-here",
    "reviewedAt": "2026-03-01T09:45:00",
    ...
  }
}
```

**400 Bad Request – Thiếu `adminNote`:**
```json
{
  "status": 400,
  "message": "Admin note (reason) is required when rejecting"
}
```

**400 Bad Request – Template không ở trạng thái PENDING:**
```json
{
  "status": 400,
  "message": "Only templates with 'Pending' status can be rejected. Current status: ACTIVE"
}
```

**403 Forbidden – Không phải Admin:**
```json
{
  "status": 403,
  "message": "Access Denied"
}
```

**404 Not Found – Không tìm thấy template:**
```json
{
  "status": 404,
  "message": "WorkshopTemplate not found with id: 550e8400-..."
}
```

### Ví dụ cURL
```bash
curl -X POST \
  http://localhost:8080/api/admin/vendors/workshop-templates/550e8400-e29b-41d4-a716-446655440000/reject \
  -H "Authorization: Bearer <admin_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{"adminNote": "Mô tả workshop chưa đủ chi tiết. Vui lòng bổ sung syllabus và ảnh bìa chất lượng cao."}'
```

---

## 3. So sánh 2 API

| Tiêu chí | Approve | Reject |
|---|---|---|
| Method | `POST` | `POST` |
| Endpoint | `.../approve` | `.../reject` |
| Request Body | ❌ Không cần | ✅ Cần `adminNote` |
| Status sau khi gọi | `PENDING` → `ACTIVE` | `PENDING` → `REJECTED` |
| `adminNote` | Tự động set `null` | Lưu lý do từ chối |
| `reviewedBy` | UUID của admin | UUID của admin |
| `reviewedAt` | Thời điểm approve | Thời điểm reject |

---

## 4. Lưu ý quan trọng

- Cả 2 API đều chỉ hoạt động khi template đang ở trạng thái **`PENDING`**. Nếu template đang ở `DRAFT`, `ACTIVE`, hoặc `REJECTED` sẽ trả về lỗi `400`.
- Sau khi **Approve**: `adminNote` sẽ bị xóa (`null`), `reviewedBy` và `reviewedAt` được ghi lại.
- Sau khi **Reject**: `adminNote` lưu lý do, `reviewedBy` và `reviewedAt` được ghi lại.
- Khi vendor **resubmit** (submit lại sau khi bị reject): `adminNote`, `reviewedBy`, `reviewedAt` đều bị reset về `null`.
- Field `adminNote` dùng chung cho cả approve và reject (trước đây là `rejectReason`).
