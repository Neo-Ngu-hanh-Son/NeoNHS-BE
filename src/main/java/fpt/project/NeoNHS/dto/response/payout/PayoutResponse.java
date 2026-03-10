package fpt.project.NeoNHS.dto.response.payout;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho lệnh chi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutResponse {

    /**
     * ID lệnh chi do hệ thống sinh ra
     */
    @JsonProperty("id")
    private String id;

    /**
     * Mã tham chiếu do client cung cấp
     */
    @JsonProperty("referenceId")
    private String referenceId;

    /**
     * Danh sách giao dịch con
     */
    @JsonProperty("transactions")
    private List<PayoutTransaction> transactions;

    /**
     * Danh mục thanh toán
     */
    @JsonProperty("category")
    private List<String> category;

    /**
     * Trạng thái phê duyệt
     * PROCESSING, SUCCEEDED, FAILED
     */
    @JsonProperty("approvalState")
    private String approvalState;

    /**
     * Thời gian tạo
     */
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    /**
     * DTO cho giao dịch con trong lệnh chi
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayoutTransaction {

        @JsonProperty("id")
        private String id;

        @JsonProperty("referenceId")
        private String referenceId;

        @JsonProperty("amount")
        private Integer amount;

        @JsonProperty("description")
        private String description;

        @JsonProperty("toBin")
        private String toBin;

        @JsonProperty("toAccountNumber")
        private String toAccountNumber;

        @JsonProperty("toAccountName")
        private String toAccountName;

        /**
         * Trạng thái giao dịch
         * PROCESSING, SUCCEEDED, FAILED
         */
        @JsonProperty("state")
        private String state;
    }
}
