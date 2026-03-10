# API Guide: Workshop Public APIs (Tourist)

> **Base URL:** `/api/public/workshops`  
> **Authentication:** ❌ Không cần (Public endpoints)  
> **Controller:** `WorkshopTouristController`

---

## Tổng quan

Bộ API này dành cho **Tourist / người dùng chưa đăng nhập** để duyệt xem các workshop template và session đang hoạt động.

| # | Method | Endpoint | Mô tả |
|---|---|---|---|
| 1 | `GET` | `/api/public/workshops/templates` | Danh sách tất cả template ACTIVE |
| 2 | `GET` | `/api/public/workshops/templates/{id}` | Chi tiết 1 template ACTIVE |
| 3 | `GET` | `/api/public/workshops/templates/search` | Search + filter template ACTIVE |
| 4 | `GET` | `/api/public/workshops/templates/{id}/sessions` | Sessions sắp tới của 1 template |

> **Lưu ý:** Tất cả API chỉ trả về template có `status = ACTIVE` (đã được admin duyệt).

---

## 1. Lấy danh sách Workshop Template đang Active

### Endpoint
```
GET /api/public/workshops/templates
```

### Query Parameters

| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `page` | `int` | ❌ | `1` | Số trang (bắt đầu từ 1) |
| `size` | `int` | ❌ | `10` | Số item mỗi trang |
| `sortBy` | `string` | ❌ | `createdAt` | Trường để sort: `createdAt`, `name`, `defaultPrice`, `averageRating`, `estimatedDuration` |
| `sortDir` | `string` | ❌ | `desc` | Chiều sort: `asc` hoặc `desc` |

### Request
```
GET /api/public/workshops/templates?page=1&size=10&sortBy=createdAt&sortDir=desc
```

### Response – 200 OK
```json
{
  "status": 200,
  "success": true,
  "message": "Active workshop templates retrieved successfully",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "name": "Lớp học làm bánh cơ bản",
        "shortDescription": "Học làm bánh từ đầu trong 3 giờ",
        "fullDescription": "Khóa học dành cho người mới bắt đầu...",
        "estimatedDuration": 180,
        "defaultPrice": 500000.00,
        "minParticipants": 5,
        "maxParticipants": 20,
        "status": "ACTIVE",
        "averageRating": 4.50,
        "totalRatings": 12,
        "vendorId": "vendor-uuid-here",
        "vendorName": "Workshop Pro",
        "createdAt": "2026-02-01T10:00:00",
        "updatedAt": "2026-02-15T09:00:00",
        "adminNote": null,
        "reviewedBy": "admin-uuid",
        "reviewedAt": "2026-02-15T09:00:00",
        "images": [
          {
            "id": "img-uuid",
            "imageUrl": "https://example.com/image1.jpg",
            "isThumbnail": true
          }
        ],
        "tags": [
          {
            "id": "tag-uuid",
            "name": "Ẩm thực",
            "description": "Workshop về nấu ăn và làm bánh",
            "tagColor": "#FF6B35",
            "iconUrl": "https://example.com/icon.png"
          }
        ]
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 25,
    "totalPages": 3,
    "last": false,
    "first": true
  }
}
```

### Ví dụ cURL
```bash
# Trang 1, 10 items, sort theo rating cao nhất
curl -X GET "http://localhost:8080/api/public/workshops/templates?page=1&size=10&sortBy=averageRating&sortDir=desc"
```

---

## 2. Xem chi tiết Workshop Template

### Endpoint
```
GET /api/public/workshops/templates/{id}
```

### Path Variable

| Tham số | Kiểu | Mô tả |
|---|---|---|
| `id` | `UUID` | ID của workshop template |

### Request
```
GET /api/public/workshops/templates/550e8400-e29b-41d4-a716-446655440000
```

### Response – 200 OK
```json
{
  "status": 200,
  "success": true,
  "message": "Workshop template retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Lớp học làm bánh cơ bản",
    "shortDescription": "Học làm bánh từ đầu trong 3 giờ",
    "fullDescription": "Khóa học dành cho người mới bắt đầu muốn học làm bánh tại nhà...",
    "estimatedDuration": 180,
    "defaultPrice": 500000.00,
    "minParticipants": 5,
    "maxParticipants": 20,
    "status": "ACTIVE",
    "averageRating": 4.50,
    "totalRatings": 12,
    "vendorId": "vendor-uuid-here",
    "vendorName": "Workshop Pro",
    "createdAt": "2026-02-01T10:00:00",
    "updatedAt": "2026-02-15T09:00:00",
    "adminNote": null,
    "reviewedBy": "admin-uuid",
    "reviewedAt": "2026-02-15T09:00:00",
    "images": [...],
    "tags": [...]
  }
}
```

### Response – 404 Not Found
> Trả về 404 nếu template **không tồn tại** hoặc **không ở trạng thái ACTIVE**

```json
{
  "status": 404,
  "success": false,
  "message": "WorkshopTemplate not found with id: 550e8400-..."
}
```

### Ví dụ cURL
```bash
curl -X GET "http://localhost:8080/api/public/workshops/templates/550e8400-e29b-41d4-a716-446655440000"
```

---

## 3. Search & Filter Workshop Template

### Endpoint
```
GET /api/public/workshops/templates/search
```

### Query Parameters

| Tham số | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| `keyword` | `string` | ❌ | Tìm kiếm trong tên, mô tả ngắn, mô tả đầy đủ |
| `tagId` | `UUID` | ❌ | Lọc theo ID của WTag |
| `minPrice` | `decimal` | ❌ | Giá tối thiểu (VD: `100000`) |
| `maxPrice` | `decimal` | ❌ | Giá tối đa (VD: `1000000`) |
| `minDuration` | `int` | ❌ | Thời lượng tối thiểu (phút) |
| `maxDuration` | `int` | ❌ | Thời lượng tối đa (phút) |
| `minRating` | `decimal` | ❌ | Rating tối thiểu (0.0 – 5.0) |
| `page` | `int` | ❌ | Số trang, mặc định `1` |
| `size` | `int` | ❌ | Số item/trang, mặc định `10` |
| `sortBy` | `string` | ❌ | Trường sort, mặc định `createdAt` |
| `sortDir` | `string` | ❌ | Chiều sort `asc`/`desc`, mặc định `desc` |

> Tất cả tham số đều **optional**, có thể kết hợp tự do.

### Ví dụ các trường hợp search

**Tìm kiếm theo từ khóa:**
```
GET /api/public/workshops/templates/search?keyword=bánh
```

**Lọc theo khoảng giá:**
```
GET /api/public/workshops/templates/search?minPrice=200000&maxPrice=800000
```

**Lọc theo tag:**
```
GET /api/public/workshops/templates/search?tagId=abc12345-0000-0000-0000-000000000000
```

**Lọc theo thời lượng (60–180 phút):**
```
GET /api/public/workshops/templates/search?minDuration=60&maxDuration=180
```

**Lọc rating >= 4.0:**
```
GET /api/public/workshops/templates/search?minRating=4.0
```

**Kết hợp nhiều điều kiện:**
```
GET /api/public/workshops/templates/search?keyword=yoga&minPrice=100000&maxPrice=500000&minRating=4.0&sortBy=averageRating&sortDir=desc&page=1&size=6
```

### Response – 200 OK
```json
{
  "status": 200,
  "success": true,
  "message": "Workshop templates retrieved successfully",
  "data": {
    "content": [ ... ],
    "totalElements": 8,
    "totalPages": 2,
    "first": true,
    "last": false
  }
}
```

### Ví dụ cURL
```bash
curl -X GET "http://localhost:8080/api/public/workshops/templates/search?keyword=yoga&minRating=4.0&sortBy=averageRating&sortDir=desc"
```

---

## 4. Xem danh sách Sessions sắp tới của Workshop Template

### Endpoint
```
GET /api/public/workshops/templates/{id}/sessions
```

### Mô tả
Trả về danh sách các session **sắp diễn ra** (`SCHEDULED` + `startTime > hiện tại`) của một workshop template cụ thể.

### Path Variable

| Tham số | Kiểu | Mô tả |
|---|---|---|
| `id` | `UUID` | ID của workshop template |

### Query Parameters

| Tham số | Kiểu | Bắt buộc | Mặc định | Mô tả |
|---|---|---|---|---|
| `page` | `int` | ❌ | `1` | Số trang |
| `size` | `int` | ❌ | `10` | Số item/trang |
| `sortBy` | `string` | ❌ | `startTime` | Trường sort (khuyến nghị dùng `startTime`) |
| `sortDir` | `string` | ❌ | `asc` | Chiều sort — mặc định `asc` để hiện session gần nhất trước |

### Request
```
GET /api/public/workshops/templates/550e8400-e29b-41d4-a716-446655440000/sessions?page=1&size=5
```

### Response – 200 OK
```json
{
  "status": 200,
  "success": true,
  "message": "Upcoming sessions retrieved successfully",
  "data": {
    "content": [
      {
        "id": "session-uuid-001",
        "startTime": "2026-03-10T09:00:00",
        "endTime": "2026-03-10T12:00:00",
        "price": 500000.00,
        "maxParticipants": 20,
        "currentEnrolled": 8,
        "availableSlots": 12,
        "status": "SCHEDULED",
        "workshopTemplateId": "550e8400-e29b-41d4-a716-446655440000",
        "name": "Lớp học làm bánh cơ bản",
        "shortDescription": "Học làm bánh từ đầu trong 3 giờ",
        "estimatedDuration": 180,
        "averageRating": 4.50,
        "totalRatings": 12,
        "vendorId": "vendor-uuid",
        "vendorName": "Workshop Pro",
        "images": [...],
        "tags": [...],
        "createdAt": "2026-02-20T10:00:00",
        "updatedAt": "2026-02-20T10:00:00"
      }
    ],
    "totalElements": 3,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

### Response – 404 Not Found
> Trả về 404 nếu template **không tồn tại** hoặc **không ở trạng thái ACTIVE**

```json
{
  "status": 404,
  "success": false,
  "message": "WorkshopTemplate not found with id: 550e8400-..."
}
```

### Ví dụ cURL
```bash
# Lấy các session gần nhất (sort startTime asc)
curl -X GET "http://localhost:8080/api/public/workshops/templates/550e8400-e29b-41d4-a716-446655440000/sessions?page=1&size=5&sortBy=startTime&sortDir=asc"
```

---

## 5. Các trường trong Response

### WorkshopTemplateResponse

| Field | Kiểu | Mô tả |
|---|---|---|
| `id` | `UUID` | ID của template |
| `name` | `string` | Tên workshop |
| `shortDescription` | `string` | Mô tả ngắn |
| `fullDescription` | `string` | Mô tả đầy đủ |
| `estimatedDuration` | `int` | Thời lượng ước tính (phút) |
| `defaultPrice` | `decimal` | Giá mặc định |
| `minParticipants` | `int` | Số người tham gia tối thiểu |
| `maxParticipants` | `int` | Số người tham gia tối đa |
| `status` | `string` | Luôn là `ACTIVE` với API public |
| `averageRating` | `decimal` | Điểm đánh giá trung bình (0.0 – 5.0) |
| `totalRatings` | `int` | Tổng số lượt đánh giá từ tourist |
| `vendorId` | `UUID` | ID của vendor |
| `vendorName` | `string` | Tên doanh nghiệp của vendor |
| `images` | `array` | Danh sách ảnh (`id`, `imageUrl`, `isThumbnail`) |
| `tags` | `array` | Danh sách WTag (`id`, `name`, `tagColor`, `iconUrl`) |

### WorkshopSessionResponse

| Field | Kiểu | Mô tả |
|---|---|---|
| `id` | `UUID` | ID của session |
| `startTime` | `datetime` | Thời gian bắt đầu |
| `endTime` | `datetime` | Thời gian kết thúc |
| `price` | `decimal` | Giá của session này (có thể khác `defaultPrice`) |
| `maxParticipants` | `int` | Sức chứa tối đa |
| `currentEnrolled` | `int` | Số người đã đăng ký |
| `availableSlots` | `int` | Số chỗ còn trống (`maxParticipants - currentEnrolled`) |
| `status` | `string` | Luôn là `SCHEDULED` với API public |

---

## 6. Lưu ý khi tích hợp Frontend

- **Tất cả API đều public**, không cần gửi `Authorization` header.
- API `/templates` và `/templates/search` **chỉ trả về template `ACTIVE`** — không lộ template `DRAFT`, `PENDING`, `REJECTED`.
- API `/templates/{id}` trả về **404** nếu template tồn tại nhưng không phải `ACTIVE` (ví dụ đang `PENDING`).
- API `/templates/{id}/sessions` chỉ trả về session **chưa diễn ra** (`startTime > now`) và có `status = SCHEDULED`.
- `availableSlots = maxParticipants - currentEnrolled` — dùng để hiển thị badge "Còn X chỗ".
- Nên sort sessions theo `startTime asc` (mặc định) để hiển thị session gần nhất lên đầu.
- `page` bắt đầu từ **1** (không phải 0) cho tất cả các API.
