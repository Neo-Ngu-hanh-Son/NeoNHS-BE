# Admin Vendor Management - Testing Checklist

## Pre-Testing Setup

### ✅ Prerequisites
- [ ] Application is running on port 8080
- [ ] Database is connected and accessible
- [ ] Admin user account exists in database
- [ ] Admin JWT token is available

### 🔑 Get Admin Token
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "your-admin-password"
}
```
**Save the token from response for all subsequent requests**

---

## Test Suite 1: Basic CRUD Operations

### Test 1.1: Create Vendor ✅
```http
POST http://localhost:8080/api/admin/vendors
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "fullname": "Test Vendor One",
  "email": "testvendor1@example.com",
  "password": "TestPass123",
  "phoneNumber": "+1234567890",
  "businessName": "Test Workshop Services",
  "description": "Professional workshop provider",
  "address": "123 Test Street",
  "latitude": "10.762622",
  "longitude": "106.660172",
  "taxCode": "TAX123456",
  "bankName": "Test Bank",
  "bankAccountNumber": "1234567890",
  "bankAccountName": "Test Workshop",
  "isVerified": true
}
```

**Expected Result:**
- [ ] Status: 201 Created
- [ ] Response contains vendor ID
- [ ] Email matches request
- [ ] isVerified is true
- [ ] isBanned is false
- [ ] isActive is true

**Save vendor ID for next tests:** `_____________________`

---

### Test 1.2: Get All Vendors ✅
```http
GET http://localhost:8080/api/admin/vendors?page=1&size=10
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Response contains pagination info
- [ ] Content array is present
- [ ] totalElements > 0
- [ ] Created vendor is in the list

---

### Test 1.3: Get Vendor by ID ✅
```http
GET http://localhost:8080/api/admin/vendors/{vendor-id}
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Vendor details match created vendor
- [ ] All fields are present

---

### Test 1.4: Update Vendor ✅
```http
PUT http://localhost:8080/api/admin/vendors/{vendor-id}
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "fullname": "Updated Vendor Name",
  "businessName": "Updated Business Name",
  "description": "Updated description",
  "isVerified": false
}
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Fullname is updated
- [ ] Business name is updated
- [ ] isVerified is now false
- [ ] Other fields remain unchanged

---

## Test Suite 2: Search Functionality

### Test 2.1: Search by Business Name ✅
```http
GET http://localhost:8080/api/admin/vendors/search?keyword=workshop
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Returns vendors matching "workshop"
- [ ] Search is case-insensitive

---

### Test 2.2: Search by Email ✅
```http
GET http://localhost:8080/api/admin/vendors/search?keyword=testvendor1
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Returns vendors with email containing "testvendor1"

---

### Test 2.3: Search with No Results ✅
```http
GET http://localhost:8080/api/admin/vendors/search?keyword=nonexistent12345
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Content array is empty
- [ ] totalElements is 0

---

## Test Suite 3: Filter Operations

### Test 3.1: Filter by Verified Status ✅
```http
GET http://localhost:8080/api/admin/vendors/filter/verified?isVerified=true
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] All returned vendors have isVerified = true

---

### Test 3.2: Filter by Unverified Status ✅
```http
GET http://localhost:8080/api/admin/vendors/filter/verified?isVerified=false
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] All returned vendors have isVerified = false
- [ ] Updated vendor should appear here

---

### Test 3.3: Filter by Active Status ✅
```http
GET http://localhost:8080/api/admin/vendors/filter/active?isActive=true
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] All returned vendors have isActive = true

---

### Test 3.4: Filter by Banned Status ✅
```http
GET http://localhost:8080/api/admin/vendors/filter/banned?isBanned=false
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] All returned vendors have isBanned = false

---

### Test 3.5: Advanced Combined Filter ✅
```http
GET http://localhost:8080/api/admin/vendors/filter?keyword=test&isVerified=false&isBanned=false&isActive=true
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Results match all criteria
- [ ] Keyword search works with filters

---

## Test Suite 4: Ban/Unban Operations

### Test 4.1: Ban Vendor ✅
```http
POST http://localhost:8080/api/admin/vendors/{vendor-id}/ban
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "reason": "Testing ban functionality"
}
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] isBanned is true
- [ ] isActive is false

---

### Test 4.2: Verify Banned Vendor Cannot Be Banned Again ✅
```http
POST http://localhost:8080/api/admin/vendors/{vendor-id}/ban
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 400 Bad Request
- [ ] Error message: "Vendor is already banned"

---

### Test 4.3: Filter Shows Banned Vendor ✅
```http
GET http://localhost:8080/api/admin/vendors/filter/banned?isBanned=true
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Banned vendor appears in results

---

### Test 4.4: Unban Vendor ✅
```http
POST http://localhost:8080/api/admin/vendors/{vendor-id}/unban
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] isBanned is false
- [ ] isActive is true

---

### Test 4.5: Verify Unbanned Vendor Cannot Be Unbanned Again ✅
```http
POST http://localhost:8080/api/admin/vendors/{vendor-id}/unban
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 400 Bad Request
- [ ] Error message: "Vendor is not banned"

---

## Test Suite 5: Pagination & Sorting

### Test 5.1: Pagination Page 1 ✅
```http
GET http://localhost:8080/api/admin/vendors?page=1&size=5
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] size = 5 or less
- [ ] number = 0 (0-indexed internally)
- [ ] Pagination metadata present

---

### Test 5.2: Pagination Page 2 ✅
```http
GET http://localhost:8080/api/admin/vendors?page=2&size=5
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Different vendors than page 1
- [ ] number = 1

---

### Test 5.3: Sort by Business Name ASC ✅
```http
GET http://localhost:8080/api/admin/vendors?sortBy=businessName&sortDirection=ASC
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Results sorted alphabetically by business name

---

### Test 5.4: Sort by Created Date DESC ✅
```http
GET http://localhost:8080/api/admin/vendors?sortBy=createdAt&sortDirection=DESC
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Newest vendors appear first

---

## Test Suite 6: Delete Operation

### Test 6.1: Soft Delete Vendor ✅
```http
DELETE http://localhost:8080/api/admin/vendors/{vendor-id}
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Message: "Vendor deleted successfully"

---

### Test 6.2: Verify Deleted Vendor Status ✅
```http
GET http://localhost:8080/api/admin/vendors/{vendor-id}
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] isActive is false
- [ ] isBanned is true

---

### Test 6.3: Deleted Vendor in Inactive Filter ✅
```http
GET http://localhost:8080/api/admin/vendors/filter/active?isActive=false
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Deleted vendor appears in results

---

## Test Suite 7: Validation Tests

### Test 7.1: Create Vendor with Invalid Email ✅
```http
POST http://localhost:8080/api/admin/vendors
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "fullname": "Test",
  "email": "invalid-email",
  "password": "TestPass123",
  "businessName": "Test Business"
}
```

**Expected Result:**
- [ ] Status: 400 Bad Request
- [ ] Error message about invalid email format

---

### Test 7.2: Create Vendor with Weak Password ✅
```http
POST http://localhost:8080/api/admin/vendors
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "fullname": "Test",
  "email": "test@example.com",
  "password": "weak",
  "businessName": "Test Business"
}
```

**Expected Result:**
- [ ] Status: 400 Bad Request
- [ ] Error about password requirements

---

### Test 7.3: Create Vendor with Duplicate Email ✅
```http
POST http://localhost:8080/api/admin/vendors
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "fullname": "Test",
  "email": "testvendor1@example.com",
  "password": "TestPass123",
  "businessName": "Test Business"
}
```

**Expected Result:**
- [ ] Status: 400 Bad Request
- [ ] Error message: "Email already exists"

---

### Test 7.4: Create Vendor with Missing Required Fields ✅
```http
POST http://localhost:8080/api/admin/vendors
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "email": "test@example.com"
}
```

**Expected Result:**
- [ ] Status: 400 Bad Request
- [ ] Error about missing required fields

---

## Test Suite 8: Authorization Tests

### Test 8.1: Access Without Token ✅
```http
GET http://localhost:8080/api/admin/vendors
```

**Expected Result:**
- [ ] Status: 401 Unauthorized

---

### Test 8.2: Access With Invalid Token ✅
```http
GET http://localhost:8080/api/admin/vendors
Authorization: Bearer invalid-token-12345
```

**Expected Result:**
- [ ] Status: 401 Unauthorized

---

### Test 8.3: Access With Non-Admin Token ✅
*If you have a vendor/customer token*
```http
GET http://localhost:8080/api/admin/vendors
Authorization: Bearer <vendor-or-customer-token>
```

**Expected Result:**
- [ ] Status: 403 Forbidden
- [ ] Error: "Access Denied"

---

## Test Suite 9: Edge Cases

### Test 9.1: Get Non-Existent Vendor ✅
```http
GET http://localhost:8080/api/admin/vendors/00000000-0000-0000-0000-000000000000
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 404 Not Found
- [ ] Error message about vendor not found

---

### Test 9.2: Update Non-Existent Vendor ✅
```http
PUT http://localhost:8080/api/admin/vendors/00000000-0000-0000-0000-000000000000
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "fullname": "Test"
}
```

**Expected Result:**
- [ ] Status: 404 Not Found

---

### Test 9.3: Ban Non-Existent Vendor ✅
```http
POST http://localhost:8080/api/admin/vendors/00000000-0000-0000-0000-000000000000/ban
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 404 Not Found

---

### Test 9.4: Empty Search Result ✅
```http
GET http://localhost:8080/api/admin/vendors/search?keyword=
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Returns all vendors (same as GET /api/admin/vendors)

---

### Test 9.5: Large Page Request ✅
```http
GET http://localhost:8080/api/admin/vendors?page=1&size=1000
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Status: 200 OK
- [ ] Returns up to 1000 vendors
- [ ] No performance issues

---

## Test Suite 10: Integration Tests

### Test 10.1: Create Multiple Vendors ✅
Create 5 vendors with different statuses

**Expected Result:**
- [ ] All vendors created successfully
- [ ] Each has unique ID

---

### Test 10.2: Search and Filter Multiple Vendors ✅
```http
GET http://localhost:8080/api/admin/vendors/filter?keyword=test&isVerified=true
Authorization: Bearer <admin-token>
```

**Expected Result:**
- [ ] Returns only verified vendors matching "test"

---

### Test 10.3: Ban Multiple Vendors ✅
Ban 2-3 vendors

**Expected Result:**
- [ ] All ban operations successful
- [ ] Filter by banned status shows all banned vendors

---

## Summary

### Total Tests: 47
- [ ] Basic CRUD: 4 tests
- [ ] Search: 3 tests
- [ ] Filter: 5 tests
- [ ] Ban/Unban: 5 tests
- [ ] Pagination: 4 tests
- [ ] Delete: 3 tests
- [ ] Validation: 4 tests
- [ ] Authorization: 3 tests
- [ ] Edge Cases: 5 tests
- [ ] Integration: 3 tests

---

## Test Results Summary

| Category | Passed | Failed | Total |
|----------|--------|--------|-------|
| Basic CRUD | ___ | ___ | 4 |
| Search | ___ | ___ | 3 |
| Filter | ___ | ___ | 5 |
| Ban/Unban | ___ | ___ | 5 |
| Pagination | ___ | ___ | 4 |
| Delete | ___ | ___ | 3 |
| Validation | ___ | ___ | 4 |
| Authorization | ___ | ___ | 3 |
| Edge Cases | ___ | ___ | 5 |
| Integration | ___ | ___ | 3 |
| **TOTAL** | ___ | ___ | **47** |

---

## Notes

- All tests should be run in sequence
- Save vendor IDs from creation tests for use in later tests
- Some tests depend on previous test results
- Use Postman or Swagger UI for easier testing
- Check application logs for detailed error information

---

## Sign-Off

- **Tester Name:** _____________________
- **Date:** _____________________
- **Overall Status:** ⬜ PASS  ⬜ FAIL
- **Comments:** 
  ```
  
  
  
  ```
