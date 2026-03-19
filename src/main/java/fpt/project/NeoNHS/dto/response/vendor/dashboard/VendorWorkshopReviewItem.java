package fpt.project.NeoNHS.dto.response.vendor.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorWorkshopReviewItem {
    private UUID workshopId;
    private String workshopName;
    private long totalReviews;
    private BigDecimal averageRating;
    private long newReviewsInWindow;
}
