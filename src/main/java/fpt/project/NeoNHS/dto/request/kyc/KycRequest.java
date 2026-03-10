package fpt.project.NeoNHS.dto.request.kyc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycRequest {

    /**
     * Ảnh CCCD mặt trước (Base64 encoded)
     */
    private String frontImageBase64;

    /**
     * Ảnh CCCD mặt sau (Base64 encoded)
     */
    private String backImageBase64;

    /**
     * Ảnh selfie chân dung (Base64 encoded)
     */
    private String selfieImageBase64;
}
