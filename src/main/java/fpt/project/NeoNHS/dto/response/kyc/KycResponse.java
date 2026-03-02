package fpt.project.NeoNHS.dto.response.kyc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponse {

    private boolean success;
    private String message;

    /**
     * Họ tên đầy đủ (đã bỏ dấu) từ OCR CCCD
     */
    private String fullName;

    /**
     * Số CCCD/CMT
     */
    private String idNumber;

    /**
     * Ngày sinh
     */
    private String dateOfBirth;

    /**
     * Địa chỉ
     */
    private String address;

    /**
     * Điểm so khớp khuôn mặt (0-100%)
     */
    private Double faceMatchScore;

    /**
     * Có phải giấy tờ giả hay không
     */
    private Boolean isFake;
}
