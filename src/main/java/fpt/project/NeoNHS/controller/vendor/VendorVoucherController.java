package fpt.project.NeoNHS.controller.vendor;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.voucher.CreateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.UpdateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.VoucherFilterRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.voucher.VoucherResponse;
import fpt.project.NeoNHS.enums.*;
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
 * Vendor controller for Voucher management.
 * All endpoints require VENDOR role.
 */
@RestController
@RequestMapping("/api/vendor/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
@Tag(name = "Vendor - Vouchers", description = "Vendor APIs for managing vouchers (requires VENDOR role)")
public class VendorVoucherController {

    private final VoucherService voucherService;

    @Operation(
            summary = "Create voucher (Vendor)",
            description = "Create a new vendor-scoped voucher. Only DISCOUNT and GIFT_PRODUCT types are allowed."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Valid @RequestBody CreateVoucherRequest request) {
        VoucherResponse response = voucherService.createVendorVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Voucher created successfully", response));
    }

    @Operation(
            summary = "Get my vouchers (Vendor)",
            description = "Retrieve a paginated list of current vendor's vouchers with optional filters"
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getMyVouchers(
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(required = false) VoucherType voucherType,
            @RequestParam(required = false) VoucherStatus status,
            @RequestParam(required = false) ApplicableProduct applicableProduct,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        VoucherFilterRequest filter = VoucherFilterRequest.builder()
                .voucherType(voucherType)
                .status(status)
                .applicableProduct(applicableProduct)
                .code(code)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VoucherResponse> vouchers = voucherService.getMyVendorVouchers(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Vouchers retrieved successfully", vouchers));
    }

    @Operation(
            summary = "Update voucher (Vendor)",
            description = "Update an existing voucher owned by current vendor"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @Parameter(description = "Voucher ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateVoucherRequest request) {
        VoucherResponse response = voucherService.updateVendorVoucher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Voucher updated successfully", response));
    }

    @Operation(
            summary = "Soft delete voucher (Vendor)",
            description = "Soft delete a voucher owned by current vendor"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(
            @Parameter(description = "Voucher ID") @PathVariable UUID id) {
        voucherService.deleteVendorVoucher(id);
        return ResponseEntity.ok(ApiResponse.success("Voucher deleted successfully", null));
    }
}
