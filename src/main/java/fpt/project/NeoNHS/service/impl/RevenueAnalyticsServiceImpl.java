package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.TimezoneConstants;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueAnalyticsServiceImpl implements RevenueAnalyticsService {

    private final OrderDetailRepository orderDetailRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public RevenueReportResponse getFullReport(RevenueReportRequest request) {
        LocalDateTime start;
        LocalDateTime end;

        // --- 1. XỬ LÝ THỜI GIAN ---
        if (request.getPeriod() != null && !request.getPeriod().isEmpty()) {
            end = LocalDateTime.now(ZoneId.of(TimezoneConstants.ASIA_HO_CHI_MINH));
            switch (request.getPeriod()) {
                case "3_MONTHS":
                    start = end.minusMonths(3).with(LocalTime.MIN);
                    break;
                case "6_MONTHS":
                    start = end.minusMonths(6).with(LocalTime.MIN);
                    break;
                case "LAST_MONTH":
                    start = end.minusMonths(1).with(LocalTime.MIN);
                    break;
                default:
                    start = end.minusDays(30).with(LocalTime.MIN);
                    break;
            }
        } else {
            start = (request.getStartDate() != null)
                    ? request.getStartDate().atStartOfDay()
                    : LocalDate.now().minusDays(30).atStartOfDay();

            end = (request.getEndDate() != null)
                    ? request.getEndDate().atTime(23, 59, 59)
                    : LocalDateTime.now(ZoneId.of(TimezoneConstants.ASIA_HO_CHI_MINH));
        }

        // --- 2. TRUY VẤN DỮ LIỆU ---
        List<OrderDetail> details = orderDetailRepository.findRevenueDetails(start, end);

        // --- 3. TÍNH TOÁN CHỈ SỐ (KPI) ---
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal adminEarnings = BigDecimal.ZERO;
        BigDecimal vendorPayouts = BigDecimal.ZERO;

        List<TransactionDetailResponse> txDtos = new ArrayList<>();

        for (OrderDetail od : details) {
            BigDecimal lineGross = od.getUnitPrice().multiply(BigDecimal.valueOf(od.getQuantity()));
            totalGross = totalGross.add(lineGross);

            boolean isVendorProduct = (od.getWorkshopSession() != null);
            BigDecimal lineFeeDisplay;
            BigDecimal lineNetDisplay;
            String vendorName = "Admin";
            String itemName = "Unknown Item";

            if (isVendorProduct) {
                BigDecimal fee = od.getCommissionAmount() != null ? od.getCommissionAmount()
                        : BigDecimal.ZERO;
                // Đảm bảo Net = Gross - Fee
                BigDecimal net = lineGross.subtract(fee);

                adminEarnings = adminEarnings.add(fee);
                vendorPayouts = vendorPayouts.add(net);
                lineFeeDisplay = fee;
                lineNetDisplay = net;

                if (od.getWorkshopSession().getWorkshopTemplate() != null) {
                    itemName = od.getWorkshopSession().getWorkshopTemplate().getName();
                    if (od.getWorkshopSession().getWorkshopTemplate().getVendor() != null) {
                        vendorName = od.getWorkshopSession().getWorkshopTemplate().getVendor()
                                .getBusinessName();
                    }
                }
            } else {
                // Nếu là Admin sản phẩm (Event), toàn bộ Gross là của Admin
                adminEarnings = adminEarnings.add(lineGross);
                lineFeeDisplay = lineGross;
                lineNetDisplay = BigDecimal.ZERO;

                if (od.getTicketCatalog() != null && od.getTicketCatalog().getEvent() != null) {
                    itemName = od.getTicketCatalog().getEvent().getName();
                } else {
                    itemName = "Event Ticket";
                }
            }

            txDtos.add(TransactionDetailResponse.builder()
                    .date(od.getCreatedAt())
                    .id("#TRX-" + od.getId().toString().substring(0, 8).toUpperCase())
                    .vendor(vendorName)
                    .item(itemName)
                    .gross(lineGross)
                    .fee(lineFeeDisplay)
                    .net(lineNetDisplay)
                    .status("SUCCESS")
                    .build());
        }

        // --- 4. BREAKDOWN CHO BIỂU ĐỒ (VENDORS) ---
        final BigDecimal finalTotalGross = totalGross.compareTo(BigDecimal.ZERO) > 0 ? totalGross
                : BigDecimal.ONE;
        List<VendorRevenueResponse> vendorList = orderDetailRepository.getRevenueByVendor(start, end)
                .stream()
                .map(obj -> {
                    BigDecimal amount = (BigDecimal) obj[1];
                    double percentage = amount.multiply(new BigDecimal("100"))
                            .divide(finalTotalGross, 2, java.math.RoundingMode.HALF_UP)
                            .doubleValue();
                    return VendorRevenueResponse.builder()
                            .vendorName(obj[0] != null ? (String) obj[0] : "Admin")
                            .amount(amount)
                            .percentage(percentage)
                            .build();
                })
                .sorted((v1, v2) -> v2.getAmount().compareTo(v1.getAmount()))
                .collect(Collectors.toList());

        // --- 5. TRENDS ---
        List<RevenueTrendItem> trends = orderDetailRepository.getGlobalDailyRevenue(start, end)
                .stream()
                .map(obj -> RevenueTrendItem.builder()
                        .period((String) obj[0])
                        .revenue((BigDecimal) obj[1])
                        .transactionCount((Long) obj[2])
                        .build())
                .collect(Collectors.toList());

        // --- 6. GROWTH CALCULATIONS (Tính toán tăng trưởng thực tế) ---
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        LocalDateTime prevStart = start.minusDays(daysBetween + 1);
        LocalDateTime prevEnd = start.minusNanos(1);
        BigDecimal prevTotalGross = orderDetailRepository.sumRevenueBetween(prevStart, prevEnd);

        double revGrowth = calculateGrowth(totalGross, prevTotalGross);

        // Lấy danh sách giao dịch cũ để tính admin earnings cũ
        List<OrderDetail> prevDetails = orderDetailRepository.findRevenueDetails(prevStart, prevEnd);
        BigDecimal prevAdminEarnings = BigDecimal.ZERO;
        for (OrderDetail od : prevDetails) {
            BigDecimal lineGross = od.getUnitPrice().multiply(BigDecimal.valueOf(od.getQuantity()));
            if (od.getWorkshopSession() != null) {
                prevAdminEarnings = prevAdminEarnings
                        .add(od.getCommissionAmount() != null ? od.getCommissionAmount()
                                : BigDecimal.ZERO);
            } else {
                prevAdminEarnings = prevAdminEarnings.add(lineGross);
            }
        }
        double netGrowth = calculateGrowth(adminEarnings, prevAdminEarnings);

        // Tính toán Average Order Value Growth
        double currentAOV = details.isEmpty() ? 0.0
                : totalGross.divide(BigDecimal.valueOf(details.size()), 2,
                java.math.RoundingMode.HALF_UP).doubleValue();
        double prevAOV = prevDetails.isEmpty() ? 0.0
                : prevTotalGross.divide(BigDecimal.valueOf(prevDetails.size()), 2,
                java.math.RoundingMode.HALF_UP).doubleValue();
        double aovGrowth = calculateGrowth(BigDecimal.valueOf(currentAOV), BigDecimal.valueOf(prevAOV));

        // --- 7. ĐÓNG GÓI RESPONSE ---
        return RevenueReportResponse.builder()
                .summary(RevenueSummaryResponse.builder()
                        .totalGross(totalGross)
                        .adminEarnings(adminEarnings)
                        .vendorPayouts(vendorPayouts)
                        .totalTransactions((long) details.size())
                        .revenueGrowth(revGrowth)
                        .netRevenueGrowth(netGrowth)
                        .avgOrderValueGrowth(aovGrowth)
                        .build())
                .vendorBreakdown(vendorList)
                .transactions(txDtos)
                .revenueTrends(trends)
                .build();
    }

    private double calculateGrowth(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous, 2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
}