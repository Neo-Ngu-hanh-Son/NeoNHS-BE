package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.payout.CreatePayoutRequest;
import fpt.project.NeoNHS.dto.response.payout.PayoutResponse;

public interface PayoutService {
    PayoutResponse createPayout(CreatePayoutRequest request);
    PayoutResponse getPayoutById(String payoutId);
}
