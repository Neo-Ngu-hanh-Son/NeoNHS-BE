# Admin Vendor Management API Documentation

## Overview
This document provides comprehensive documentation for the Admin Vendor Management system. Admins can perform full CRUD operations, search, filter, ban/unban, and manage vendor accounts.

## Base URL
```
/api/admin/vendors
```

## Authentication & Authorization
- **Required**: Admin JWT Token
- **Role**: ADMIN
- **Header**: `Authorization: Bearer <admin-jwt-token>`

---

## API Endpoints

### 1. Create Vendor Account

**POST** `/api/admin/vendors`

Creates a new vendor account with profile information.

**Request Body:**
```json
{
  "fullname": "John Doe",
  "email": "vendor@example.com",
  "password": "SecurePass123",
  "phoneNumber": "+1234567890",
  "businessName": "ABC Workshop Services",
  "description": "Professional workshop services provider",
  "address": "123 Main St, City",
  "latitude": "10.762622",
  "longitude": "106.660172",
  "taxCode": "TAX123456789",
  "bankName": "ABC Bank",
  "bankAccountNumber": "1234567890",
  "bankAccountName": "ABC Workshop Services",
  "isVerified": true
}
```

**Validation Rules:**
- `fullname`: Required, 2-100 characters
- `email`: Required, valid email format
- `password`: Required, min 8 characters, must contain at least one letter and one number
- `phoneNumber`: Optional, 10-15 digits
- `businessName`: Required, 2-200 characters

**Response:**
```json
{
  "status": "success",
  "statusCode": 201,
  "message": "Vendor created successfully",
  "data": {
    "id": "uuid",
    "email": "vendor@example.com",
    "fullname": "John Doe",
    "phoneNumber": "+1234567890",
    "avatarUrl": null,
    "role": "VENDOR",
    "businessName": "ABC Workshop Services",
    "description": "Professional workshop services provider",
    "address": "123 Main St, City",
    "latitude": "10.762622",
    "longitude": "106.660172",
    "taxCode": "TAX123456789",
    "bankName": "ABC Bank",
    "bankAccountNumber": "1234567890",
    "bankAccountName": "ABC Workshop Services",
    "isVerifiedVendor": true,
    "isActive": true,
    "isBanned": false
  }
}
```

---

### 2. Get All Vendors

**GET** `/api/admin/vendors`

Retrieves all vendors with pagination and sorting.

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 1 | Page number (1-based) |
| size | int | 10 | Items per page |
| sortBy | string | createdAt | Field to sort by |
| sortDirection | string | DESC | ASC or DESC |

**Example Request:**
```http
GET /api/admin/vendors?page=1&size=10&sortBy=createdAt&sortDirection=DESC
```

**Response:**
```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Vendors retrieved successfully",
  "data": {
    "content": [...],
    "pageable": {...},
    "totalPages": 5,
    "totalElements": 50,
    "size": 10,
    "number": 0
  }
}
```

---

### 3. Get Vendor by ID

**GET** `/api/admin/vendors/{id}`

Retrieves a specific vendor by their ID.

**Path Parameter:**
- `id`: UUID of the vendor

**Response:**
```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Vendor retrieved successfully",
  "data": {
    "id": "uuid",
    "email": "vendor@example.com",
    ...
  }
}
```

---

### 4. Update Vendor Profile

**PUT** `/api/admin/vendors/{id}`

Updates a vendor's profile information.

**Path Parameter:**
- `id`: UUID of the vendor

**Request Body:**
```json
{
  "fullname": "Updated Name",
  "phoneNumber": "+9876543210",
  "avatarUrl": "https://example.com/avatar.jpg",
  "businessName": "Updated Business Name",
  "description": "Updated description",
  "address": "New Address",
  "latitude": "10.123456",
  "longitude": "106.123456",
  "taxCode": "NEWTAX123",
  "bankName": "New Bank",
  "bankAccountNumber": "9876543210",
  "bankAccountName": "New Account",
  "isVerified": true,
  "isActive": true
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Response:**
```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Vendor updated successfully",
  "data": {
    "id": "uuid",
    ...
  }
}
```

---

### 5. Ban Vendor

**POST** `/api/admin/vendors/{id}/ban`

Bans a vendor account.

**Path Parameter:**
- `id`: UUID of the vendor

**Request Body (Optional):**
```json
{
  "reason": "Violation of terms and conditions"
}
```

**Effect:**
- Sets `isBanned` to `true`
- Sets `isActive` to `false`
- Vendor cannot login or perform actions

**Response:**
```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Vendor banned successfully",
  "data": {
    "id": "uuid",
    "isBanned": true,
    "isActive": false,
    ...
  }
}
```

---

### 6. Unban Vendor

**POST** `/api/admin/vendors/{id}/unban`

Unbans a previously banned vendor account.

**Path Parameter:**
- `id`: UUID of the vendor

**Effect:**
- Sets `isBanned` to `false`
- Sets `isActive` to `true`
- Vendor can login and perform actions again

**Response:**
```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Vendor unbanned successfully",
  "data": {
    "id": "uuid",
    "isBanned": false,
    "isActive": true,
    ...
  }
}
```

---

### 7. Delete Vendor (Soft Delete)

**DELETE** `/api/admin/vendors/{id}`

Soft deletes a vendor account (deactivates).

**Path Parameter:**
- `id`: UUID of the vendor

**Effect:**
- Sets `isActive` to `false`
- Sets `isBanned` to `true`
- Data is preserved in database

**Response:**
```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Vendor deleted successfully",
  "data": null
}
```

---

### 8. Search Vendors

**GET** `/api/admin/vendors/search`

Searches vendors by keyword (matches business name, fullname, or email).

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| keyword | string | Yes | Search keyword |
| page | int | No | Page number (default: 1) |
| size | int | No | Page size (default: 10) |
| sortBy | string | No | Sort field (default: createdAt) |
| sortDirection | string | No | ASC/DESC (default: DESC) |

**Example Request:**
```http
GET /api/admin/vendors/search?keyword=workshop&page=1&size=10
```

**Response:**
```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Search results retrieved successfully",
  "data": {
    "content": [...],
    ...
  }
}
```

---

### 9. Advanced Filter

**GET** `/api/admin/vendors/filter`

Filters vendors with multiple criteria.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| keyword | string | No | Search keyword |
| isVerified | boolean | No | Verification status |
| isBanned | boolean | No | Banned status |
| isActive | boolean | No | Active status |
| page | int | No | Page number (default: 1) |
| size | int | No | Page size (default: 10) |
| sortBy | string | No | Sort field (default: createdAt) |
| sortDirection | string | No | ASC/DESC (default: DESC) |

**Example Request:**
```http
GET /api/admin/vendors/filter?keyword=workshop&isVerified=true&isBanned=false&page=1
```

**Response:**
```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Filtered results retrieved successfully",
  "data": {
    "content": [...],
    ...
  }
}
```

---

### 10. Filter by Verification Status

**GET** `/api/admin/vendors/filter/verified`

Filters vendors by verification status.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| isVerified | boolean | Yes | true or false |
| page | int | No | Page number |
| size | int | No | Page size |
| sortBy | string | No | Sort field |
| sortDirection | string | No | Sort direction |

**Example Request:**
```http
GET /api/admin/vendors/filter/verified?isVerified=true&page=1&size=10
```

---

### 11. Filter by Banned Status

**GET** `/api/admin/vendors/filter/banned`

Filters vendors by banned status.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| isBanned | boolean | Yes | true or false |
| page | int | No | Page number |
| size | int | No | Page size |
| sortBy | string | No | Sort field |
| sortDirection | string | No | Sort direction |

**Example Request:**
```http
GET /api/admin/vendors/filter/banned?isBanned=false&page=1&size=10
```

---

### 12. Filter by Active Status

**GET** `/api/admin/vendors/filter/active`

Filters vendors by active status.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| isActive | boolean | Yes | true or false |
| page | int | No | Page number |
| size | int | No | Page size |
| sortBy | string | No | Sort field |
| sortDirection | string | No | Sort direction |

**Example Request:**
```http
GET /api/admin/vendors/filter/active?isActive=true&page=1&size=10
```

---

## Error Responses

### 400 Bad Request
```json
{
  "status": "error",
  "statusCode": 400,
  "message": "Email already exists in the system",
  "data": null
}
```

### 401 Unauthorized
```json
{
  "status": "error",
  "statusCode": 401,
  "message": "Unauthorized",
  "data": null
}
```

### 403 Forbidden
```json
{
  "status": "error",
  "statusCode": 403,
  "message": "Access Denied - Admin role required",
  "data": null
}
```

### 404 Not Found
```json
{
  "status": "error",
  "statusCode": 404,
  "message": "VendorProfile not found with id: <uuid>",
  "data": null
}
```

---

## Sort Fields

You can sort by any of these fields:
- `createdAt` - Creation date (default)
- `updatedAt` - Last update date
- `businessName` - Business name
- `email` - Vendor email (via user.email)
- `fullname` - Vendor fullname (via user.fullname)
- `isVerified` - Verification status

---

## Testing with Swagger

1. Navigate to `/swagger-ui.html`
2. Locate **Admin Vendor Management** section
3. Click "Authorize" and enter admin JWT token
4. Try out any endpoint

---

## Testing with Postman

### Setup
1. Create a new collection: "Admin Vendor Management"
2. Add collection variable: `baseUrl` = `http://localhost:8080`
3. Add collection variable: `adminToken` = `<your-admin-jwt>`

### Collection Authorization
- Type: Bearer Token
- Token: `{{adminToken}}`

### Example Requests

**Create Vendor:**
```
POST {{baseUrl}}/api/admin/vendors
Body: raw JSON (see request body above)
```

**Get All Vendors:**
```
GET {{baseUrl}}/api/admin/vendors?page=1&size=10
```

**Search Vendors:**
```
GET {{baseUrl}}/api/admin/vendors/search?keyword=workshop
```

**Ban Vendor:**
```
POST {{baseUrl}}/api/admin/vendors/{vendorId}/ban
Body: {"reason": "Policy violation"}
```

---

## Use Cases

### 1. Admin creates a new vendor manually
```
POST /api/admin/vendors
```

### 2. Admin views all vendors
```
GET /api/admin/vendors?page=1&size=20&sortBy=createdAt&sortDirection=DESC
```

### 3. Admin searches for a specific vendor
```
GET /api/admin/vendors/search?keyword=workshop
```

### 4. Admin finds unverified vendors
```
GET /api/admin/vendors/filter/verified?isVerified=false
```

### 5. Admin bans a vendor for policy violation
```
POST /api/admin/vendors/{id}/ban
Body: {"reason": "Violated community guidelines"}
```

### 6. Admin finds all banned vendors
```
GET /api/admin/vendors/filter/banned?isBanned=true
```

### 7. Admin unbans a vendor after review
```
POST /api/admin/vendors/{id}/unban
```

### 8. Admin updates vendor information
```
PUT /api/admin/vendors/{id}
Body: {updated fields}
```

### 9. Admin finds active verified vendors
```
GET /api/admin/vendors/filter?isVerified=true&isActive=true&isBanned=false
```

### 10. Admin soft deletes a vendor
```
DELETE /api/admin/vendors/{id}
```

---

## Implementation Files

### DTOs
- `CreateVendorByAdminRequest.java` - Request DTO for creating vendor
- `UpdateVendorByAdminRequest.java` - Request DTO for updating vendor
- `BanVendorRequest.java` - Request DTO for banning vendor
- `VendorProfileResponse.java` - Response DTO (updated with status fields)

### Service Layer
- `AdminVendorManagementService.java` - Service interface
- `AdminVendorManagementServiceImpl.java` - Service implementation

### Repository
- `VendorProfileRepository.java` - Updated with custom queries

### Controller
- `AdminVendorManagementController.java` - REST API controller

---

## Notes

1. **Soft Delete**: Delete operation doesn't remove data, just deactivates the account
2. **Ban vs Delete**: Ban is temporary and reversible, Delete is a permanent deactivation
3. **Password Requirements**: Min 8 characters, at least one letter and one number
4. **Phone Validation**: 10-15 digits format
5. **Default Status**: New vendors are active, verified, and not banned by default
6. **Search**: Case-insensitive, searches in business name, fullname, and email
7. **Pagination**: All list endpoints support pagination with 1-based page numbers

---

## Future Enhancements

1. Email notification when vendor is banned/unbanned
2. Ban history tracking
3. Vendor activity logs
4. Bulk operations (ban/unban multiple vendors)
5. Export vendor list to CSV/Excel
6. Advanced analytics dashboard
7. Vendor performance metrics
8. Automated verification process
