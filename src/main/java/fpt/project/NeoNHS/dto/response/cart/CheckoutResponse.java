package fpt.project.NeoNHS.dto.response.cart;

import fpt.project.NeoNHS.dto.response.voucher.UserVoucherRespone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private List<CartItemResponse> cartItems;
    private BigDecimal totalPrice;
    private List<UserVoucherRespone> validVouchers;
    private List<UserVoucherRespone> invalidVouchers;
    private BigDecimal discountValue;
    private BigDecimal finalTotalPrice;
    private UserVoucherRespone appliedVoucher;
    private LocalDateTime transactionDate;
}
