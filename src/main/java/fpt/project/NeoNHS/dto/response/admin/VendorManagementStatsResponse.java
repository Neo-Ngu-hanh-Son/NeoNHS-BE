package fpt.project.NeoNHS.dto.response.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VendorManagementStatsResponse {
    private Long total;
    private Long active;
    private Long verified;
    private Long banned;
    private Long pendingVerification;
}

