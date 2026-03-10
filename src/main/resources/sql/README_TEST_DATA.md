# 🎯 Admin Vendor Management - Test Data Package

## 📦 Package Contents

Bộ test data hoàn chỉnh cho Admin Vendor Management API, bao gồm:

### 1. SQL Scripts
- **add_test_vendors.sql** - Script insert 3 vendors test (2 verified, 1 unverified)

### 2. Testing Tools
- **test_vendors_postman.json** - Postman Collection với 13 test cases
- **TEST_VENDORS_GUIDE.md** - Hướng dẫn chi tiết đầy đủ
- **QUICK_TEST_REFERENCE.md** - Tham chiếu nhanh cho testing
- **README_TEST_DATA.md** - File tổng quan này

---

## 🚀 Quick Start (3 Steps)

### Step 1️⃣: Insert Test Data (1 phút)

**Option A - MySQL Workbench:**
```
1. Open MySQL Workbench
2. Open file: add_test_vendors.sql
3. Click Execute (⚡) or Ctrl+Shift+Enter
```

**Option B - Command Line:**
```bash
mysql -u root -p neonhs_db < add_test_vendors.sql
```

**Option C - DBeaver:**
```
1. Open DBeaver
2. SQL Editor > Load SQL Script
3. Select add_test_vendors.sql
4. Execute
```

### Step 2️⃣: Get Admin Token (30 giây)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"your_admin_password"}'
```

Copy token từ response.

### Step 3️⃣: Test APIs (2 phút)

**Option A - Postman:**
```
1. Import test_vendors_postman.json
2. Set adminToken variable
3. Run tests
```

**Option B - cURL:**
```bash
export ADMIN_TOKEN="your_jwt_token"
curl -X GET "http://localhost:8080/api/admin/vendors" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 👥 Test Vendors Created

### ✅ Vendor 1: Michael Anderson (Verified)
```
Business: Workshop Pro Services
Email: michael.anderson@workshoppro.com
Password: SecurePass123
Phone: +15551234567
Status: Verified ✅, Active ✅, Not Banned ✅
Location: San Francisco, CA
```

### ✅ Vendor 2: Sarah Martinez (Verified)
```
Business: Creative Skills Hub
Email: sarah.martinez@creativeskills.com
Password: SecurePass123
Phone: +15559876543
Status: Verified ✅, Active ✅, Not Banned ✅
Location: Los Angeles, CA
```

### ⚠️ Vendor 3: David Chen (Unverified)
```
Business: TechMasters Academy
Email: david.chen@techmasters.com
Password: SecurePass123
Phone: +15555551234
Status: NOT Verified ❌, Active ✅, Not Banned ✅
Location: Silicon Valley, CA
```

---

## 📋 API Testing Checklist

### Basic Operations
- [ ] Get All Vendors (should return 3+)
- [ ] Get Vendor by ID
- [ ] Create New Vendor
- [ ] Update Vendor Profile
- [ ] Delete Vendor (soft delete)

### Search & Filter
- [ ] Search by keyword "workshop"
- [ ] Filter verified vendors (expect 2)
- [ ] Filter unverified vendors (expect 1)
- [ ] Filter active vendors
- [ ] Advanced filter (multiple conditions)

### Ban/Unban Operations
- [ ] Ban vendor (David Chen)
- [ ] Get banned vendors list
- [ ] Unban vendor
- [ ] Verify vendor can login again

### Related Data
- [ ] Get vendor's workshop templates
- [ ] Verify pagination works
- [ ] Verify sorting works

---

## 📊 Test Scenarios

### Scenario 1: Admin Reviews New Vendor
```
1. Filter Unverified Vendors
   → Should see: David Chen (TechMasters Academy)
   
2. Get Vendor by ID (David's ID)
   → Review: business info, tax code, bank details
   
3. Update Vendor
   → Set: isVerified = true
   
4. Verify in Verified List
   → David now appears in verified vendors
```

### Scenario 2: Admin Handles Policy Violation
```
1. Search Vendor
   → Find: David Chen
   
2. Review Vendor Details
   → Check: workshop templates, reviews
   
3. Ban Vendor
   → Reason: "Violation of community guidelines"
   
4. Verify Ban Status
   → isBanned=true, isActive=false
   
5. Test Vendor Login
   → Should fail: Account is banned
   
6. Review After Investigation
   → Decide: Unban or Delete
   
7. Unban Vendor
   → Restore access
```

### Scenario 3: Admin Onboards Manual Vendor
```
1. Create Vendor Account
   → Emily Johnson - Johnson's Craft Workshop
   
2. Verify Creation
   → Get by ID, check all fields
   
3. Update Missing Info
   → Add: tax code, bank details
   
4. Set Verified Status
   → isVerified = true
   
5. Notify Vendor
   → Send welcome email (manual/future feature)
```

---

## 🧪 Testing Commands

### Get Vendor IDs (Run First!)
```sql
SELECT 
    vp.id as vendor_profile_id,
    u.fullname,
    vp.business_name,
    u.email
FROM vendor_profiles vp
JOIN users u ON vp.user_id = u.id
WHERE u.email IN (
    'michael.anderson@workshoppro.com',
    'sarah.martinez@creativeskills.com',
    'david.chen@techmasters.com'
)
ORDER BY u.created_at DESC;
```

Copy các IDs này để dùng trong API tests.

### Verify Data Integrity
```sql
-- Should return 3
SELECT COUNT(*) 
FROM vendor_profiles vp
JOIN users u ON vp.user_id = u.id
WHERE u.role = 'VENDOR'
AND u.email LIKE '%@workshoppro.com'
   OR u.email LIKE '%@creativeskills.com'
   OR u.email LIKE '%@techmasters.com';
```

---

## 📚 File Descriptions

| File | Purpose | When to Use |
|------|---------|-------------|
| **add_test_vendors.sql** | Insert test data | Setup phase - run once |
| **test_vendors_postman.json** | Postman tests | API testing with UI |
| **TEST_VENDORS_GUIDE.md** | Detailed guide | First time setup & learning |
| **QUICK_TEST_REFERENCE.md** | Quick commands | Daily testing reference |
| **README_TEST_DATA.md** | This overview | Understanding the package |

---

## 🎓 Learning Path

### For Beginners
1. Read **TEST_VENDORS_GUIDE.md** (đầy đủ từng bước)
2. Run SQL script
3. Use Postman Collection (visual interface)
4. Follow test scenarios

### For Experienced Devs
1. Scan **QUICK_TEST_REFERENCE.md**
2. Run SQL script
3. Use cURL commands
4. Write custom test scripts

---

## 💡 Pro Tips

### 1. Save Time
```bash
# Create alias for token
alias get-admin-token='curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@example.com\",\"password\":\"your_pass\"}" \
  | jq -r ".data.token"'

export ADMIN_TOKEN=$(get-admin-token)
```

### 2. Batch Testing
```bash
# Test all endpoints in sequence
for endpoint in vendors vendors/search vendors/filter/verified; do
  echo "Testing: $endpoint"
  curl -s "http://localhost:8080/api/admin/$endpoint" \
    -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.status'
done
```

### 3. Monitor Logs
```bash
# In separate terminal
tail -f logs/application.log | grep "AdminVendorManagement"
```

### 4. Quick Validation
```bash
# Check if vendors exist
curl -s "http://localhost:8080/api/admin/vendors?page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  | jq '.data.totalElements'
# Expected: >= 3
```

---

## 🐛 Common Issues & Solutions

### Issue: SQL Insert Fails - Duplicate Entry
```sql
-- Solution: Delete existing test vendors first
DELETE FROM vendor_profiles WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@workshoppro.com'
    OR email LIKE '%@creativeskills.com'
    OR email LIKE '%@techmasters.com'
);

DELETE FROM users WHERE email LIKE '%@workshoppro.com'
    OR email LIKE '%@creativeskills.com'
    OR email LIKE '%@techmasters.com';
```

### Issue: 401 Unauthorized
```
Cause: Token expired or invalid
Solution: Login again and get new token
```

### Issue: 403 Forbidden
```
Cause: Using non-admin token
Solution: Ensure you login with admin account
```

### Issue: 404 Vendor Not Found
```
Cause: Using wrong vendor_profile_id
Solution: Query database to get correct IDs
```

### Issue: Password Not Working
```
Cause: BCrypt hash mismatch
Solution: Generate new hash and update SQL script
```

---

## 🔄 Reset & Cleanup

### Full Reset
```sql
-- 1. Delete all test vendors
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

-- 2. Re-run insert script
source add_test_vendors.sql;
```

### Delete Only New Test Vendors
```sql
-- Delete vendors created during testing (Emily Johnson, etc.)
DELETE FROM vendor_profiles WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@newvendor.com'
);

DELETE FROM users WHERE email LIKE '%@newvendor.com';
```

---

## 📊 Expected Test Results

| Test | Expected Result |
|------|-----------------|
| Get All Vendors | Status 200, totalElements >= 3 |
| Search "workshop" | Find Michael Anderson |
| Filter Verified | 2 vendors (Michael, Sarah) |
| Filter Unverified | 1 vendor (David) |
| Create Vendor | Status 201, new ID returned |
| Update Vendor | Status 200, fields updated |
| Ban Vendor | isBanned=true, isActive=false |
| Unban Vendor | isBanned=false, isActive=true |
| Delete Vendor | Status 200, soft deleted |
| Get Templates | Status 200, may be empty |

---

## 🎯 Test Coverage

### ✅ Covered Scenarios
- Create vendor by admin
- Get all vendors with pagination
- Search vendors by keyword
- Filter by verification status
- Filter by banned status
- Filter by active status
- Advanced multi-criteria filtering
- Update vendor profile
- Ban/unban vendor
- Soft delete vendor
- Get vendor's workshop templates

### 🔜 Future Tests (Not Included)
- Email notifications
- Ban history tracking
- Audit logs
- Bulk operations
- Export to CSV
- Performance testing
- Load testing

---

## 📞 Support & Documentation

### Primary Docs
- **API Documentation**: `/MD/ADMIN_VENDOR_MANAGEMENT_API.md`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### Test Data Files
- **SQL**: `src/main/resources/sql/add_test_vendors.sql`
- **Postman**: `src/main/resources/sql/test_vendors_postman.json`
- **Guides**: `src/main/resources/sql/TEST_VENDORS_GUIDE.md`
- **Quick Ref**: `src/main/resources/sql/QUICK_TEST_REFERENCE.md`

### Need Help?
1. Check **TEST_VENDORS_GUIDE.md** (troubleshooting section)
2. Review **QUICK_TEST_REFERENCE.md** (quick fixes)
3. Check application logs
4. Verify database state with SQL queries

---

## ✨ Next Steps

1. ✅ Setup test data (this package)
2. 🔄 Test all CRUD operations
3. 📝 Document any issues found
4. 🎨 Integrate with frontend
5. 🚀 Deploy to staging
6. 🧪 Run integration tests
7. 📊 Performance testing
8. 🎓 User acceptance testing

---

## 📝 Version History

- **v1.0** (Feb 2026) - Initial release
  - 3 test vendors
  - 13 API test cases
  - Complete documentation
  - Postman collection

---

**Package Created**: February 2026  
**Author**: Development Team  
**Purpose**: Admin Vendor Management API Testing  
**Status**: Ready to Use ✅

---

## 🎉 You're All Set!

Bạn đã có đầy đủ:
- ✅ 3 test vendors trong database
- ✅ Postman collection để test
- ✅ Hướng dẫn chi tiết
- ✅ Quick reference commands
- ✅ Troubleshooting guides

**Start Testing Now! 🚀**
