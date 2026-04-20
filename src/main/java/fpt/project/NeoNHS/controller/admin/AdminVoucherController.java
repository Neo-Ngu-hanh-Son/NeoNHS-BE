package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.voucher.CreateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.UpdateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.VoucherFilterRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.voucher.VoucherResponse;
import fpt.project.NeoNHS.enums.*;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Admin controller for Voucher management.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Vouchers", description = "Admin APIs for managing vouchers (requires ADMIN role)")
public class AdminVoucherController {

    private final VoucherService voucherService;

    @Operation(
            summary = "Create voucher (Admin)",
            description = "Create a new platform-wide voucher with the provided details"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Valid @RequestBody CreateVoucherRequest request) {
        VoucherResponse response = voucherService.createAdminVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Voucher created successfully", response));
    }

    @Operation(
            summary = "Get all vouchers (Admin)",
            description = "Retrieve a paginated list of vouchers with optional filters. Admin can filter by scope, type, status, code, date range, and deleted status."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getAllVouchers(
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(required = false) VoucherScope scope,
            @RequestParam(required = false) VoucherType voucherType,
            @RequestParam(required = false) VoucherStatus status,
            @RequestParam(required = false) ApplicableProduct applicableProduct,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(defaultValue = "false") Boolean includeDeleted,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        VoucherFilterRequest filter = VoucherFilterRequest.builder()
                .scope(scope)
                .voucherType(voucherType)
                .status(status)
                .applicableProduct(applicableProduct)
                .code(code)
                .startDate(startDate)
                .endDate(endDate)
                .deleted(deleted)
                .includeDeleted(includeDeleted)
                .build();

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VoucherResponse> vouchers = voucherService.getAllVouchers(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Vouchers retrieved successfully", vouchers));
    }

    @Operation(
            summary = "Get voucher by ID (Admin)",
            description = "Retrieve detailed information of a specific voucher"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(
            @Parameter(description = "Voucher ID") @PathVariable UUID id) {
        VoucherResponse response = voucherService.getVoucherById(id);
        return ResponseEntity.ok(ApiResponse.success("Voucher retrieved successfully", response));
    }

    @Operation(
            summary = "Update voucher (Admin)",
            description = "Update an existing voucher by ID"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @Parameter(description = "Voucher ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateVoucherRequest request) {
        VoucherResponse response = voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Voucher updated successfully", response));
    }

    @Operation(
            summary = "Soft delete voucher (Admin)",
            description = "Soft delete a voucher by setting deletedAt and status to INACTIVE"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(
            @Parameter(description = "Voucher ID") @PathVariable UUID id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(ApiResponse.success("Voucher deleted successfully", null));
    }

    @Operation(
            summary = "Restore voucher (Admin)",
            description = "Restore a soft-deleted voucher by clearing deletedAt/deletedBy and setting status back to ACTIVE"
    )
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<VoucherResponse>> restoreVoucher(
            @Parameter(description = "Voucher ID") @PathVariable UUID id) {
        VoucherResponse response = voucherService.restoreVoucher(id);
        return ResponseEntity.ok(ApiResponse.success("Voucher restored successfully", response));
    }

    @Operation(
            summary = "Permanently delete voucher (Admin)",
            description = "Permanently delete a voucher from the database. " +
                    "Only allowed if no user has used this voucher in an order. " +
                    "Unused user-voucher records will be removed. This action cannot be undone."
    )
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> hardDeleteVoucher(
            @Parameter(description = "Voucher ID") @PathVariable UUID id) {
        voucherService.hardDeleteVoucher(id);
        return ResponseEntity.ok(ApiResponse.success("Voucher permanently deleted successfully", null));
    }
}
