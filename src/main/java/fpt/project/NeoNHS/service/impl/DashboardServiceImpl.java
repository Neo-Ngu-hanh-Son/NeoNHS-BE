package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.admin.*;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.repository.*;
import fpt.project.NeoNHS.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final WorkshopTemplateRepository workshopRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public KpiOverviewResponse getKpiOverview() {
        Double totalRevenue = orderRepository.sumTotalRevenue();
        return KpiOverviewResponse.builder()
                .totalUsers((int) userRepository.count())
                .activeVendors((int) vendorRepository.count())
                .ticketsSold((int) ticketRepository.count())
                .revenue(BigDecimal.valueOf(totalRevenue != null ? totalRevenue : 0.0))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueTrendsResponse getRevenueTrends(String periodType, Integer limit) {
        List<Map<String, Object>> rawData;
        if ("MONTHLY".equalsIgnoreCase(periodType)) {
            rawData = orderRepository.getMonthlyRevenueTrends(limit);
        } else if ("WEEKLY".equalsIgnoreCase(periodType)) {
            rawData = orderRepository.getWeeklyRevenueTrends(limit);
        } else {
            throw new BadRequestException("Invalid period type. Supported: MONTHLY, WEEKLY");
        }

        List<RevenueTrendItem> items = rawData.stream()
                .map(m -> RevenueTrendItem.builder()
                        .period((String) m.get("period"))
                        .revenue(BigDecimal.valueOf(((Number) m.get("amount")).doubleValue()))
                        .build())
                .collect(Collectors.toList());

        return RevenueTrendsResponse.builder()
                .monthly("MONTHLY".equalsIgnoreCase(periodType) ? items : new ArrayList<>())
                .weekly("WEEKLY".equalsIgnoreCase(periodType) ? items : new ArrayList<>())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StatusCountResponse getActivityStatus() {
        return StatusCountResponse.builder()
                .workshop(Map.of("TOTAL", workshopRepository.count()))
                .event(Map.of("TOTAL", eventRepository.count()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesByTypeResponse getSalesByType() {
        List<Map<String, Object>> salesData = orderRepository.getRevenueByTicketType();

        SalesByTypeResponse.Type workshopType = new SalesByTypeResponse.Type(0L, BigDecimal.ZERO);
        SalesByTypeResponse.Type eventType = new SalesByTypeResponse.Type(0L, BigDecimal.ZERO);

        for (Map<String, Object> m : salesData) {
            String type = (String) m.get("type");
            long sold = ((Number) m.get("totalQuantity")).longValue();
            BigDecimal rev = BigDecimal.valueOf(((Number) m.get("totalRevenue")).doubleValue());

            if ("WORKSHOP".equalsIgnoreCase(type)) {
                workshopType = new SalesByTypeResponse.Type(sold, rev);
            } else if ("EVENT".equalsIgnoreCase(type)) {
                eventType = new SalesByTypeResponse.Type(sold, rev);
            }
        }

        return SalesByTypeResponse.builder()
                .workshop(workshopType)
                .event(eventType)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopActivityResponse> getTopActivities(String type, Integer limit) {
        List<Map<String, Object>> rawData;
        if ("WORKSHOP".equalsIgnoreCase(type)) {
            rawData = workshopRepository.findTopWorkshopsBySales(limit);
        } else if ("EVENT".equalsIgnoreCase(type)) {
            rawData = eventRepository.findTopEventsBySales(limit);
        } else {
            throw new BadRequestException("Invalid activity type. Expected WORKSHOP or EVENT");
        }

        return rawData.stream()
                .map(m -> TopActivityResponse.builder()
                        .id(UUID.fromString(m.get("id").toString()))
                        .name((String) m.get("name"))
                        .ticketsSold(((Number) m.get("totalSales")).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationStatResponse> getRegistrations(String type, String periodType, Integer limit) {
        List<Map<String, Object>> rawData = "USER".equalsIgnoreCase(type)
                ? userRepository.getMonthlyRegistrationStats(limit)
                : vendorRepository.getMonthlyRegistrationStats(limit);

        return rawData.stream()
                .map(m -> RegistrationStatResponse.builder()
                        .period((String) m.get("period"))
                        .count(((Number) m.get("count")).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorActivityResponse> getRecentVendorActivities(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<VendorActivityResponse> activities = new ArrayList<>();

        // 1️⃣ Workshop created (Hoạt động của Vendor)
        workshopRepository.findRecentCreated(pageable)
                .forEach(w -> activities.add(
                        VendorActivityResponse.builder()
                                .vendorId(w.getVendor().getId())
                                .vendorName(w.getVendor().getBusinessName())
                                .action("CREATE_WORKSHOP")
                                .targetName(w.getName())
                                .time(w.getCreatedAt())
                                .build()
                ));

        // 2️⃣ Event created (Hoạt động của Hệ thống/Admin)
        eventRepository.findRecentCreated(pageable)
                .forEach(e -> activities.add(
                        VendorActivityResponse.builder()
                                .vendorId(null)
                                .vendorName("Admin")
                                .action("CREATE_EVENT")
                                .targetName(e.getName())
                                .time(e.getCreatedAt())
                                .build()
                ));

        // 3️⃣ Ticket sold (Hoạt động bán vé)
        ticketRepository.findRecentSold(pageable)
                .forEach(t -> {
                    String name;
                    UUID id = null;
                    String target;

                    if (t.getWorkshopSession() != null) {
                        var vendor = t.getWorkshopSession().getWorkshopTemplate().getVendor();
                        id = vendor.getId();
                        name = vendor.getBusinessName();
                        target = t.getWorkshopSession().getWorkshopTemplate().getName();
                    } else {
                        name = "Admin";
                        target = t.getTicketCatalog().getEvent().getName();
                    }

                    activities.add(VendorActivityResponse.builder()
                            .vendorId(id)
                            .vendorName(name)
                            .action("SELL_TICKET")
                            .targetName(target)
                            .time(t.getCreatedAt())
                            .build());
                });

        // 4️⃣ Workshop approved (Hoạt động Admin phê duyệt cho Vendor)
        workshopRepository.findRecentApproved(pageable)
                .forEach(w -> activities.add(
                        VendorActivityResponse.builder()
                                .vendorId(w.getVendor().getId())
                                .vendorName(w.getVendor().getBusinessName())
                                .action("APPROVE_WORKSHOP")
                                .targetName(w.getName())
                                .time(w.getApprovedAt())
                                .build()
                ));

        // 5️⃣ Sắp xếp và giới hạn
        return activities.stream()
                .sorted(Comparator.comparing(VendorActivityResponse::getTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }
}