# Tóm tắt thay đổi - Admin Tracking cho Workshop Template

## Những gì đã thêm:

### 1. Trường mới trong Entity (WorkshopTemplate.java)
- `rejectedBy` (UUID): Lưu ID của admin đã reject template

### 2. Trường mới trong Response DTO (WorkshopTemplateResponse.java)
- `rejectedBy` (UUID): Trả về ID của admin đã reject

### 3. Cập nhật logic trong Service (WorkshopTemplateServiceImpl.java)

#### API Approve:
```java
// Khi approve, sẽ:
- Set approvedBy = admin.getId()
- Set approvedAt = LocalDateTime.now()
- Set status = ACTIVE
- Clear rejectReason = null
- Clear rejectedBy = null  // ← MỚI THÊM
```

#### API Reject:
```java
// Khi reject, sẽ:
- Set rejectedBy = admin.getId()  // ← MỚI THÊM
- Set rejectReason = lý do reject
- Set status = REJECTED
- Clear approvedBy = null
- Clear approvedAt = null
```

### 4. SQL Migration (add_rejected_by_column.sql)
```sql
ALTER TABLE workshop_templates 
ADD COLUMN rejected_by BINARY(16) NULL;
```

## Cách chạy Migration:

1. Chạy lệnh SQL:
```sql
ALTER TABLE workshop_templates 
ADD COLUMN rejected_by BINARY(16) NULL COMMENT 'UUID của admin đã reject template';
```

2. Hoặc chạy file SQL trực tiếp:
```bash
mysql -u username -p database_name < src/main/resources/sql/add_rejected_by_column.sql
```

## Kết quả:

### Response khi Approve thành công:
```json
{
  "id": "template-uuid",
  "status": "ACTIVE",
  "approvedBy": "admin-uuid",
  "approvedAt": "2026-03-01T10:00:00",
  "rejectedBy": null,
  "rejectReason": null
}
```

### Response khi Reject thành công:
```json
{
  "id": "template-uuid", 
  "status": "REJECTED",
  "rejectedBy": "admin-uuid",  // ← MỚI
  "rejectReason": "Lý do reject",
  "approvedBy": null,
  "approvedAt": null
}
```

## Lợi ích:

✅ Biết chính xác admin nào đã approve/reject
✅ Có đầy đủ thông tin để audit
✅ Tăng tính minh bạch và trách nhiệm
✅ Dễ dàng debug và tra cứu lịch sử

## Files đã thay đổi:

1. ✅ `WorkshopTemplate.java` - Thêm field `rejectedBy`
2. ✅ `WorkshopTemplateResponse.java` - Thêm field `rejectedBy`
3. ✅ `WorkshopTemplateServiceImpl.java` - Cập nhật logic approve/reject
4. ✅ `add_rejected_by_column.sql` - Script migration database

## Lưu ý:

- **QUAN TRỌNG**: Phải chạy migration SQL trước khi test API
- Admin identity được lấy từ JWT token (Principal.getName())
- UUID được lấy từ database dựa trên email của admin
- Chỉ một trong hai field (approvedBy hoặc rejectedBy) được set tại một thời điểm
