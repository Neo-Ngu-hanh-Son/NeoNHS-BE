package fpt.project.NeoNHS.dto.response.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Holds the result of classifying a user's vouchers against the current cart.
 * Passed between VoucherService and CartService to avoid re-querying.
 */
@Getter
@Builder
@AllArgsConstructor
public class VoucherClassificationResult {

    private final List<UserVoucherRespone> validVouchers;
    private final List<UserVoucherRespone> invalidVouchers;
}
