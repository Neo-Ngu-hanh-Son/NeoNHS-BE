package fpt.project.NeoNHS.dto.response.admin;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorActivityResponse {

    private UUID vendorId;
    private String vendorName;

    /**
     * CREATE_WORKSHOP | APPROVE_WORKSHOP | CREATE_EVENT | SELL_TICKET
     */
    private String action;
    private String targetName; // workshop / event name
    private LocalDateTime time;
}
