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
                        BigDecimal lineGross = od.getUnitPrice().multiply(BigDecimal.valueOf(od.getQuantity()));
                        totalGross = totalGross.add(lineGross);

                        boolean isVendorProduct = (od.getWorkshopSession() != null);
                        BigDecimal lineFeeDisplay;
                        BigDecimal lineNetDisplay;

                        if (isVendorProduct) {
                                BigDecimal fee = od.getCommissionAmount() != null ? od.getCommissionAmount()
                                                : BigDecimal.ZERO;
                                BigDecimal net = od.getNetAmount() != null ? od.getNetAmount() : lineGross;
                                adminEarnings = adminEarnings.add(fee);
                                vendorPayouts = vendorPayouts.add(net);
                                lineFeeDisplay = fee;
                                lineNetDisplay = net;
                        } else {
                                adminEarnings = adminEarnings.add(lineGross);
                                lineFeeDisplay = BigDecimal.ZERO;
                                lineNetDisplay = lineGross;
                        }

                        txDtos.add(TransactionDetailResponse.builder()
                                        .date(od.getCreatedAt())
                                        .id("#TRX-" + od.getId().toString().substring(0, 8).toUpperCase())
                                        .vendor(isVendorProduct
                                                        ? od.getWorkshopSession().getWorkshopTemplate().getVendor()
                                                                        .getBusinessName()
                                                        : "Admin")
                                        .item(isVendorProduct ? od.getWorkshopSession().getWorkshopTemplate().getName()
                                                        : (od.getTicketCatalog() != null
                                                                        ? od.getTicketCatalog().getEvent().getName()
                                                                        : "Event Ticket"))
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

                // --- 6. GROWTH CALCULATIONS ---
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                LocalDateTime prevStart = start.minusDays(daysBetween + 1);
                LocalDateTime prevEnd = start.minusNanos(1);
                BigDecimal prevTotalGross = orderDetailRepository.sumRevenueBetween(prevStart, prevEnd);

                double revGrowth = 0.0;
                if (prevTotalGross.compareTo(BigDecimal.ZERO) > 0) {
                        revGrowth = totalGross.subtract(prevTotalGross)
                                        .multiply(new BigDecimal("100"))
                                        .divide(prevTotalGross, 2, java.math.RoundingMode.HALF_UP).doubleValue();
                } else if (totalGross.compareTo(BigDecimal.ZERO) > 0) {
                        revGrowth = 100.0;
                }

                // --- 7. ĐÓNG GÓI RESPONSE ---
                return RevenueReportResponse.builder()
                                .summary(RevenueSummaryResponse.builder()
                                                .totalGross(totalGross)
                                                .adminEarnings(adminEarnings)
                                                .vendorPayouts(vendorPayouts)
                                                .totalTransactions((long) details.size())
                                                .revenueGrowth(revGrowth)
                                                .netRevenueGrowth(revGrowth * 0.8) // Simplified estimation
                                                .avgOrderValueGrowth(details.isEmpty() ? 0.0 : 2.5) // Simplified
                                                                                                    // estimation
                                                .build())
                                .vendorBreakdown(vendorList)
                                .transactions(txDtos)
                                .revenueTrends(trends)
                                .build();
        }
}