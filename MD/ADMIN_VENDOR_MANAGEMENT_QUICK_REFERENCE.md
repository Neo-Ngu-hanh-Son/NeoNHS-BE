# Admin Vendor Management - Quick Reference

## Base URL
```
/api/admin/vendors
```

## Authentication
All endpoints require Admin JWT token in Authorization header.

---

## Quick API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/vendors` | Create vendor account |
| GET | `/api/admin/vendors` | Get all vendors (paginated) |
| GET | `/api/admin/vendors/{id}` | Get vendor by ID |
| PUT | `/api/admin/vendors/{id}` | Update vendor profile |
| DELETE | `/api/admin/vendors/{id}` | Delete vendor (soft delete) |
| POST | `/api/admin/vendors/{id}/ban` | Ban vendor |
| POST | `/api/admin/vendors/{id}/unban` | Unban vendor |
| GET | `/api/admin/vendors/search` | Search vendors |
| GET | `/api/admin/vendors/filter` | Advanced filter |
| GET | `/api/admin/vendors/filter/verified` | Filter by verification |
| GET | `/api/admin/vendors/filter/banned` | Filter by banned status |
| GET | `/api/admin/vendors/filter/active` | Filter by active status |

---

## cURL Examples

### Create Vendor
```bash
curl -X POST "http://localhost:8080/api/admin/vendors" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullname": "John Doe",
    "email": "vendor@example.com",
    "password": "SecurePass123",
    "businessName": "ABC Workshops",
    "phoneNumber": "+1234567890",
    "isVerified": true
  }'
```

### Get All Vendors
```bash
curl -X GET "http://localhost:8080/api/admin/vendors?page=1&size=10" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Search Vendors
```bash
curl -X GET "http://localhost:8080/api/admin/vendors/search?keyword=workshop" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Ban Vendor
```bash
curl -X POST "http://localhost:8080/api/admin/vendors/{id}/ban" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Policy violation"}'
```

### Unban Vendor
```bash
curl -X POST "http://localhost:8080/api/admin/vendors/{id}/unban" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Advanced Filter
```bash
curl -X GET "http://localhost:8080/api/admin/vendors/filter?isVerified=true&isBanned=false&page=1" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

## Common Use Cases

### 1. View all active vendors
```
GET /api/admin/vendors/filter/active?isActive=true
```

### 2. Find unverified vendors
```
GET /api/admin/vendors/filter/verified?isVerified=false
```

### 3. Search by business name
```
GET /api/admin/vendors/search?keyword=workshop
```

### 4. Ban a vendor
```
POST /api/admin/vendors/{id}/ban
Body: {"reason": "Terms violation"}
```

### 5. Update vendor details
```
PUT /api/admin/vendors/{id}
Body: {"isVerified": true, "isActive": true}
```

---

## Status Flags Explained

| Flag | Meaning |
|------|---------|
| `isVerified` | Vendor profile is verified by admin |
| `isActive` | Vendor account is active |
| `isBanned` | Vendor is banned from the platform |

**Common Combinations:**
- Active Vendor: `isActive=true, isBanned=false`
- Banned Vendor: `isActive=false, isBanned=true`
- Unverified Vendor: `isVerified=false`

---

## Default Pagination

- **Page**: 1 (1-based)
- **Size**: 10 items
- **Sort By**: createdAt
- **Sort Direction**: DESC (newest first)

---

## Error Codes

| Code | Meaning |
|------|---------|
| 201 | Created successfully |
| 200 | Success |
| 400 | Bad request (validation error) |
| 401 | Unauthorized (no/invalid token) |
| 403 | Forbidden (not admin) |
| 404 | Vendor not found |
| 500 | Internal server error |

---

## Testing Tips

1. **Get Admin Token First**
   - Login as admin
   - Copy JWT token from response

2. **Use Swagger UI**
   - Navigate to `/swagger-ui.html`
   - Click "Authorize"
   - Paste admin token
   - Try endpoints interactively

3. **Postman Collection**
   - Import the endpoints
   - Set collection-level auth
   - Use variables for baseUrl and token

4. **Check Logs**
   - All operations are logged
   - Check console for detailed info

---

## Development Checklist

✅ Service interface created
✅ Service implementation with logging
✅ Custom repository queries
✅ Controller with Swagger annotations
✅ Request/Response DTOs
✅ Validation rules
✅ Error handling
✅ Pagination support
✅ Search functionality
✅ Multiple filter options
✅ Ban/Unban functionality
✅ Soft delete support
✅ Documentation

---

## Files Created/Modified

### New Files
- `dto/request/admin/CreateVendorByAdminRequest.java`
- `dto/request/admin/UpdateVendorByAdminRequest.java`
- `dto/request/admin/BanVendorRequest.java`
- `service/AdminVendorManagementService.java`
- `service/impl/AdminVendorManagementServiceImpl.java`
- `controller/AdminVendorManagementController.java`

### Modified Files
- `repository/VendorProfileRepository.java` (added custom queries)
- `dto/response/auth/VendorProfileResponse.java` (added status fields)
- `service/impl/VendorProfileServiceImpl.java` (updated mapper)

---

## Next Steps

1. Test all endpoints
2. Verify permissions work correctly
3. Test validation rules
4. Test search and filter combinations
5. Add email notifications (future)
6. Add activity logging (future)
7. Create admin dashboard UI
