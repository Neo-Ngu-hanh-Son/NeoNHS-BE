package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.kyc.KycRequest;
import fpt.project.NeoNHS.dto.response.kyc.KycResponse;

public interface VnptEkycService {

    /**
     * Lấy access token VNPT eKYC
     */
    String getAccessToken();

    /**
     * Thực hiện toàn bộ flow KYC:
     * 1. Upload ảnh → nhận hash
     * 2. OCR CCCD mặt trước
     * 3. OCR CCCD mặt sau
     * 4. Face compare (CCCD vs Selfie)
     */
    KycResponse performKyc(KycRequest request);
}
