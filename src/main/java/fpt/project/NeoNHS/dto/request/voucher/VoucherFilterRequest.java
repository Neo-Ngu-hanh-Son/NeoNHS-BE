package fpt.project.NeoNHS.dto.request.voucher;

import fpt.project.NeoNHS.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherFilterRequest {

    private VoucherScope scope;

    private VoucherType voucherType;

    private VoucherStatus status;

    private ApplicableProduct applicableProduct;

    private String code;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * Filter by soft delete status (Admin only):
     * - null or false: show only active (non-deleted) vouchers
     * - true: show only deleted vouchers
     */
    private Boolean deleted;

    /**
     * Include all vouchers regardless of deleted status (Admin only).
     * If true, ignores the 'deleted' field and shows all vouchers.
     */
    @Builder.Default
    private Boolean includeDeleted = false;
}
