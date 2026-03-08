package fpt.project.NeoNHS.dto.request.payout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {

    /**
     * Số tiền muốn rút (VND)
     */
    private int amount;

    /**
     * Ảnh chụp live (Base64 encoded) — dùng để face verification trước khi rút
     * tiền.
     */
    private String livePhotoBase64;
}
