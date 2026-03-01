# Hướng Dẫn Tạo Test Data Cho Admin Vendor Management API

## 📋 Tổng Quan

File này hướng dẫn chi tiết cách tạo và sử dụng test data cho việc kiểm thử Admin Vendor Management API.

## 📦 Files Đã Tạo

1. **add_test_vendors.sql** - SQL script để insert test vendors vào database
2. **test_vendors_postman.json** - Postman collection với các API test cases
3. **TEST_VENDORS_GUIDE.md** - File hướng dẫn này

## 🎯 Test Vendors Đã Tạo

### Vendor 1: Workshop Pro Services (Verified)
- **Tên**: Michael Anderson
- **Email**: michael.anderson@workshoppro.com
- **Password**: SecurePass123
- **Business**: Workshop Pro Services
- **Phone**: +15551234567
- **Status**: Verified, Active, Not Banned
- **Đặc điểm**: Vendor chuyên nghiệp, đã được xác thực

### Vendor 2: Creative Skills Hub (Verified)
- **Tên**: Sarah Martinez
- **Email**: sarah.martinez@creativeskills.com
- **Password**: SecurePass123
- **Business**: Creative Skills Hub
- **Phone**: +15559876543
- **Status**: Verified, Active, Not Banned
- **Đặc điểm**: Vendor sáng tạo, đã được xác thực

### Vendor 3: TechMasters Academy (Unverified)
- **Tên**: David Chen
- **Email**: david.chen@techmasters.com
- **Password**: SecurePass123
- **Business**: TechMasters Academy
- **Phone**: +15555551234
- **Status**: NOT Verified, Active, Not Banned
- **Đặc điểm**: Vendor mới, chưa được xác thực (để test filter)

## 🚀 Bước 1: Insert Test Data Vào Database

### Option A: Sử dụng MySQL Workbench

1. Mở MySQL Workbench
2. Kết nối tới database của bạn
3. Mở file `add_test_vendors.sql`
4. Chạy toàn bộ script (Click ⚡ Execute hoặc Ctrl+Shift+Enter)
5. Kiểm tra kết quả bằng verification query ở cuối file

### Option B: Sử dụng Command Line

```bash
# Điều hướng đến thư mục chứa file SQL
cd "E:\University\KI9 2026 SPRING\Capstone\New folder\NeoNHS-BE\src\main\resources\sql"

# Chạy script
mysql -u your_username -p your_database_name < add_test_vendors.sql

# Nhập password khi được yêu cầu
```

### Option C: Sử dụng DBeaver

1. Mở DBeaver
2. Kết nối tới database
3. Right-click vào database > SQL Editor > Load SQL script
4. Chọn file `add_test_vendors.sql`
5. Execute script

### Verification Query

Sau khi insert, chạy query này để kiểm tra:

```sql
SELECT 
    u.id as user_id,
    u.fullname,
    u.email,
    u.role,
    u.is_verified as user_verified,
    u.is_banned,
    vp.id as vendor_profile_id,
    vp.business_name,
    vp.is_verified as vendor_verified
FROM users u
INNER JOIN vendor_profiles vp ON u.id = vp.user_id
WHERE u.email IN (
    'michael.anderson@workshoppro.com',
    'sarah.martinez@creativeskills.com',
    'david.chen@techmasters.com'
)
ORDER BY u.created_at DESC;
```

**Expected Result**: Bạn sẽ thấy 3 vendors với thông tin đầy đủ

## 🔐 Bước 2: Lấy Admin JWT Token

### 2.1. Login với Admin Account

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin_password"
}
```

### 2.2. Lưu Token

Copy JWT token từ response:
```json
{
  "status": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {...}
  }
}
```

## 📮 Bước 3: Import Postman Collection

### 3.1. Import vào Postman

1. Mở Postman
2. Click **Import** button (góc trên bên trái)
3. Chọn file `test_vendors_postman.json`
4. Collection "Admin Vendor Management - Test Data" sẽ xuất hiện

### 3.2. Cấu Hình Variables

1. Click vào Collection name
2. Chọn tab **Variables**
3. Cập nhật các giá trị:
   - `baseUrl`: `http://localhost:8080` (hoặc URL server của bạn)
   - `adminToken`: Paste JWT token từ bước 2

### 3.3. Lấy Vendor IDs

Sau khi insert data, bạn cần lấy vendor_profile_id để test các API cụ thể:

**Chạy API Get All Vendors:**
```
GET http://localhost:8080/api/admin/vendors?page=1&size=10
```

**Copy các IDs từ response:**
- Michael Anderson vendor ID
- Sarah Martinez vendor ID  
- David Chen vendor ID

**Update trong các request của Postman:**
- Thay `REPLACE_WITH_MICHAEL_VENDOR_PROFILE_ID` bằng ID thực
- Thay `REPLACE_WITH_VENDOR_ID` bằng ID muốn test
- Thay `REPLACE_WITH_DAVID_VENDOR_ID` bằng David's ID

## 🧪 Bước 4: Test Các API Endpoints

### Test Suite Order (theo thứ tự)

#### 1️⃣ **Get All Vendors**
- **Mục đích**: Xem tất cả vendors trong hệ thống
- **Expected**: Trả về list với 3+ vendors
- **Verify**: Status 200, có pagination info

#### 2️⃣ **Search Vendors by Keyword**
- **Keyword**: "workshop"
- **Expected**: Tìm thấy Michael Anderson (Workshop Pro Services)
- **Verify**: Kết quả chứa keyword trong business name

#### 3️⃣ **Filter Verified Vendors**
- **isVerified**: true
- **Expected**: Michael và Sarah (2 vendors)
- **Verify**: Tất cả đều có isVerifiedVendor = true

#### 4️⃣ **Filter Unverified Vendors**
- **isVerified**: false
- **Expected**: David Chen (TechMasters Academy)
- **Verify**: isVerifiedVendor = false

#### 5️⃣ **Create New Vendor**
- **Mục đích**: Test tạo vendor mới bởi admin
- **Data**: Emily Johnson - Johnson's Craft Workshop
- **Expected**: Status 201, vendor được tạo thành công
- **Verify**: Có vendor_profile_id trong response

#### 6️⃣ **Get Vendor by ID**
- **Vendor**: Michael Anderson
- **Expected**: Status 200, thông tin chi tiết đầy đủ
- **Verify**: Business name, email, phone number chính xác

#### 7️⃣ **Update Vendor Profile**
- **Vendor**: Michael Anderson
- **Changes**: Đổi tên, phone, business name
- **Expected**: Status 200, thông tin đã update
- **Verify**: Response chứa data mới

#### 8️⃣ **Ban Vendor**
- **Vendor**: David Chen (vendor chưa verified)
- **Reason**: "Violation of community guidelines - testing ban functionality"
- **Expected**: isBanned = true, isActive = false
- **Verify**: Status 200, vendor không thể login

#### 9️⃣ **Get Banned Vendors**
- **isBanned**: true
- **Expected**: Tìm thấy David Chen
- **Verify**: Tất cả có isBanned = true

#### 🔟 **Unban Vendor**
- **Vendor**: David Chen
- **Expected**: isBanned = false, isActive = true
- **Verify**: Vendor có thể login lại

#### 1️⃣1️⃣ **Advanced Filter**
- **Filters**: isVerified=true, isActive=true, isBanned=false
- **Expected**: Michael và Sarah
- **Verify**: Tất cả thỏa mãn điều kiện

#### 1️⃣2️⃣ **Delete Vendor (Soft Delete)**
- **Vendor**: Vendor test (Emily Johnson vừa tạo)
- **Expected**: Status 200, isActive = false
- **Verify**: Vendor biến mất khỏi active list

#### 1️⃣3️⃣ **Get Vendor's Workshop Templates**
- **Vendor**: Michael Anderson
- **Expected**: List các workshop templates của vendor
- **Verify**: Status 200 (có thể empty nếu chưa có templates)

## 📊 Expected Results Summary

| Test Case | Expected Status | Key Verification |
|-----------|----------------|------------------|
| Get All Vendors | 200 | totalElements >= 3 |
| Search "workshop" | 200 | Found Michael Anderson |
| Filter Verified | 200 | 2 vendors (Michael, Sarah) |
| Filter Unverified | 200 | 1 vendor (David) |
| Create Vendor | 201 | New vendor ID returned |
| Get by ID | 200 | Correct vendor details |
| Update Vendor | 200 | Updated fields reflected |
| Ban Vendor | 200 | isBanned=true |
| Get Banned | 200 | David in list |
| Unban Vendor | 200 | isBanned=false |
| Advanced Filter | 200 | Only matching vendors |
| Delete Vendor | 200 | isActive=false |
| Get Templates | 200 | Template list (may be empty) |

## 🐛 Troubleshooting

### Issue 1: SQL Script Error
**Error**: Duplicate entry for email
**Solution**: Vendors đã tồn tại. Xóa hoặc thay đổi email trong script

```sql
-- Xóa test vendors
DELETE FROM vendor_profiles WHERE user_id IN (
    SELECT id FROM users WHERE email IN (
        'michael.anderson@workshoppro.com',
        'sarah.martinez@creativeskills.com',
        'david.chen@techmasters.com'
    )
);

DELETE FROM users WHERE email IN (
    'michael.anderson@workshoppro.com',
    'sarah.martinez@creativeskills.com',
    'david.chen@techmasters.com'
);
```

### Issue 2: 401 Unauthorized
**Cause**: Admin token hết hạn hoặc không valid
**Solution**: Login lại và cập nhật token mới trong Postman

### Issue 3: 403 Forbidden
**Cause**: Token không phải của admin
**Solution**: Đảm bảo login với account có role ADMIN

### Issue 4: 404 Not Found
**Cause**: Vendor ID không tồn tại
**Solution**: Chạy Get All Vendors để lấy ID chính xác

### Issue 5: Password Hash không đúng
**Cause**: BCrypt hash trong SQL không match
**Solution**: Generate hash mới:

```java
// Trong Java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("SecurePass123");
System.out.println(hash);
```

Hoặc dùng online tool: https://bcrypt-generator.com/

## 📝 Notes

1. **Password**: Tất cả test vendors dùng password "SecurePass123"
2. **Vendor IDs**: Sẽ khác nhau mỗi lần insert (do UUID auto-generate)
3. **JWT Token**: Cần refresh khi hết hạn (thường 24h)
4. **Soft Delete**: Delete không xóa hẳn khỏi DB, chỉ set isActive=false
5. **Ban vs Delete**: Ban là tạm thời và có thể unban, Delete là vĩnh viễn

## 🎓 Test Scenarios

### Scenario 1: Admin quản lý vendor mới
```
1. Create Vendor (Emily Johnson)
2. Get Vendor by ID (verify data)
3. Update profile (add more info)
4. Get All Vendors (verify in list)
```

### Scenario 2: Admin xử lý vi phạm
```
1. Search vendor by keyword
2. Get Vendor by ID (review details)
3. Ban Vendor (with reason)
4. Get Banned Vendors (verify in list)
5. Unban Vendor (after review)
```

### Scenario 3: Admin tìm vendor để verify
```
1. Filter Unverified Vendors
2. Get Vendor by ID (review profile)
3. Update Vendor (set isVerified=true)
4. Filter Verified Vendors (verify in list)
```

### Scenario 4: Admin xem báo cáo
```
1. Get All Vendors (overall stats)
2. Filter Verified (count verified)
3. Filter Banned (count banned)
4. Advanced Filter (active + verified only)
```

## 🔄 Reset Test Data

Nếu muốn reset về trạng thái ban đầu:

```sql
-- Xóa vendors test
DELETE FROM vendor_profiles WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@workshoppro.com'
    OR email LIKE '%@creativeskills.com'
    OR email LIKE '%@techmasters.com'
    OR email LIKE '%@newvendor.com'
);

DELETE FROM users WHERE email LIKE '%@workshoppro.com'
    OR email LIKE '%@creativeskills.com'
    OR email LIKE '%@techmasters.com'
    OR email LIKE '%@newvendor.com';

-- Chạy lại add_test_vendors.sql
```

## ✅ Checklist Trước Khi Test

- [ ] Database đã có 3 test vendors
- [ ] Backend server đang chạy (port 8080)
- [ ] Đã có Admin JWT token
- [ ] Postman collection đã import
- [ ] Variables trong Postman đã cấu hình
- [ ] Đã lấy vendor IDs từ database
- [ ] Đã thay thế IDs trong Postman requests

## 📞 Support

Nếu gặp vấn đề, kiểm tra:
1. Application logs trong terminal
2. Database connection
3. JWT token expiration
4. API endpoint URL đúng chưa

---

**Created**: February 2026  
**Version**: 1.0  
**Author**: Development Team
