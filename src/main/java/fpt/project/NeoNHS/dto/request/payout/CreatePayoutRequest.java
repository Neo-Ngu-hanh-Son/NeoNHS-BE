package fpt.project.NeoNHS.dto.request.payout;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO cho việc tạo lệnh chi đơn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayoutRequest {

    @jakarta.validation.constraints.NotBlank(message = "Reference ID is required")
    @JsonProperty("referenceId")
    private String referenceId;

    /**
     * Số tiền thanh toán (VND)
     */
    @NotNull(message = "Amount is required")
    @Min(value = 10000, message = "Amount must be at least 10,000 VND")
    @JsonProperty("amount")
    private Integer amount;

    /**
     * Mô tả thanh toán
     */
    @NotBlank(message = "Description is required")
    @JsonProperty("description")
    private String description;

    /**
     * Mã ngân hàng đích (BIN code)
     * VD: 970422 = MB Bank, 970415 = Vietinbank
     */
    @NotBlank(message = "Bank BIN is required")
    @JsonProperty("toBin")
    private String toBin;

    /**
     * Số tài khoản đích
     */
    @NotBlank(message = "Account number is required")
    @JsonProperty("toAccountNumber")
    private String toAccountNumber;

    /**
     * Danh mục thanh toán (tùy chọn)
     * VD: ["salary", "bonus"]
     */
    @JsonProperty("category")
    private List<String> category;
}
