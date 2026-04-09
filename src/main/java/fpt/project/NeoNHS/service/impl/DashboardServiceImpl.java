package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.admin.*;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.repository.*;
import fpt.project.NeoNHS.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
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
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

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
        String normalizedPeriodType = normalizePeriodType(periodType);
        int requestedPointCount = (limit != null && limit > 0) ? limit : 6;

        if ("MONTHLY".equalsIgnoreCase(normalizedPeriodType)) {
            int pointCount = requestedPointCount;
            YearMonth now = YearMonth.now();
            YearMonth startCurrent = now.minusMonths(pointCount - 1L);

            LocalDate startCurrentDate = startCurrent.atDay(1);
            LocalDate endCurrentDateExclusive = now.plusMonths(1).atDay(1);
            LocalDate startPreviousDate = startCurrentDate.minusMonths(pointCount);

            List<Map<String, Object>> rawData = orderRepository.getMonthlyRevenueTrendsBetween(
                    startPreviousDate.atStartOfDay(),
                    endCurrentDateExclusive.atStartOfDay()
            );

            Map<String, BigDecimal> revenueByPeriod = new HashMap<>();
            Map<String, Long> transactionCountByPeriod = new HashMap<>();
            for (Map<String, Object> row : rawData) {
                String key = (String) getIgnoreCase(row, "period");
                if (key == null) {
                    continue;
                }
                revenueByPeriod.merge(key, coerceBigDecimal(getIgnoreCase(row, "amount")), BigDecimal::add);

                Object countValue = getIgnoreCase(row, "transactionCount");
                if (countValue instanceof Number number) {
                    transactionCountByPeriod.merge(key, number.longValue(), Long::sum);
                }
            }

            return buildRevenueTrendsMonthly(pointCount, startCurrent, revenueByPeriod, transactionCountByPeriod);
        }

        if ("WEEKLY".equalsIgnoreCase(normalizedPeriodType)) {
            YearMonth currentMonth = YearMonth.now();
            int weekBuckets = weekBucketsInMonth(currentMonth);
            int pointCount = Math.min(requestedPointCount, weekBuckets);

            LocalDate startPreviousMonth = currentMonth.minusMonths(1).atDay(1);
            LocalDate endCurrentMonthExclusive = currentMonth.plusMonths(1).atDay(1);

            List<Map<String, Object>> rawData = orderRepository.getMonthWeekRevenueTrendsBetween(
                    startPreviousMonth.atStartOfDay(),
                    endCurrentMonthExclusive.atStartOfDay());

            Map<String, BigDecimal> revenueByPeriod = new HashMap<>();
            Map<String, Long> transactionCountByPeriod = new HashMap<>();
            for (Map<String, Object> row : rawData) {
                String key = (String) getIgnoreCase(row, "period");
                if (key == null) {
                    continue;
                }
                revenueByPeriod.merge(key, coerceBigDecimal(getIgnoreCase(row, "amount")), BigDecimal::add);

                Object countValue = getIgnoreCase(row, "transactionCount");
                if (countValue instanceof Number number) {
                    transactionCountByPeriod.merge(key, number.longValue(), Long::sum);
                }
            }

            return buildRevenueTrendsMonthWeekly(currentMonth, pointCount, revenueByPeriod, transactionCountByPeriod);
        }

        throw new BadRequestException("Invalid period type. Supported: MONTHLY, WEEKLY");
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
                .map(m -> {
                    UUID id = coerceUuid(getIgnoreCase(m, "id"));
                    if (id == null) {
                        throw new IllegalStateException("Top activity row is missing 'id'. Keys: " + m.keySet());
                    }

                    Object totalSalesValue = getIgnoreCase(m, "totalSales");
                    if (!(totalSalesValue instanceof Number)) {
                        throw new IllegalStateException("Top activity row is missing 'totalSales'. Keys: " + m.keySet());
                    }

                    return TopActivityResponse.builder()
                            .id(id)
                            .name((String) getIgnoreCase(m, "name"))
                            .ticketsSold(((Number) totalSalesValue).longValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static Object getIgnoreCase(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (var entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static UUID coerceUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof byte[] bytes) {
            return uuidFromBytes(bytes);
        }
        if (value instanceof Blob blob) {
            try {
                return uuidFromBytes(blob.getBytes(1, (int) blob.length()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to read UUID from Blob", e);
            }
        }
        if (value instanceof String str) {
            return UUID.fromString(str.trim());
        }
        return UUID.fromString(value.toString().trim());
    }

    private static UUID uuidFromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length == 16) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return new UUID(buffer.getLong(), buffer.getLong());
        }

        // Fallback: some drivers may return UUID text as bytes
        String text = new String(bytes, StandardCharsets.UTF_8).trim();
        if (text.isEmpty()) {
            return null;
        }
        return UUID.fromString(text);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationGrowthResponse getRegistrations(String type, String periodType, Integer limit) {
        String normalizedType = normalizeRegistrationType(type);
        String normalizedPeriodType = normalizePeriodType(periodType);
        int requestedPointCount = (limit != null && limit > 0) ? limit : 6;

        long touristTotal = userRepository.countByRole(UserRole.TOURIST);
        long touristActive = userRepository.countByIsActiveFalseAndIsBannedFalse(UserRole.TOURIST);
        long vendorTotal = vendorRepository.count();
        long vendorActive = vendorRepository.countByUserIsActiveTrueAndUserIsBannedFalse();

        long selectedTotal = switch (normalizedType) {
            case "USER" -> touristTotal;
            case "VENDOR" -> vendorTotal;
            default -> touristTotal + vendorTotal;
        };
        long selectedActive = switch (normalizedType) {
            case "USER" -> touristActive;
            case "VENDOR" -> vendorActive;
            default -> touristActive + vendorActive;
        };

        Double activePercentage = selectedTotal == 0
                ? 0.0
                : roundTo1Decimal(selectedActive * 100.0 / selectedTotal);

        if ("MONTHLY".equalsIgnoreCase(normalizedPeriodType)) {
            int pointCount = requestedPointCount;
            YearMonth now = YearMonth.now();
            YearMonth startCurrent = now.minusMonths(pointCount - 1L);

            LocalDate startCurrentDate = startCurrent.atDay(1);
            LocalDate endCurrentDateExclusive = now.plusMonths(1).atDay(1);
            LocalDate startPreviousDate = startCurrentDate.minusMonths(pointCount);

            Map<String, Long> touristByPeriod = toCountMap(
                    userRepository.getMonthlyTouristRegistrationStatsBetween(
                            startPreviousDate.atStartOfDay(),
                            endCurrentDateExclusive.atStartOfDay()
                    )
            );
            Map<String, Long> vendorByPeriod = toCountMap(
                    vendorRepository.getMonthlyRegistrationStatsBetween(
                            startPreviousDate.atStartOfDay(),
                            endCurrentDateExclusive.atStartOfDay()
                    )
            );

            return buildRegistrationGrowthMonthly(pointCount, startCurrent, normalizedType, touristByPeriod, vendorByPeriod, activePercentage);
        }

        if ("WEEKLY".equalsIgnoreCase(normalizedPeriodType)) {
            YearMonth currentMonth = YearMonth.now();
            int weekBuckets = weekBucketsInMonth(currentMonth);
            int pointCount = Math.min(requestedPointCount, weekBuckets);

            LocalDate startPreviousMonth = currentMonth.minusMonths(1).atDay(1);
            LocalDate endCurrentMonthExclusive = currentMonth.plusMonths(1).atDay(1);

            Map<String, Long> touristByPeriod = toCountMap(
                    userRepository.getMonthWeekTouristRegistrationStatsBetween(
                            startPreviousMonth.atStartOfDay(),
                            endCurrentMonthExclusive.atStartOfDay()
                    )
            );
            Map<String, Long> vendorByPeriod = toCountMap(
                    vendorRepository.getMonthWeekRegistrationStatsBetween(
                            startPreviousMonth.atStartOfDay(),
                            endCurrentMonthExclusive.atStartOfDay()
                    )
            );

            return buildRegistrationGrowthMonthWeekly(currentMonth, pointCount, normalizedType, touristByPeriod, vendorByPeriod, activePercentage);
        }

        throw new BadRequestException("Invalid period type. Supported: MONTHLY, WEEKLY");
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
        workshopRepository.findRecentApprovedReviews(pageable)
                .forEach(w -> activities.add(
                        VendorActivityResponse.builder()
                                .vendorId(w.getVendor().getId())
                                .vendorName(w.getVendor().getBusinessName())
                                .action("APPROVE_WORKSHOP")
                                .targetName(w.getName())
                                .time(w.getReviewedAt())
                                .build()
                ));

        // 5️⃣ Sắp xếp và giới hạn
        return activities.stream()
                .sorted(Comparator.comparing(VendorActivityResponse::getTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private static String normalizePeriodType(String periodType) {
        return (periodType == null || periodType.isBlank())
                ? "MONTHLY"
                : periodType.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeRegistrationType(String type) {
        String normalized = (type == null || type.isBlank())
                ? "ALL"
                : type.trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(normalized) || "USER".equals(normalized) || "VENDOR".equals(normalized)) {
            return normalized;
        }
        throw new BadRequestException("Invalid type. Supported: ALL, USER, VENDOR");
    }

    private static Map<String, Long> toCountMap(List<Map<String, Object>> rawData) {
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> row : rawData) {
            String period = (String) getIgnoreCase(row, "period");
            Object countValue = getIgnoreCase(row, "count");
            if (period != null && countValue instanceof Number number) {
                map.merge(period, number.longValue(), Long::sum);
            }
        }
        return map;
    }

    private static double roundTo1Decimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static Double growthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current == null || current.compareTo(BigDecimal.ZERO) == 0) {
                return 0.0;
            }
            return 100.0;
        }

        BigDecimal rate = current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 4, RoundingMode.HALF_UP);
        return roundTo1Decimal(rate.doubleValue());
    }

    private static Double growthRate(long current, long previous) {
        if (previous == 0) {
            return current == 0 ? 0.0 : 100.0;
        }
        return roundTo1Decimal((current - previous) * 100.0 / previous);
    }

    private static String toWeekLabel(String mysqlWeekKey) {
        if (mysqlWeekKey == null || mysqlWeekKey.isBlank()) {
            return mysqlWeekKey;
        }
        String lower = mysqlWeekKey.toLowerCase(Locale.ROOT);
        int weekIndex = lower.indexOf("week");
        if (weekIndex < 0) {
            return mysqlWeekKey;
        }

        String suffix = mysqlWeekKey.substring(weekIndex + 4).trim();
        if (suffix.isEmpty()) {
            return mysqlWeekKey;
        }

        try {
            String digits = suffix.replaceFirst("^0+", "");
            if (digits.isEmpty()) {
                digits = "0";
            }
            int weekNumber = Integer.parseInt(digits);
            return "Week " + weekNumber;
        } catch (NumberFormatException ignored) {
            return mysqlWeekKey;
        }
    }

    private static RevenueTrendsResponse buildRevenueTrendsMonthly(
            int pointCount,
            YearMonth startCurrent,
            Map<String, BigDecimal> revenueByPeriod,
            Map<String, Long> transactionCountByPeriod
    ) {
        List<RevenueTrendsResponse.TrendPoint> trends = new ArrayList<>(pointCount);

        BigDecimal currentTotal = BigDecimal.ZERO;
        BigDecimal previousTotal = BigDecimal.ZERO;
        BigDecimal peakValue = BigDecimal.ZERO;
        String peakPeriod = null;

        for (int i = 0; i < pointCount; i++) {
            YearMonth currentYm = startCurrent.plusMonths(i);
            YearMonth previousYm = currentYm.minusMonths(pointCount);

            String periodKey = currentYm.format(YEAR_MONTH_FORMATTER);
            String previousPeriodKey = previousYm.format(YEAR_MONTH_FORMATTER);

            BigDecimal revenue = revenueByPeriod.getOrDefault(periodKey, BigDecimal.ZERO);
            BigDecimal previousRevenue = revenueByPeriod.getOrDefault(previousPeriodKey, BigDecimal.ZERO);
            long transactionCount = transactionCountByPeriod.getOrDefault(periodKey, 0L);

            String label = currentYm.format(MONTH_LABEL_FORMATTER);

            currentTotal = currentTotal.add(revenue);
            previousTotal = previousTotal.add(previousRevenue);

            if (peakPeriod == null || revenue.compareTo(peakValue) > 0) {
                peakValue = revenue;
                peakPeriod = label;
            }

            trends.add(RevenueTrendsResponse.TrendPoint.builder()
                    .periodKey(periodKey)
                    .period(label)
                    .revenue(revenue)
                    .previousRevenue(previousRevenue)
                    .transactionCount(transactionCount)
                    .build());
        }

        BigDecimal averageValue = pointCount == 0
                ? BigDecimal.ZERO
                : currentTotal.divide(BigDecimal.valueOf(pointCount), 0, RoundingMode.HALF_UP);

        return RevenueTrendsResponse.builder()
                .summary(RevenueTrendsResponse.Summary.builder()
                        .currentTotal(currentTotal)
                        .previousTotal(previousTotal)
                        .growthRate(growthRate(currentTotal, previousTotal))
                        .averageValue(averageValue)
                        .peakValue(peakValue)
                        .peakPeriod(peakPeriod)
                        .build())
                .trends(trends)
                .metadata(RevenueTrendsResponse.Metadata.builder()
                        .currency("VND")
                        .periodType("MONTHLY")
                        .pointCount(pointCount)
                        .build())
                .build();
    }

    private static RevenueTrendsResponse buildRevenueTrendsWeekly(
            int pointCount,
            LocalDate startCurrentMonday,
            Map<String, BigDecimal> revenueByPeriod,
            Map<String, Long> transactionCountByPeriod
    ) {
        List<RevenueTrendsResponse.TrendPoint> trends = new ArrayList<>(pointCount);

        BigDecimal currentTotal = BigDecimal.ZERO;
        BigDecimal previousTotal = BigDecimal.ZERO;
        BigDecimal peakValue = BigDecimal.ZERO;
        String peakPeriod = null;

        for (int i = 0; i < pointCount; i++) {
            LocalDate currentWeekStart = startCurrentMonday.plusWeeks(i);
            LocalDate previousWeekStart = currentWeekStart.minusWeeks(pointCount);

            String periodKey = formatMysqlWeekU(currentWeekStart);
            String previousPeriodKey = formatMysqlWeekU(previousWeekStart);

            BigDecimal revenue = revenueByPeriod.getOrDefault(periodKey, BigDecimal.ZERO);
            BigDecimal previousRevenue = revenueByPeriod.getOrDefault(previousPeriodKey, BigDecimal.ZERO);
            long transactionCount = transactionCountByPeriod.getOrDefault(periodKey, 0L);

            String label = toWeekLabel(periodKey);

            currentTotal = currentTotal.add(revenue);
            previousTotal = previousTotal.add(previousRevenue);

            if (peakPeriod == null || revenue.compareTo(peakValue) > 0) {
                peakValue = revenue;
                peakPeriod = label;
            }

            trends.add(RevenueTrendsResponse.TrendPoint.builder()
                    .periodKey(periodKey)
                    .period(label)
                    .revenue(revenue)
                    .previousRevenue(previousRevenue)
                    .transactionCount(transactionCount)
                    .build());
        }

        BigDecimal averageValue = pointCount == 0
                ? BigDecimal.ZERO
                : currentTotal.divide(BigDecimal.valueOf(pointCount), 0, RoundingMode.HALF_UP);

        return RevenueTrendsResponse.builder()
                .summary(RevenueTrendsResponse.Summary.builder()
                        .currentTotal(currentTotal)
                        .previousTotal(previousTotal)
                        .growthRate(growthRate(currentTotal, previousTotal))
                        .averageValue(averageValue)
                        .peakValue(peakValue)
                        .peakPeriod(peakPeriod)
                        .build())
                .trends(trends)
                .metadata(RevenueTrendsResponse.Metadata.builder()
                        .currency("VND")
                        .periodType("WEEKLY")
                        .pointCount(pointCount)
                        .build())
                .build();
    }

    private static RevenueTrendsResponse buildRevenueTrendsMonthWeekly(
            YearMonth currentMonth,
            int pointCount,
            Map<String, BigDecimal> revenueByPeriod,
            Map<String, Long> transactionCountByPeriod
    ) {
        List<RevenueTrendsResponse.TrendPoint> trends = new ArrayList<>(pointCount);

        YearMonth previousMonth = currentMonth.minusMonths(1);

        BigDecimal currentTotal = BigDecimal.ZERO;
        BigDecimal previousTotal = BigDecimal.ZERO;
        BigDecimal peakValue = BigDecimal.ZERO;
        String peakPeriod = null;

        for (int weekBucket = 1; weekBucket <= pointCount; weekBucket++) {
            String periodKey = formatMonthWeekKey(currentMonth, weekBucket);
            String previousPeriodKey = formatMonthWeekKey(previousMonth, weekBucket);

            BigDecimal revenue = revenueByPeriod.getOrDefault(periodKey, BigDecimal.ZERO);
            BigDecimal previousRevenue = revenueByPeriod.getOrDefault(previousPeriodKey, BigDecimal.ZERO);
            long transactionCount = transactionCountByPeriod.getOrDefault(periodKey, 0L);

            String label = "Week " + weekBucket;

            currentTotal = currentTotal.add(revenue);
            previousTotal = previousTotal.add(previousRevenue);

            if (peakPeriod == null || revenue.compareTo(peakValue) > 0) {
                peakValue = revenue;
                peakPeriod = label;
            }

            trends.add(RevenueTrendsResponse.TrendPoint.builder()
                    .periodKey(periodKey)
                    .period(label)
                    .revenue(revenue)
                    .previousRevenue(previousRevenue)
                    .transactionCount(transactionCount)
                    .build());
        }

        BigDecimal averageValue = pointCount == 0
                ? BigDecimal.ZERO
                : currentTotal.divide(BigDecimal.valueOf(pointCount), 0, RoundingMode.HALF_UP);

        return RevenueTrendsResponse.builder()
                .summary(RevenueTrendsResponse.Summary.builder()
                        .currentTotal(currentTotal)
                        .previousTotal(previousTotal)
                        .growthRate(growthRate(currentTotal, previousTotal))
                        .averageValue(averageValue)
                        .peakValue(peakValue)
                        .peakPeriod(peakPeriod)
                        .build())
                .trends(trends)
                .metadata(RevenueTrendsResponse.Metadata.builder()
                        .currency("VND")
                        .periodType("WEEKLY")
                        .pointCount(pointCount)
                        .build())
                .build();
    }

    private static RegistrationGrowthResponse buildRegistrationGrowthMonthly(
            int pointCount,
            YearMonth startCurrent,
            String normalizedType,
            Map<String, Long> touristByPeriod,
            Map<String, Long> vendorByPeriod,
            Double activePercentage
    ) {
        List<RegistrationGrowthResponse.TrendPoint> trends = new ArrayList<>(pointCount);

        long currentTotal = 0;
        long previousTotal = 0;

        for (int i = 0; i < pointCount; i++) {
            YearMonth currentYm = startCurrent.plusMonths(i);
            YearMonth previousYm = currentYm.minusMonths(pointCount);

            String periodKey = currentYm.format(YEAR_MONTH_FORMATTER);
            String previousPeriodKey = previousYm.format(YEAR_MONTH_FORMATTER);

            long currentIndividuals = touristByPeriod.getOrDefault(periodKey, 0L);
            long currentOrganizations = vendorByPeriod.getOrDefault(periodKey, 0L);
            long previousIndividuals = touristByPeriod.getOrDefault(previousPeriodKey, 0L);
            long previousOrganizations = vendorByPeriod.getOrDefault(previousPeriodKey, 0L);

            long breakdownIndividuals = switch (normalizedType) {
                case "VENDOR" -> 0L;
                default -> currentIndividuals;
            };
            long breakdownOrganizations = switch (normalizedType) {
                case "USER" -> 0L;
                default -> currentOrganizations;
            };

            long count = breakdownIndividuals + breakdownOrganizations;
            long previousCount = switch (normalizedType) {
                case "USER" -> previousIndividuals;
                case "VENDOR" -> previousOrganizations;
                default -> previousIndividuals + previousOrganizations;
            };

            currentTotal += count;
            previousTotal += previousCount;

            trends.add(RegistrationGrowthResponse.TrendPoint.builder()
                    .periodKey(periodKey)
                    .period(currentYm.format(MONTH_LABEL_FORMATTER))
                    .count(count)
                    .previousCount(previousCount)
                    .breakdown(RegistrationGrowthResponse.Breakdown.builder()
                            .individual(breakdownIndividuals)
                            .organization(breakdownOrganizations)
                            .build())
                    .build());
        }

        return RegistrationGrowthResponse.builder()
                .summary(RegistrationGrowthResponse.Summary.builder()
                        .totalJoined(currentTotal)
                        .previousTotal(previousTotal)
                        .growthRate(growthRate(currentTotal, previousTotal))
                        .activePercentage(activePercentage)
                        .build())
                .trends(trends)
                .build();
    }

    private static RegistrationGrowthResponse buildRegistrationGrowthWeekly(
            int pointCount,
            LocalDate startCurrentMonday,
            String normalizedType,
            Map<String, Long> touristByPeriod,
            Map<String, Long> vendorByPeriod,
            Double activePercentage
    ) {
        List<RegistrationGrowthResponse.TrendPoint> trends = new ArrayList<>(pointCount);

        long currentTotal = 0;
        long previousTotal = 0;

        for (int i = 0; i < pointCount; i++) {
            LocalDate currentWeekStart = startCurrentMonday.plusWeeks(i);
            LocalDate previousWeekStart = currentWeekStart.minusWeeks(pointCount);

            String periodKey = formatMysqlWeekU(currentWeekStart);
            String previousPeriodKey = formatMysqlWeekU(previousWeekStart);

            long currentIndividuals = touristByPeriod.getOrDefault(periodKey, 0L);
            long currentOrganizations = vendorByPeriod.getOrDefault(periodKey, 0L);
            long previousIndividuals = touristByPeriod.getOrDefault(previousPeriodKey, 0L);
            long previousOrganizations = vendorByPeriod.getOrDefault(previousPeriodKey, 0L);

            long breakdownIndividuals = switch (normalizedType) {
                case "VENDOR" -> 0L;
                default -> currentIndividuals;
            };
            long breakdownOrganizations = switch (normalizedType) {
                case "USER" -> 0L;
                default -> currentOrganizations;
            };

            long count = breakdownIndividuals + breakdownOrganizations;
            long previousCount = switch (normalizedType) {
                case "USER" -> previousIndividuals;
                case "VENDOR" -> previousOrganizations;
                default -> previousIndividuals + previousOrganizations;
            };

            currentTotal += count;
            previousTotal += previousCount;

            trends.add(RegistrationGrowthResponse.TrendPoint.builder()
                    .periodKey(periodKey)
                    .period(toWeekLabel(periodKey))
                    .count(count)
                    .previousCount(previousCount)
                    .breakdown(RegistrationGrowthResponse.Breakdown.builder()
                            .individual(breakdownIndividuals)
                            .organization(breakdownOrganizations)
                            .build())
                    .build());
        }

        return RegistrationGrowthResponse.builder()
                .summary(RegistrationGrowthResponse.Summary.builder()
                        .totalJoined(currentTotal)
                        .previousTotal(previousTotal)
                        .growthRate(growthRate(currentTotal, previousTotal))
                        .activePercentage(activePercentage)
                        .build())
                .trends(trends)
                .build();
    }

    private static RegistrationGrowthResponse buildRegistrationGrowthMonthWeekly(
            YearMonth currentMonth,
            int pointCount,
            String normalizedType,
            Map<String, Long> touristByPeriod,
            Map<String, Long> vendorByPeriod,
            Double activePercentage
    ) {
        List<RegistrationGrowthResponse.TrendPoint> trends = new ArrayList<>(pointCount);

        YearMonth previousMonth = currentMonth.minusMonths(1);

        long currentTotal = 0;
        long previousTotal = 0;

        for (int weekBucket = 1; weekBucket <= pointCount; weekBucket++) {
            String periodKey = formatMonthWeekKey(currentMonth, weekBucket);
            String previousPeriodKey = formatMonthWeekKey(previousMonth, weekBucket);

            long currentIndividuals = touristByPeriod.getOrDefault(periodKey, 0L);
            long currentOrganizations = vendorByPeriod.getOrDefault(periodKey, 0L);
            long previousIndividuals = touristByPeriod.getOrDefault(previousPeriodKey, 0L);
            long previousOrganizations = vendorByPeriod.getOrDefault(previousPeriodKey, 0L);

            long breakdownIndividuals = switch (normalizedType) {
                case "VENDOR" -> 0L;
                default -> currentIndividuals;
            };
            long breakdownOrganizations = switch (normalizedType) {
                case "USER" -> 0L;
                default -> currentOrganizations;
            };

            long count = breakdownIndividuals + breakdownOrganizations;
            long previousCount = switch (normalizedType) {
                case "USER" -> previousIndividuals;
                case "VENDOR" -> previousOrganizations;
                default -> previousIndividuals + previousOrganizations;
            };

            currentTotal += count;
            previousTotal += previousCount;

            trends.add(RegistrationGrowthResponse.TrendPoint.builder()
                    .periodKey(periodKey)
                    .period("Week " + weekBucket)
                    .count(count)
                    .previousCount(previousCount)
                    .breakdown(RegistrationGrowthResponse.Breakdown.builder()
                            .individual(breakdownIndividuals)
                            .organization(breakdownOrganizations)
                            .build())
                    .build());
        }

        return RegistrationGrowthResponse.builder()
                .summary(RegistrationGrowthResponse.Summary.builder()
                        .totalJoined(currentTotal)
                        .previousTotal(previousTotal)
                        .growthRate(growthRate(currentTotal, previousTotal))
                        .activePercentage(activePercentage)
                        .build())
                .trends(trends)
                .build();
    }

    // Matches MySQL DATE_FORMAT(..., '%Y-Week %u'): week number 00-53, Monday as first day,
    // week 1 starts on the first Monday of the year; days before that are week 0.
    private static String formatMysqlWeekU(LocalDate date) {
        int year = date.getYear();
        LocalDate firstMonday = LocalDate.of(year, 1, 1);
        while (firstMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
            firstMonday = firstMonday.plusDays(1);
        }

        int week = 0;
        if (!date.isBefore(firstMonday)) {
            long days = ChronoUnit.DAYS.between(firstMonday, date);
            week = (int) (days / 7) + 1;
        }

        return String.format("%d-Week %02d", year, week);
    }

    private static String formatMonthWeekKey(YearMonth month, int weekBucket) {
        return month.format(YEAR_MONTH_FORMATTER) + "-W" + weekBucket;
    }

    private static int weekBucketsInMonth(YearMonth month) {
        if (month == null) {
            return 0;
        }
        return (month.lengthOfMonth() + 6) / 7;
    }

    private static BigDecimal coerceBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }
}
