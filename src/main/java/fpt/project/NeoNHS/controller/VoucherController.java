package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.voucher.UserVoucherRespone;
import fpt.project.NeoNHS.dto.response.voucher.VoucherResponse;
import fpt.project.NeoNHS.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public / Tourist controller for Voucher browsing and collection.
 */
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Vouchers", description = "APIs for browsing and collecting vouchers (requires authentication)")
public class VoucherController {

    private final VoucherService voucherService;

    @Operation(
            summary = "Get available platform vouchers",
            description = "Retrieve a paginated list of currently available platform-wide vouchers"
    )
    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getAvailablePlatformVouchers(
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VoucherResponse> response = voucherService.getAvailablePlatformVouchers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Available vouchers retrieved successfully", response));
    }

    @Operation(
            summary = "Get available vendor vouchers",
            description = "Retrieve a paginated list of currently available vouchers for a specific vendor"
    )
    @GetMapping("/available/vendor/{vendorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getAvailableVendorVouchers(
            @Parameter(description = "Vendor Profile ID") @PathVariable UUID vendorId,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VoucherResponse> response = voucherService.getAvailableVendorVouchers(vendorId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Vendor vouchers retrieved successfully", response));
    }

    @Operation(
            summary = "Collect a voucher",
            description = "Add a voucher to the current user's voucher wallet"
    )
    @PostMapping("/collect/{voucherId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserVoucherRespone>> collectVoucher(
            @Parameter(description = "Voucher ID") @PathVariable UUID voucherId) {
        UserVoucherRespone response = voucherService.collectVoucher(voucherId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Voucher collected successfully", response));
    }

    @Operation(
            summary = "Get my collected vouchers",
            description = "Retrieve a paginated list of vouchers collected by the current user, optionally filtered by used status"
    )
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<UserVoucherRespone>>> getMyVouchers(
            @RequestParam(required = false) Boolean isUsed,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = "obtainedDate") String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserVoucherRespone> response = voucherService.getMyVouchers(isUsed, pageable);
        return ResponseEntity.ok(ApiResponse.success("My vouchers retrieved successfully", response));
    }
}
