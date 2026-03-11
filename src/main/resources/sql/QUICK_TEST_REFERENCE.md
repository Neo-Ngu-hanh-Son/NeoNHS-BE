# Quick Test Reference - Admin Vendor Management

## 🚀 Quick Start (5 phút setup)

### 1. Insert Test Data
```sql
-- Chạy file này trong MySQL
source add_test_vendors.sql;
```

### 2. Get Admin Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin_password"}'
```

### 3. Export Token
```bash
export ADMIN_TOKEN="your_jwt_token_here"
```

---

## 📋 Test Vendors Credentials

| Vendor | Email | Password | Status |
|--------|-------|----------|--------|
| Michael Anderson | michael.anderson@workshoppro.com | SecurePass123 | ✅ Verified |
| Sarah Martinez | sarah.martinez@creativeskills.com | SecurePass123 | ✅ Verified |
| David Chen | david.chen@techmasters.com | SecurePass123 | ❌ Unverified |

---

## 🧪 Quick cURL Tests

### Get All Vendors
```bash
curl -X GET "http://localhost:8080/api/admin/vendors?page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Search Vendors
```bash
curl -X GET "http://localhost:8080/api/admin/vendors/search?keyword=workshop" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Filter Verified
```bash
curl -X GET "http://localhost:8080/api/admin/vendors/filter/verified?isVerified=true" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Filter Unverified
```bash
curl -X GET "http://localhost:8080/api/admin/vendors/filter/verified?isVerified=false" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Create Vendor
```bash
curl -X POST http://localhost:8080/api/admin/vendors \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullname": "Test Vendor",
    "email": "test@example.com",
    "password": "SecurePass123",
    "phoneNumber": "+1234567890",
    "businessName": "Test Business",
    "description": "Test description",
    "address": "Test address",
    "isVerified": true
  }'
```

### Get Vendor by ID
```bash
# Replace {id} with actual vendor profile ID
curl -X GET "http://localhost:8080/api/admin/vendors/{id}" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Update Vendor
```bash
# Replace {id} with actual vendor profile ID
curl -X PUT "http://localhost:8080/api/admin/vendors/{id}" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullname": "Updated Name",
    "businessName": "Updated Business"
  }'
```

### Ban Vendor
```bash
# Replace {id} with actual vendor profile ID
curl -X POST "http://localhost:8080/api/admin/vendors/{id}/ban" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Testing ban functionality"
  }'
```

### Unban Vendor
```bash
# Replace {id} with actual vendor profile ID
curl -X POST "http://localhost:8080/api/admin/vendors/{id}/unban" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Delete Vendor
```bash
# Replace {id} with actual vendor profile ID
curl -X DELETE "http://localhost:8080/api/admin/vendors/{id}" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Get Vendor's Templates
```bash
# Replace {id} with actual vendor profile ID
curl -X GET "http://localhost:8080/api/admin/vendors/{id}/workshop-templates" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 🔍 Quick Queries

### Get Vendor IDs
```sql
SELECT 
    vp.id as vendor_profile_id,
    u.fullname,
    vp.business_name,
    u.email
FROM vendor_profiles vp
JOIN users u ON vp.user_id = u.id
WHERE u.email LIKE '%workshop%'
   OR u.email LIKE '%creative%'
   OR u.email LIKE '%techmaster%';
```

### Count by Status
```sql
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN vp.is_verified = TRUE THEN 1 ELSE 0 END) as verified,
    SUM(CASE WHEN u.is_banned = TRUE THEN 1 ELSE 0 END) as banned,
    SUM(CASE WHEN u.is_active = TRUE THEN 1 ELSE 0 END) as active
FROM vendor_profiles vp
JOIN users u ON vp.user_id = u.id;
```

### Find Specific Vendor
```sql
SELECT 
    vp.id as vendor_profile_id,
    u.id as user_id,
    u.fullname,
    u.email,
    vp.business_name,
    vp.is_verified as vendor_verified,
    u.is_verified as user_verified,
    u.is_active,
    u.is_banned
FROM vendor_profiles vp
JOIN users u ON vp.user_id = u.id
WHERE u.email = 'michael.anderson@workshoppro.com';
```

---

## ✅ Quick Validation

### Verify Data Inserted
```sql
SELECT COUNT(*) as vendor_count 
FROM vendor_profiles vp
JOIN users u ON vp.user_id = u.id
WHERE u.email IN (
    'michael.anderson@workshoppro.com',
    'sarah.martinez@creativeskills.com',
    'david.chen@techmasters.com'
);
-- Expected: 3
```

### Check API Response
```bash
# Should return 200 OK and list of vendors
curl -s -o /dev/null -w "%{http_code}" \
  -X GET "http://localhost:8080/api/admin/vendors" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 🐛 Quick Troubleshooting

### Error 401 - Token expired
```bash
# Login again and get new token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin_password"}' \
  | jq -r '.data.token'
```

### Error 404 - Vendor not found
```bash
# Get all vendor IDs first
curl -X GET "http://localhost:8080/api/admin/vendors?page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  | jq '.data.content[].id'
```

### Reset Test Data
```sql
-- Delete test vendors
DELETE FROM vendor_profiles WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@workshoppro.com'
    OR email LIKE '%@creativeskills.com'
    OR email LIKE '%@techmasters.com'
);

DELETE FROM users WHERE email LIKE '%@workshoppro.com'
    OR email LIKE '%@creativeskills.com'
    OR email LIKE '%@techmasters.com';

-- Re-run insert script
source add_test_vendors.sql;
```

---

## 📊 Expected Results Table

| Endpoint | Method | Expected Status | Key Field |
|----------|--------|----------------|-----------|
| /api/admin/vendors | GET | 200 | totalElements >= 3 |
| /api/admin/vendors/search | GET | 200 | content.length > 0 |
| /api/admin/vendors/filter/verified | GET | 200 | All isVerified=true |
| /api/admin/vendors | POST | 201 | New vendor.id |
| /api/admin/vendors/{id} | GET | 200 | Vendor details |
| /api/admin/vendors/{id} | PUT | 200 | Updated fields |
| /api/admin/vendors/{id}/ban | POST | 200 | isBanned=true |
| /api/admin/vendors/{id}/unban | POST | 200 | isBanned=false |
| /api/admin/vendors/{id} | DELETE | 200 | isActive=false |

---

## 🎯 Test Flow (Recommended Order)

```
1. GET All Vendors → Verify 3+ vendors exist
2. Search "workshop" → Find Michael Anderson
3. Filter Verified → 2 verified vendors
4. Filter Unverified → 1 unverified (David)
5. Create New Vendor → Emily Johnson
6. Get by ID → Verify details
7. Update Vendor → Change name/phone
8. Ban Vendor → David Chen
9. Get Banned → Verify David in list
10. Unban Vendor → Restore David
11. Delete Vendor → Remove Emily
```

---

## 💡 Pro Tips

1. **Save Vendor IDs**: Lưu lại IDs sau khi insert để dùng trong tests
2. **Use jq**: Parse JSON responses dễ hơn với `| jq`
3. **Environment Variables**: Set ADMIN_TOKEN để tái sử dụng
4. **Postman**: Import collection để test UI-based
5. **Reset Data**: Luôn có thể reset và chạy lại script

---

## 📱 Postman Shortcuts

```
Import: test_vendors_postman.json
Variables: Set baseUrl and adminToken
Run: Send requests one by one or use Runner
Verify: Check response status and body
```

---

**Last Updated**: Feb 2026  
**Quick Access**: Keep this file open while testing!
