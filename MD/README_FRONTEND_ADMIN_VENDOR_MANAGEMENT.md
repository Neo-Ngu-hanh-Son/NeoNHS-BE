# Frontend Implementation Guide: Admin Vendor Management System

## Overview
This guide explains how to build the frontend UI for the Admin Vendor Management system based on the backend API endpoints. The UI allows administrators to view, create, manage, and monitor all vendors in the system, including their workshop templates.

---

## 📋 Table of Contents
1. [API Endpoints Reference](#api-endpoints-reference)
2. [Data Models & Types](#data-models--types)
3. [Folder Structure](#folder-structure)
4. [Components to Build](#components-to-build)
5. [Pages to Build](#pages-to-build)
6. [API Service Layer](#api-service-layer)
7. [State Management](#state-management)
8. [User Flows](#user-flows)
9. [Important Business Rules](#important-business-rules)

---

## 🔌 API Endpoints Reference

### Vendor Management Endpoints

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/vendors` | ADMIN | Get all vendors (paginated) |
| GET | `/api/admin/vendors/{id}` | ADMIN | Get vendor details by ID |
| POST | `/api/admin/vendors` | ADMIN | Create new vendor account |
| PUT | `/api/admin/vendors/{id}` | ADMIN | Update vendor profile |
| DELETE | `/api/admin/vendors/{id}` | ADMIN | Soft delete vendor |
| POST | `/api/admin/vendors/{id}/ban` | ADMIN | Ban vendor account |
| POST | `/api/admin/vendors/{id}/unban` | ADMIN | Unban vendor account |
| GET | `/api/admin/vendors/search` | ADMIN | Search vendors by keyword |
| GET | `/api/admin/vendors/filter` | ADMIN | Advanced filter with multiple criteria |
| GET | `/api/admin/vendors/filter/verified` | ADMIN | Filter by verification status |
| GET | `/api/admin/vendors/filter/banned` | ADMIN | Filter by banned status |
| GET | `/api/admin/vendors/filter/active` | ADMIN | Filter by active status |

### Workshop Template Management (Admin View)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/vendors/{id}/workshop-templates` | ADMIN | Get all templates for specific vendor (paginated) |
| GET | `/api/admin/vendors/workshop-templates` | ADMIN | Get all workshop templates (all vendors) |
| POST | `/api/admin/vendors/workshop-templates/{id}/approve` | ADMIN | Approve pending template |
| POST | `/api/admin/vendors/workshop-templates/{id}/reject` | ADMIN | Reject pending template |

### Pagination Parameters (All Endpoints)
- `page`: Page number (1-based, default: 1)
- `size`: Items per page (default: 10)
- `sortBy`: Field to sort by (default: "createdAt")
- `sortDirection`: Sort direction - "ASC" or "DESC" (default: "DESC")

---

## 📦 Data Models & Types

### TypeScript Interfaces

```typescript
// Vendor Profile Response (extends User)
interface VendorProfileResponse {
  // User fields (inherited)
  id: string; // UUID
  email: string;
  fullname: string;
  phoneNumber: string;
  avatarUrl: string | null;
  role: string; // "VENDOR"
  
  // Vendor-specific fields
  userId: string; // UUID - reference to User entity
  businessName: string;
  description: string | null;
  address: string | null;
  latitude: string | null;
  longitude: string | null;
  taxCode: string | null;
  bankName: string | null;
  bankAccountNumber: string | null;
  bankAccountName: string | null;
  isVerifiedVendor: boolean;
  isActive: boolean;
  isBanned: boolean;
}

// Create Vendor Request
interface CreateVendorRequest {
  // User credentials
  email: string; // Valid email format
  password: string; // Min 8 chars, must contain letter and number
  fullname: string; // Required
  phoneNumber: string; // Optional
  
  // Vendor profile
  businessName: string; // Required
  description?: string;
  address?: string;
  taxCode?: string;
  bankName?: string;
  bankAccountNumber?: string;
  bankAccountName?: string;
}

// Update Vendor Request
interface UpdateVendorRequest {
  fullname?: string;
  phoneNumber?: string;
  businessName?: string;
  description?: string;
  address?: string;
  latitude?: string;
  longitude?: string;
  taxCode?: string;
  bankName?: string;
  bankAccountNumber?: string;
  bankAccountName?: string;
  isVerifiedVendor?: boolean; // Admin can verify vendors
  isActive?: boolean; // Admin can activate/deactivate
}

// Ban Vendor Request
interface BanVendorRequest {
  reason?: string; // Optional ban reason
}

// Paginated Response
interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

// API Response Wrapper
interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: T;
  timestamp: string;
}

// Filter Options
interface VendorFilterOptions {
  keyword?: string; // Search in name, email, business name
  isVerified?: boolean;
  isBanned?: boolean;
  isActive?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}
```

---

## 📁 Folder Structure

```
src/
├── pages/
│   └── admin/
│       └── vendors/
│           ├── VendorManagementPage.tsx        # Main page with table
│           └── components/
│               ├── VendorTable.tsx             # Data table component
│               ├── VendorDetailModal.tsx       # View vendor details
│               ├── CreateVendorModal.tsx       # Create new vendor
│               ├── EditVendorModal.tsx         # Edit vendor profile
│               ├── BanVendorModal.tsx          # Ban vendor with reason
│               ├── VendorFilters.tsx           # Search and filter bar
│               ├── VendorStats.tsx             # Statistics cards
│               └── VendorTemplatesList.tsx     # Templates in modal
│
├── services/
│   └── api/
│       ├── adminVendorService.ts               # Vendor CRUD API calls
│       └── adminWorkshopService.ts             # Workshop template APIs
│
├── hooks/
│   └── admin/
│       ├── useVendorList.ts                    # Vendor list with pagination
│       ├── useVendorDetail.ts                  # Single vendor detail
│       ├── useVendorFilters.ts                 # Filter state management
│       └── useVendorActions.ts                 # Ban, unban, delete actions
│
├── types/
│   └── admin/
│       └── vendor.types.ts                     # All vendor-related types
│
└── utils/
    ├── pagination.ts                            # Pagination helpers
    └── validators.ts                            # Form validation rules
```

---

## 🧩 Components to Build

### 1. VendorManagementPage (Main Container)
**Purpose:** Main page that orchestrates all vendor management features

**Layout Structure:**
```
┌─────────────────────────────────────────────────────────┐
│  Admin Vendor Management                                 │
├─────────────────────────────────────────────────────────┤
│  [VendorStats Component - 4 stat cards in a row]        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │Total: 150│ │Active: 120│ │Banned: 5│ │Pending: 25│  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
├─────────────────────────────────────────────────────────┤
│  [VendorFilters Component]                              │
│  Search: [__________] [🔍]  [+ Create Vendor]          │
│  Filters: [All ▼] [Verified ▼] [Status ▼]             │
├─────────────────────────────────────────────────────────┤
│  [VendorTable Component]                                │
│  ID  Email        Business    Status  Verified  Actions │
│  ... ...          ...         ...     ...       [⋮]     │
├─────────────────────────────────────────────────────────┤
│  [Pagination] Showing 1-10 of 150  [< 1 2 3 4 5 >]     │
└─────────────────────────────────────────────────────────┘
```

**Key Features:**
- Display summary statistics at the top
- Integrated search and filter bar
- Responsive data table
- Action buttons for each vendor
- Pagination controls

---

### 2. VendorStats Component
**Purpose:** Display key metrics about vendors

**Stats to Display:**
```typescript
interface VendorStats {
  total: number;
  active: number;
  banned: number;
  pendingVerification: number;
}
```

**Design:**
- 4 cards in a row (responsive: 2x2 on mobile)
- Each card shows:
  - Icon (relevant to the stat)
  - Large number (the count)
  - Label (descriptive text)
  - Trend indicator (optional: +5% this month)

---

### 3. VendorFilters Component
**Purpose:** Search bar and filter controls

**Features:**
- **Search Input:**
  - Placeholder: "Search by name, email, or business name..."
  - Real-time or debounced search
  - Clear button when text is entered

- **Filter Dropdowns:**
  - **Verification Status:** All | Verified | Not Verified
  - **Ban Status:** All | Active | Banned
  - **Active Status:** All | Active | Inactive

- **Action Buttons:**
  - "Create Vendor" button (primary, top-right)
  - "Clear Filters" button (when filters are applied)
  - "Export" button (optional: export vendor list to CSV)

**State Management:**
```typescript
const [filters, setFilters] = useState<VendorFilterOptions>({
  keyword: '',
  isVerified: undefined,
  isBanned: undefined,
  isActive: undefined,
  page: 1,
  size: 10,
  sortBy: 'createdAt',
  sortDirection: 'DESC'
});
```

---

### 4. VendorTable Component
**Purpose:** Display vendors in a sortable, paginated table

**Columns:**
| Column | Width | Sortable | Description |
|--------|-------|----------|-------------|
| Avatar | 60px | No | Vendor avatar thumbnail |
| Business Name | 200px | Yes | Business name |
| Owner Name | 150px | Yes | Vendor full name |
| Email | 200px | Yes | Contact email |
| Phone | 120px | No | Phone number |
| Status | 100px | No | Badge: Active/Inactive |
| Verified | 80px | Yes | ✓ or ✗ icon |
| Banned | 80px | Yes | ✓ or ✗ icon |
| Created | 120px | Yes | Date joined |
| Actions | 100px | No | Action dropdown menu |

**Status Badges:**
```typescript
// Active vendor (green)
<Badge color="green">Active</Badge>

// Inactive vendor (gray)
<Badge color="gray">Inactive</Badge>

// Banned vendor (red)
<Badge color="red">Banned</Badge>
```

**Actions Menu (Three-dot dropdown):**
- 👁️ **View Details** - Opens VendorDetailModal
- ✏️ **Edit** - Opens EditVendorModal
- 🚫 **Ban** - Opens BanVendorModal (if not banned)
- ✅ **Unban** - Directly unbans (if banned)
- ✓ **Verify** - Mark as verified vendor
- 🗑️ **Delete** - Soft delete with confirmation

**Row Styling:**
- Banned vendors: Light red background
- Hover effect on rows
- Clickable rows (click to view details)

---

### 5. VendorDetailModal
**Purpose:** View comprehensive vendor information

**Modal Layout:**
```
┌─────────────────────────────────────────────────────────┐
│  Vendor Details                                    [✕]   │
├─────────────────────────────────────────────────────────┤
│  ┌───────────────┬─────────────────────────────────────┐│
│  │               │  Business Name: ABC Workshop Co.    ││
│  │   [Avatar]    │  Owner: John Doe                     ││
│  │   150x150     │  Email: john@abc.com                 ││
│  │               │  Phone: +84 123 456 789              ││
│  └───────────────┴─────────────────────────────────────┘│
│                                                           │
│  Status Badges:                                           │
│  [✓ Verified] [● Active] [Joined: Jan 15, 2026]         │
│                                                           │
│  Business Information:                                    │
│  ├─ Tax Code: 1234567890                                │
│  ├─ Address: 123 Main St, District 1, HCMC             │
│  ├─ Location: 10.7769° N, 106.7009° E                  │
│  └─ Description: Professional workshop provider...      │
│                                                           │
│  Banking Information:                                     │
│  ├─ Bank: Vietcombank                                   │
│  ├─ Account Number: 0123456789                          │
│  └─ Account Name: ABC Workshop Co., Ltd                 │
│                                                           │
│  Statistics:                                              │
│  ├─ Total Templates: 12                                 │
│  ├─ Active Templates: 8                                 │
│  ├─ Pending Templates: 2                                │
│  └─ Rejected Templates: 2                               │
│                                                           │
│  [View All Templates] [Edit Profile] [Ban Vendor]       │
└─────────────────────────────────────────────────────────┘
```

**Sections:**
1. **Header:** Avatar, business name, basic contact info
2. **Status Badges:** Visual indicators for verification, active, banned status
3. **Business Info:** Tax, address, description
4. **Banking Info:** Payment details
5. **Statistics:** Workshop template counts
6. **Action Buttons:**
   - "View All Templates" - Opens list of vendor's templates
   - "Edit Profile" - Opens EditVendorModal
   - "Ban Vendor" / "Unban Vendor" - Quick action

**Special Cases:**
- If banned, show red alert banner with ban reason
- If not verified, show yellow warning banner

---

### 6. CreateVendorModal
**Purpose:** Admin creates a new vendor account

**Form Fields:**

**Section 1: Account Credentials**
```typescript
// Email (required)
<Input 
  label="Email" 
  type="email" 
  placeholder="vendor@example.com"
  required 
/>

// Password (required)
<Input 
  label="Password" 
  type="password" 
  placeholder="Min 8 characters"
  helperText="Must contain at least 1 letter and 1 number"
  required 
/>

// Full Name (required)
<Input 
  label="Full Name" 
  placeholder="John Doe"
  required 
/>

// Phone Number (optional)
<Input 
  label="Phone Number" 
  placeholder="+84 123 456 789"
/>
```

**Section 2: Business Information**
```typescript
// Business Name (required)
<Input 
  label="Business Name" 
  placeholder="ABC Workshop Co."
  required 
/>

// Description (optional)
<Textarea 
  label="Business Description" 
  rows={4}
  placeholder="Describe the vendor's business..."
/>

// Address (optional)
<Input 
  label="Address" 
  placeholder="123 Main St, District 1, HCMC"
/>

// Tax Code (optional)
<Input 
  label="Tax Code" 
  placeholder="1234567890"
/>
```

**Section 3: Banking Information**
```typescript
// Bank Name (optional)
<Input 
  label="Bank Name" 
  placeholder="Vietcombank"
/>

// Account Number (optional)
<Input 
  label="Bank Account Number" 
  placeholder="0123456789"
/>

// Account Name (optional)
<Input 
  label="Account Holder Name" 
  placeholder="ABC Workshop Co., Ltd"
/>
```

**Validation Rules:**
```typescript
const passwordValidation = {
  minLength: 8,
  hasNumber: /\d/.test(password),
  hasLetter: /[a-zA-Z]/.test(password)
};
```

**Buttons:**
- "Cancel" - Close modal
- "Create Vendor" - Submit form (primary button)

---

### 7. EditVendorModal
**Purpose:** Edit existing vendor profile

**Similar to CreateVendorModal, but:**
- Pre-filled with existing data
- No password field (separate password reset flow)
- Additional field: **Verification Status** (checkbox)
- Additional field: **Active Status** (toggle)

**Form Sections:**
1. Basic Information (name, phone)
2. Business Information (all editable)
3. Banking Information (all editable)
4. Admin Controls:
   - ✓ Verified Vendor (checkbox)
   - ● Active Status (toggle)

---

### 8. BanVendorModal
**Purpose:** Ban a vendor with optional reason

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│  Ban Vendor                                        [✕]   │
├─────────────────────────────────────────────────────────┤
│  ⚠️ Warning                                              │
│  You are about to ban vendor: [Business Name]           │
│                                                           │
│  This action will:                                        │
│  • Prevent vendor from logging in                        │
│  • Hide all their workshop templates                     │
│  • Cancel all pending workshop sessions                  │
│                                                           │
│  Reason (Optional):                                       │
│  ┌─────────────────────────────────────────────────────┐│
│  │ [Textarea for ban reason]                           ││
│  │                                                     ││
│  │                                                     ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
│  [Cancel] [Ban Vendor]                                   │
└─────────────────────────────────────────────────────────┘
```

**Features:**
- Warning message explaining consequences
- Optional textarea for ban reason
- Confirmation required
- "Ban Vendor" button is red/danger color

---

### 9. VendorTemplatesList Component
**Purpose:** Display vendor's workshop templates in modal

**Triggered from:** VendorDetailModal "View All Templates" button

**Modal Layout:**
```
┌─────────────────────────────────────────────────────────┐
│  Workshop Templates - [Vendor Business Name]       [✕]   │
├─────────────────────────────────────────────────────────┤
│  Filter by Status: [All ▼] [PENDING] [ACTIVE] [REJECTED]│
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐│
│  │ [Image] Template Name                    [PENDING] ││
│  │         Short description...                        ││
│  │         Price: $50 | Duration: 2h | Rating: 4.5★   ││
│  │         [Approve] [Reject] [View Details]          ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
│  ┌─────────────────────────────────────────────────────┐│
│  │ [Image] Another Template                  [ACTIVE]  ││
│  │         Description...                              ││
│  │         Price: $75 | Duration: 3h | Rating: 4.8★   ││
│  │         [View Details] [Deactivate]                ││
│  └─────────────────────────────────────────────────────┘│
│                                                           │
│  [Pagination] 1-5 of 12                                  │
└─────────────────────────────────────────────────────────┘
```

**Features:**
- Filter tabs by status (All, PENDING, ACTIVE, REJECTED, DRAFT)
- Template cards with image, name, description, price
- Status badges with color coding:
  - DRAFT: Gray
  - PENDING: Yellow
  - ACTIVE: Green
  - REJECTED: Red
- Quick actions per template:
  - **PENDING:** [Approve] [Reject] buttons
  - **ACTIVE:** [View Details] [Deactivate] buttons
  - **REJECTED:** [View Details] [View Reject Reason]
- Pagination for large lists

---

## 📄 Pages to Build

### Main Page: `/admin/vendors`

**Page Structure:**
```typescript
// VendorManagementPage.tsx

const VendorManagementPage: React.FC = () => {
  // State for filters and pagination
  const [filters, setFilters] = useState<VendorFilterOptions>({
    page: 1,
    size: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  });

  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showBanModal, setShowBanModal] = useState(false);
  const [showTemplatesModal, setShowTemplatesModal] = useState(false);
  const [selectedVendor, setSelectedVendor] = useState<VendorProfileResponse | null>(null);

  // Fetch vendors with current filters
  const { data, isLoading, error, refetch } = useVendorList(filters);

  // Calculate stats
  const stats = useMemo(() => calculateVendorStats(data), [data]);

  return (
    <div className="vendor-management-page">
      <PageHeader title="Vendor Management" />
      
      <VendorStats stats={stats} />
      
      <VendorFilters 
        filters={filters}
        onFilterChange={setFilters}
        onCreateClick={() => setShowCreateModal(true)}
      />
      
      <VendorTable 
        vendors={data?.content || []}
        isLoading={isLoading}
        onViewDetails={(vendor) => {
          setSelectedVendor(vendor);
          setShowDetailModal(true);
        }}
        onEdit={(vendor) => {
          setSelectedVendor(vendor);
          setShowEditModal(true);
        }}
        onBan={(vendor) => {
          setSelectedVendor(vendor);
          setShowBanModal(true);
        }}
        onUnban={(vendor) => handleUnban(vendor.id)}
        onDelete={(vendor) => handleDelete(vendor.id)}
      />
      
      <Pagination 
        currentPage={filters.page}
        totalPages={data?.totalPages || 1}
        onPageChange={(page) => setFilters({...filters, page})}
      />

      {/* Modals */}
      {showCreateModal && (
        <CreateVendorModal 
          onClose={() => setShowCreateModal(false)}
          onSuccess={() => {
            setShowCreateModal(false);
            refetch();
          }}
        />
      )}

      {showDetailModal && selectedVendor && (
        <VendorDetailModal 
          vendor={selectedVendor}
          onClose={() => setShowDetailModal(false)}
          onEdit={() => {
            setShowDetailModal(false);
            setShowEditModal(true);
          }}
          onViewTemplates={() => {
            setShowDetailModal(false);
            setShowTemplatesModal(true);
          }}
        />
      )}

      {/* Other modals... */}
    </div>
  );
};
```

---

## 🔌 API Service Layer

### adminVendorService.ts

```typescript
import axios from 'axios';
import type { ApiResponse, PageResponse, VendorProfileResponse, CreateVendorRequest, UpdateVendorRequest, BanVendorRequest, VendorFilterOptions } from '@/types/admin/vendor.types';

const BASE_URL = '/api/admin/vendors';

export const adminVendorService = {
  /**
   * Get all vendors with pagination
   */
  getAllVendors: async (params: {
    page: number;
    size: number;
    sortBy: string;
    sortDirection: string;
  }): Promise<ApiResponse<PageResponse<VendorProfileResponse>>> => {
    const response = await axios.get(BASE_URL, { params });
    return response.data;
  },

  /**
   * Get single vendor by ID
   */
  getVendorById: async (id: string): Promise<ApiResponse<VendorProfileResponse>> => {
    const response = await axios.get(`${BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * Create new vendor account
   */
  createVendor: async (data: CreateVendorRequest): Promise<ApiResponse<VendorProfileResponse>> => {
    const response = await axios.post(BASE_URL, data);
    return response.data;
  },

  /**
   * Update vendor profile
   */
  updateVendor: async (id: string, data: UpdateVendorRequest): Promise<ApiResponse<VendorProfileResponse>> => {
    const response = await axios.put(`${BASE_URL}/${id}`, data);
    return response.data;
  },

  /**
   * Ban vendor account
   */
  banVendor: async (id: string, request?: BanVendorRequest): Promise<ApiResponse<VendorProfileResponse>> => {
    const response = await axios.post(`${BASE_URL}/${id}/ban`, request || {});
    return response.data;
  },

  /**
   * Unban vendor account
   */
  unbanVendor: async (id: string): Promise<ApiResponse<VendorProfileResponse>> => {
    const response = await axios.post(`${BASE_URL}/${id}/unban`);
    return response.data;
  },

  /**
   * Delete vendor (soft delete)
   */
  deleteVendor: async (id: string): Promise<ApiResponse<void>> => {
    const response = await axios.delete(`${BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * Search vendors by keyword
   */
  searchVendors: async (params: {
    keyword: string;
    page: number;
    size: number;
    sortBy: string;
    sortDirection: string;
  }): Promise<ApiResponse<PageResponse<VendorProfileResponse>>> => {
    const response = await axios.get(`${BASE_URL}/search`, { params });
    return response.data;
  },

  /**
   * Advanced filter with multiple criteria
   */
  advancedFilter: async (filters: VendorFilterOptions): Promise<ApiResponse<PageResponse<VendorProfileResponse>>> => {
    const response = await axios.get(`${BASE_URL}/filter`, { 
      params: {
        keyword: filters.keyword,
        isVerified: filters.isVerified,
        isBanned: filters.isBanned,
        isActive: filters.isActive,
        page: filters.page,
        size: filters.size,
        sortBy: filters.sortBy,
        sortDirection: filters.sortDirection
      }
    });
    return response.data;
  },

  /**
   * Filter by verification status
   */
  filterByVerification: async (params: {
    isVerified: boolean;
    page: number;
    size: number;
    sortBy: string;
    sortDirection: string;
  }): Promise<ApiResponse<PageResponse<VendorProfileResponse>>> => {
    const response = await axios.get(`${BASE_URL}/filter/verified`, { params });
    return response.data;
  },

  /**
   * Filter by banned status
   */
  filterByBanned: async (params: {
    isBanned: boolean;
    page: number;
    size: number;
    sortBy: string;
    sortDirection: string;
  }): Promise<ApiResponse<PageResponse<VendorProfileResponse>>> => {
    const response = await axios.get(`${BASE_URL}/filter/banned`, { params });
    return response.data;
  },

  /**
   * Filter by active status
   */
  filterByActive: async (params: {
    isActive: boolean;
    page: number;
    size: number;
    sortBy: string;
    sortDirection: string;
  }): Promise<ApiResponse<PageResponse<VendorProfileResponse>>> => {
    const response = await axios.get(`${BASE_URL}/filter/active`, { params });
    return response.data;
  }
};
```

### adminWorkshopService.ts

```typescript
import axios from 'axios';
import type { ApiResponse, PageResponse, WorkshopTemplateResponse } from '@/types/workshop.types';

const BASE_URL = '/api/admin/vendors/workshop-templates';

export const adminWorkshopService = {
  /**
   * Get all workshop templates for a specific vendor
   */
  getVendorWorkshopTemplates: async (vendorId: string, params: {
    page: number;
    size: number;
    sortBy: string;
    sortDir: string;
  }): Promise<ApiResponse<PageResponse<WorkshopTemplateResponse>>> => {
    const response = await axios.get(`/api/admin/vendors/${vendorId}/workshop-templates`, { params });
    return response.data;
  },

  /**
   * Get all workshop templates (all vendors)
   */
  getAllWorkshopTemplates: async (params: {
    page: number;
    size: number;
    sortBy: string;
    sortDir: string;
  }): Promise<ApiResponse<PageResponse<WorkshopTemplateResponse>>> => {
    const response = await axios.get(BASE_URL, { params });
    return response.data;
  },

  /**
   * Approve workshop template
   */
  approveTemplate: async (id: string): Promise<ApiResponse<WorkshopTemplateResponse>> => {
    const response = await axios.post(`${BASE_URL}/${id}/approve`);
    return response.data;
  },

  /**
   * Reject workshop template
   */
  rejectTemplate: async (id: string, rejectReason: string): Promise<ApiResponse<WorkshopTemplateResponse>> => {
    const response = await axios.post(`${BASE_URL}/${id}/reject`, { rejectReason });
    return response.data;
  }
};
```

---

## 🎯 State Management

### useVendorList Hook

```typescript
import { useQuery } from '@tanstack/react-query';
import { adminVendorService } from '@/services/api/adminVendorService';
import type { VendorFilterOptions } from '@/types/admin/vendor.types';

export const useVendorList = (filters: VendorFilterOptions) => {
  return useQuery({
    queryKey: ['vendors', filters],
    queryFn: () => {
      // Use advanced filter if any filter is applied
      if (filters.keyword || filters.isVerified !== undefined || 
          filters.isBanned !== undefined || filters.isActive !== undefined) {
        return adminVendorService.advancedFilter(filters);
      }
      
      // Otherwise use simple get all
      return adminVendorService.getAllVendors({
        page: filters.page || 1,
        size: filters.size || 10,
        sortBy: filters.sortBy || 'createdAt',
        sortDirection: filters.sortDirection || 'DESC'
      });
    },
    keepPreviousData: true, // Keep showing old data while fetching new
    staleTime: 30000 // 30 seconds
  });
};
```

### useVendorDetail Hook

```typescript
import { useQuery } from '@tanstack/react-query';
import { adminVendorService } from '@/services/api/adminVendorService';

export const useVendorDetail = (vendorId: string | null) => {
  return useQuery({
    queryKey: ['vendor', vendorId],
    queryFn: () => adminVendorService.getVendorById(vendorId!),
    enabled: !!vendorId, // Only run if vendorId exists
    staleTime: 60000 // 1 minute
  });
};
```

### useVendorActions Hook

```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminVendorService } from '@/services/api/adminVendorService';
import { toast } from 'react-toastify';

export const useVendorActions = () => {
  const queryClient = useQueryClient();

  const createVendor = useMutation({
    mutationFn: adminVendorService.createVendor,
    onSuccess: () => {
      queryClient.invalidateQueries(['vendors']);
      toast.success('Vendor created successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create vendor');
    }
  });

  const updateVendor = useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) => 
      adminVendorService.updateVendor(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['vendors']);
      queryClient.invalidateQueries(['vendor']);
      toast.success('Vendor updated successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update vendor');
    }
  });

  const banVendor = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) => 
      adminVendorService.banVendor(id, reason ? { reason } : undefined),
    onSuccess: () => {
      queryClient.invalidateQueries(['vendors']);
      queryClient.invalidateQueries(['vendor']);
      toast.success('Vendor banned successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to ban vendor');
    }
  });

  const unbanVendor = useMutation({
    mutationFn: (id: string) => adminVendorService.unbanVendor(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['vendors']);
      queryClient.invalidateQueries(['vendor']);
      toast.success('Vendor unbanned successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to unban vendor');
    }
  });

  const deleteVendor = useMutation({
    mutationFn: (id: string) => adminVendorService.deleteVendor(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['vendors']);
      toast.success('Vendor deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to delete vendor');
    }
  });

  return {
    createVendor,
    updateVendor,
    banVendor,
    unbanVendor,
    deleteVendor
  };
};
```

---

## 🔄 User Flows

### Flow 1: View Vendor List (Default View)
```
1. Admin navigates to /admin/vendors
2. System loads vendor list (page 1, 10 items, sorted by createdAt DESC)
3. System displays VendorStats at top
4. System displays VendorTable with data
5. Admin can:
   - Scroll through table
   - Click column headers to sort
   - Click pagination to navigate pages
   - Use search/filters to refine list
```

### Flow 2: Create New Vendor
```
1. Admin clicks "Create Vendor" button
2. System opens CreateVendorModal
3. Admin fills in required fields:
   - Email (unique)
   - Password (min 8 chars, letter + number)
   - Full Name
   - Business Name
4. Admin optionally fills additional fields
5. Admin clicks "Create Vendor"
6. System validates input:
   ✓ Email format valid
   ✓ Password meets requirements
   ✓ Required fields filled
7. System sends POST /api/admin/vendors
8. On Success:
   - Modal closes
   - Success toast shown
   - Table refreshes with new vendor
9. On Error:
   - Error message shown
   - Modal stays open for correction
```

### Flow 3: View Vendor Details
```
1. Admin clicks "View Details" on a vendor row (or clicks row)
2. System opens VendorDetailModal
3. System displays:
   - All vendor information
   - Status badges
   - Statistics (template counts)
4. Admin can:
   - View all information
   - Click "Edit Profile" → Opens EditVendorModal
   - Click "View All Templates" → Opens VendorTemplatesList
   - Click "Ban Vendor" → Opens BanVendorModal
   - Click "Unban Vendor" → Unbans immediately with confirmation
```

### Flow 4: Ban Vendor
```
1. Admin clicks "Ban" action on a vendor
2. System opens BanVendorModal
3. Modal shows warning about consequences
4. Admin optionally enters ban reason
5. Admin clicks "Ban Vendor"
6. System shows confirmation dialog
7. Admin confirms
8. System sends POST /api/admin/vendors/{id}/ban
9. On Success:
   - Modal closes
   - Table refreshes
   - Vendor row shows red background
   - Status badge changes to "Banned"
   - Success toast shown
```

### Flow 5: Search and Filter
```
1. Admin enters search keyword in search box
2. System debounces input (500ms delay)
3. System sends GET /api/admin/vendors/search?keyword=...
4. Table updates with search results
5. Admin applies additional filters:
   - Verification: Verified only
   - Status: Active only
6. System combines filters in single request
7. System sends GET /api/admin/vendors/filter?keyword=...&isVerified=true&isActive=true
8. Table updates with filtered results
9. Admin clicks "Clear Filters"
10. System resets to default view (all vendors)
```

### Flow 6: View Vendor's Workshop Templates
```
1. From VendorDetailModal, Admin clicks "View All Templates"
2. System opens VendorTemplatesModal
3. System loads templates for that vendor
4. System displays template cards with status badges
5. For PENDING templates, Admin can:
   - Click "Approve" → Immediately approves
   - Click "Reject" → Opens textarea for reject reason
6. For ACTIVE templates, Admin can:
   - Click "View Details" → Opens template detail view
   - Click "Deactivate" → Changes status with confirmation
7. Modal shows pagination if vendor has many templates
```

### Flow 7: Approve/Reject Templates from Vendor View
```
APPROVE:
1. Admin clicks "Approve" on PENDING template
2. System shows confirmation: "Approve this template?"
3. Admin confirms
4. System sends POST /api/admin/vendors/workshop-templates/{id}/approve
5. Template status changes to ACTIVE
6. Success toast shown
7. Template card updates in modal

REJECT:
1. Admin clicks "Reject" on PENDING template
2. System shows reject modal with textarea
3. Admin enters rejection reason (required)
4. Admin clicks "Reject Template"
5. System sends POST /api/admin/vendors/workshop-templates/{id}/reject
6. Template status changes to REJECTED
7. Rejection reason stored
8. Success toast shown
9. Template card updates in modal
```

---

## 📌 Important Business Rules

### Vendor Status Rules
1. **Active Status:**
   - `isActive = true`: Vendor can log in and operate normally
   - `isActive = false`: Vendor cannot log in (soft deleted)

2. **Banned Status:**
   - `isBanned = true`: Vendor is banned (cannot log in)
   - `isBanned = false`: Vendor is not banned
   - Banned vendors automatically become inactive
   - Ban reason is optional but recommended

3. **Verification Status:**
   - `isVerifiedVendor = true`: Vendor is verified by admin
   - `isVerifiedVendor = false`: Vendor is not verified
   - Only verified vendors can submit templates for approval
   - Verification is granted by admin only

### Password Requirements
When creating vendors, password must:
- Be at least 8 characters long
- Contain at least 1 letter (a-zA-Z)
- Contain at least 1 number (0-9)

```typescript
const passwordChecks = {
  length: password.length >= 8,
  hasNumber: /\d/.test(password),
  hasLetter: /[a-zA-Z]/.test(password),
};

const isPasswordValid = Object.values(passwordChecks).every(check => check);
```

### Email Uniqueness
- Email must be unique across all users in the system
- Backend will return error if email already exists
- Show clear error message to admin

### Deletion Rules
- Delete is **soft delete** (sets `isActive = false`)
- Vendor data is not physically removed
- Deleted vendors cannot log in
- Templates remain in database but hidden

### Ban/Unban Rules
- Only active vendors can be banned
- Banned vendors can be unbanned at any time
- Unbanning restores previous active status
- Ban reason is visible to admin only (not to vendor)

### Workshop Template Approval
- Only verified vendors can submit templates
- Only PENDING templates can be approved/rejected
- Approved templates become ACTIVE
- Rejected templates can be edited and resubmitted
- Rejection reason must be provided

### Sorting Options
Available sort fields:
- `createdAt`: Registration date (default)
- `updatedAt`: Last modification
- `fullname`: Vendor owner name
- `businessName`: Business name
- `email`: Email address

Sort directions:
- `ASC`: Ascending (A-Z, 0-9, oldest first)
- `DESC`: Descending (Z-A, 9-0, newest first)

### Pagination
- Default page size: 10 items
- Page numbers are 1-based (page 1 = first page)
- Backend uses 0-based indexing internally
- Maximum page size: 100 items (recommended limit)

---

## 🎨 UI/UX Best Practices

### Status Colors
```typescript
const statusColors = {
  active: 'green',    // Green: #10B981
  inactive: 'gray',   // Gray: #6B7280
  banned: 'red',      // Red: #EF4444
  verified: 'blue',   // Blue: #3B82F6
  pending: 'yellow',  // Yellow: #F59E0B
};
```

### Icons to Use
- 👁️ View Details
- ✏️ Edit
- 🚫 Ban
- ✅ Unban / Approve
- ❌ Reject
- 🗑️ Delete
- ✓ Verified
- 🔍 Search
- 📊 Templates
- 👤 Vendor

### Loading States
- Show skeleton loaders for table rows while fetching
- Show spinner in modal buttons during API calls
- Disable action buttons during operations
- Show "Loading..." text in modals

### Error Handling
- Display error toasts for failed operations
- Show validation errors under form fields
- Retry button for network failures
- Clear error messages (not technical jargon)

### Confirmation Dialogs
Always confirm destructive actions:
- Ban vendor
- Delete vendor
- Approve/Reject template

Example confirmation:
```typescript
const handleBan = async (vendor: VendorProfileResponse) => {
  const confirmed = await confirm({
    title: 'Ban Vendor',
    message: `Are you sure you want to ban ${vendor.businessName}?`,
    confirmText: 'Ban',
    cancelText: 'Cancel',
    confirmColor: 'red'
  });
  
  if (confirmed) {
    // Proceed with ban
  }
};
```

### Responsive Design
- Table should be scrollable horizontally on mobile
- Modals should be full-screen on mobile
- Filter dropdowns should stack vertically on mobile
- Statistics cards should be 2x2 grid on mobile, 1x4 on desktop

### Accessibility
- Use semantic HTML (table, button, input)
- Proper ARIA labels for icons
- Keyboard navigation support
- Focus management in modals
- Screen reader friendly

---

## 🚀 Implementation Checklist

### Phase 1: Basic Setup
- [ ] Create folder structure
- [ ] Define TypeScript interfaces
- [ ] Set up API service layer
- [ ] Configure React Query
- [ ] Set up routing

### Phase 2: Core Components
- [ ] VendorManagementPage (main container)
- [ ] VendorStats component
- [ ] VendorFilters component
- [ ] VendorTable component
- [ ] Pagination component

### Phase 3: Modals
- [ ] VendorDetailModal
- [ ] CreateVendorModal
- [ ] EditVendorModal
- [ ] BanVendorModal
- [ ] VendorTemplatesModal

### Phase 4: Functionality
- [ ] Implement search functionality
- [ ] Implement filter functionality
- [ ] Implement sorting
- [ ] Implement pagination
- [ ] Implement CRUD operations

### Phase 5: Workshop Template Integration
- [ ] Display vendor's templates
- [ ] Implement approve/reject actions
- [ ] Link to template detail pages

### Phase 6: Polish
- [ ] Add loading states
- [ ] Add error handling
- [ ] Add success/error toasts
- [ ] Add confirmation dialogs
- [ ] Responsive design
- [ ] Accessibility improvements

### Phase 7: Testing
- [ ] Test all CRUD operations
- [ ] Test search and filters
- [ ] Test pagination
- [ ] Test ban/unban flow
- [ ] Test template approval flow
- [ ] Test error scenarios
- [ ] Mobile responsive testing

---

## 📝 Example API Requests

### Get All Vendors (Default)
```http
GET /api/admin/vendors?page=1&size=10&sortBy=createdAt&sortDirection=DESC
Authorization: Bearer {admin_jwt_token}
```

### Search Vendors
```http
GET /api/admin/vendors/search?keyword=workshop&page=1&size=10&sortBy=createdAt&sortDirection=DESC
Authorization: Bearer {admin_jwt_token}
```

### Advanced Filter
```http
GET /api/admin/vendors/filter?keyword=abc&isVerified=true&isBanned=false&isActive=true&page=1&size=10&sortBy=businessName&sortDirection=ASC
Authorization: Bearer {admin_jwt_token}
```

### Create Vendor
```http
POST /api/admin/vendors
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json

{
  "email": "vendor@example.com",
  "password": "SecurePass123",
  "fullname": "John Doe",
  "phoneNumber": "+84 123 456 789",
  "businessName": "ABC Workshop Co.",
  "description": "Professional workshop provider",
  "address": "123 Main St, District 1, HCMC",
  "taxCode": "1234567890",
  "bankName": "Vietcombank",
  "bankAccountNumber": "0123456789",
  "bankAccountName": "ABC Workshop Co., Ltd"
}
```

### Update Vendor
```http
PUT /api/admin/vendors/{vendorId}
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json

{
  "fullname": "John Doe Jr.",
  "businessName": "ABC Workshop Co. Ltd.",
  "description": "Updated description",
  "isVerifiedVendor": true,
  "isActive": true
}
```

### Ban Vendor
```http
POST /api/admin/vendors/{vendorId}/ban
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json

{
  "reason": "Violated terms of service"
}
```

### Unban Vendor
```http
POST /api/admin/vendors/{vendorId}/unban
Authorization: Bearer {admin_jwt_token}
```

### Get Vendor's Workshop Templates (Specific Vendor)
```http
GET /api/admin/vendors/{vendorId}/workshop-templates?page=1&size=10&sortBy=createdAt&sortDir=DESC
Authorization: Bearer {admin_jwt_token}
```

### Get All Workshop Templates (All Vendors)
```http
GET /api/admin/vendors/workshop-templates?page=1&size=10&sortBy=createdAt&sortDir=DESC
Authorization: Bearer {admin_jwt_token}
```

### Approve Template
```http
POST /api/admin/vendors/workshop-templates/{templateId}/approve
Authorization: Bearer {admin_jwt_token}
```

### Reject Template
```http
POST /api/admin/vendors/workshop-templates/{templateId}/reject
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json

{
  "rejectReason": "Incomplete information. Please add more details about the workshop content."
}
```

---

## 🎯 Summary

This guide provides a complete blueprint for building the Admin Vendor Management system. The key features include:

1. **Vendor List Table:** Paginated, sortable, filterable table displaying all vendors
2. **Search & Filter:** Advanced filtering by verification, ban, and active status
3. **CRUD Operations:** Create, view, update, delete vendor accounts
4. **Ban Management:** Ban/unban vendors with optional reasons
5. **Workshop Template Management:** View and manage vendor's templates
6. **Template Approval:** Approve or reject pending workshop templates
7. **Responsive Design:** Works on desktop, tablet, and mobile
8. **Real-time Updates:** Uses React Query for efficient data management

The UI prioritizes clarity, efficiency, and ease of use for administrators managing a large vendor base.

---

## 📚 Additional Resources

- [React Query Documentation](https://tanstack.com/query/latest)
- [Axios Documentation](https://axios-http.com/docs/intro)
- [TailwindCSS Documentation](https://tailwindcss.com/docs) (if using)
- [React Hook Form](https://react-hook-form.com/) (for form management)
- [React Toastify](https://fkhadra.github.io/react-toastify/) (for notifications)

---

**Last Updated:** February 16, 2026
**Version:** 1.0
