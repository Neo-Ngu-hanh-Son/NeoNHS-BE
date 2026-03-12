package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.admin.RevenueReportRequest;
import fpt.project.NeoNHS.dto.response.admin.*;
import fpt.project.NeoNHS.entity.OrderDetail;
import fpt.project.NeoNHS.repository.OrderDetailRepository;
import fpt.project.NeoNHS.service.RevenueAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueAnalyticsServiceImpl implements RevenueAnalyticsService {

    private final OrderDetailRepository orderDetailRepository;

    @Override
    public RevenueReportResponse getFullReport(RevenueReportRequest request) {
        LocalDateTime start;
        LocalDateTime end;

        // --- 1. XỬ LÝ THỜI GIAN ---
        if (request.getPeriod() != null && !request.getPeriod().isEmpty()) {
            end = LocalDateTime.now();
            switch (request.getPeriod()) {
                case "3_MONTHS": start = end.minusMonths(3).with(LocalTime.MIN); break;
                case "6_MONTHS": start = end.minusMonths(6).with(LocalTime.MIN); break;
                case "LAST_MONTH": start = end.minusMonths(1).with(LocalTime.MIN); break;
                default: start = end.minusDays(30).with(LocalTime.MIN); break;
            }
        } else {
            // Chuyển LocalDate sang LocalDateTime (00:00:00 và 23:59:59)
            start = (request.getStartDate() != null)
                    ? request.getStartDate().atStartOfDay()
                    : LocalDate.now().minusDays(30).atStartOfDay();

            end = (request.getEndDate() != null)
                    ? request.getEndDate().atTime(23, 59, 59)
                    : LocalDateTime.now();
        }

        // --- 2. TRUY VẤN DỮ LIỆU ---
        List<OrderDetail> details = orderDetailRepository.findRevenueDetails(start, end);

        // --- 3. TÍNH TOÁN CHỈ SỐ (KPI) ---
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal adminEarnings = BigDecimal.ZERO;
        BigDecimal vendorPayouts = BigDecimal.ZERO;

        List<TransactionDetailResponse> txDtos = new ArrayList<>();

        for (OrderDetail od : details) {
            // Tính doanh thu dòng (UnitPrice * Quantity)
            BigDecimal lineGross = od.getUnitPrice().multiply(BigDecimal.valueOf(od.getQuantity()));
            totalGross = totalGross.add(lineGross);

            // Kiểm tra xem đơn hàng thuộc Vendor hay Admin tự tổ chức
            boolean isVendorProduct = (od.getWorkshopSession() != null);

            BigDecimal lineFeeDisplay;
            BigDecimal lineNetDisplay;

            if (isVendorProduct) {
                // TRƯỜNG HỢP VENDOR: Admin ăn phí (Fee), Vendor nhận Net
                BigDecimal fee = od.getCommissionAmount() != null ? od.getCommissionAmount() : BigDecimal.ZERO;
                BigDecimal net = od.getNetAmount() != null ? od.getNetAmount() : lineGross;

                adminEarnings = adminEarnings.add(fee);
                vendorPayouts = vendorPayouts.add(net);

                lineFeeDisplay = fee;
                lineNetDisplay = net;
            } else {
                // TRƯỜNG HỢP ADMIN EVENT: Admin ăn trọn 100%, không trả cho Vendor nào
                adminEarnings = adminEarnings.add(lineGross);

                lineFeeDisplay = BigDecimal.ZERO; // Để 0 trên UI vì không phải "phí thu từ người khác"
                lineNetDisplay = lineGross;      // Hiện tổng tiền ở cột Net trên UI
            }

            // Mapping dữ liệu cho bảng Table
            txDtos.add(TransactionDetailResponse.builder()
                    .date(od.getCreatedAt())
                    .id("#TRX-" + od.getId().toString().substring(0, 8).toUpperCase())
                    .vendor(isVendorProduct ? od.getWorkshopSession().getWorkshopTemplate().getVendor().getBusinessName() : "Admin")
                    .item(isVendorProduct ? od.getWorkshopSession().getWorkshopTemplate().getName() :
                            (od.getTicketCatalog() != null ? od.getTicketCatalog().getEvent().getName() : "Event Ticket"))
                    .gross(lineGross)
                    .fee(lineFeeDisplay)
                    .net(lineNetDisplay)
                    .status("SUCCESS")
                    .build());
        }

        // --- 4. BREAKDOWN CHO BIỂU ĐỒ (VENDORS) ---
        List<VendorRevenueResponse> vendorList = orderDetailRepository.getRevenueByVendor(start, end)
                .stream()
                .map(obj -> new VendorRevenueResponse(
                        obj[0] != null ? (String) obj[0] : "Admin",
                        (BigDecimal) obj[1]))
                .collect(Collectors.toList());

        // --- 5. ĐÓNG GÓI RESPONSE ---
        return RevenueReportResponse.builder()
                .summary(RevenueSummaryResponse.builder()
                        .totalGross(totalGross)
                        .adminEarnings(adminEarnings)
                        .vendorPayouts(vendorPayouts)
                        .totalTransactions((long) details.size())
                        .build())
                .vendorBreakdown(vendorList)
                .transactions(txDtos)
                .build();
    }
}