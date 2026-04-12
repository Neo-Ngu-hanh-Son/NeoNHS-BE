package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.vendor.dashboard.*;
import fpt.project.NeoNHS.entity.Transaction;
import fpt.project.NeoNHS.entity.VendorProfile;
import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.helpers.AuthHelper;
import fpt.project.NeoNHS.repository.*;
import fpt.project.NeoNHS.service.VendorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendorDashboardServiceImpl implements VendorDashboardService {

    private final VendorProfileRepository vendorProfileRepository;
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;
    private final VoucherRepository voucherRepository;

    // ─── Public API methods ─────────────────────────────────────────

    @Override
    public VendorStatsResponse getStats(String timezone) {
        UUID vendorId = resolveCurrentVendorId();
        LocalDateTime now = LocalDateTime.now(parseZone(timezone));
        return buildStats(vendorId, now);
    }

    @Override
    public VendorRevenueSeriesResponse getRevenue(String range, String timezone) {
        UUID vendorId = resolveCurrentVendorId();
        LocalDateTime now = LocalDateTime.now(parseZone(timezone));
        return buildRevenueSeries(vendorId, range, now);
    }

    @Override
    public List<VendorWorkshopStatusItem> getWorkshopStatus() {
        UUID vendorId = resolveCurrentVendorId();
        return buildWorkshopStatus(vendorId);
    }

    @Override
    public List<VendorTransactionItem> getTransactions(int limit) {
        UUID vendorId = resolveCurrentVendorId();
        return buildTransactions(vendorId, limit);
    }

    @Override
    public List<VendorWorkshopReviewItem> getWorkshopReviews(String timezone, int limit) {
        UUID vendorId = resolveCurrentVendorId();
        LocalDateTime now = LocalDateTime.now(parseZone(timezone));
        return buildWorkshopReviews(vendorId, now, limit);
    }

    @Override
    public VendorSessionsResponse getSessions(String from, String to, String timezone) {
        UUID vendorId = resolveCurrentVendorId();
        ZoneId zone = parseZone(timezone);
        LocalDateTime now = LocalDateTime.now(zone);

        LocalDate fromDate = (from != null && !from.isBlank())
                ? LocalDate.parse(from)
                : now.toLocalDate().withDayOfMonth(1);
        LocalDate toDate = (to != null && !to.isBlank())
                ? LocalDate.parse(to)
                : fromDate.plusMonths(1);

        return buildSessions(vendorId, fromDate, toDate);
    }

    // ─── Internals ──────────────────────────────────────────────────

    private UUID resolveCurrentVendorId() {
        String email = AuthHelper.getCurrentUserEmail();
        VendorProfile vendor = vendorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for current user"));
        return vendor.getId();
    }

    private ZoneId parseZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneId.of("Asia/Ho_Chi_Minh");
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            return ZoneId.of("Asia/Ho_Chi_Minh");
        }
    }

    // ─── Stats cards ────────────────────────────────────────────────

    private VendorStatsResponse buildStats(UUID vendorId, LocalDateTime now) {
        LocalDateTime weekAgo = now.minusWeeks(1);
        LocalDateTime twoWeeksAgo = now.minusWeeks(2);

        BigDecimal totalRevenue = safeDecimal(orderDetailRepository.sumTotalRevenueByVendorId(vendorId));
        BigDecimal previousRevenue = null; // No trend mapping for all-time total revenue

        long currentWorkshops = workshopTemplateRepository.countByVendorIdAndDeletedAtIsNull(vendorId);
        long currentBookings = workshopSessionRepository.countBookingsByVendorId(vendorId);
        long previousBookings = workshopSessionRepository.countBookingsByVendorIdSince(vendorId, weekAgo);
        long currentVouchers = voucherRepository.countByVendorIdAndDeletedAtIsNull(vendorId);
        long previousVouchersDelta = voucherRepository.countByVendorIdAndDeletedAtIsNullAndCreatedAtAfter(vendorId, weekAgo);

        return VendorStatsResponse.builder()
                .revenue(buildStatCard(totalRevenue, previousRevenue, "VND"))
                .workshops(buildStatCard(BigDecimal.valueOf(currentWorkshops), null, null))
                .bookings(buildStatCard(BigDecimal.valueOf(currentBookings), BigDecimal.valueOf(currentBookings - previousBookings), null))
                .vouchers(buildStatCard(BigDecimal.valueOf(currentVouchers), BigDecimal.valueOf(currentVouchers - previousVouchersDelta), null))
                .build();
    }

    private VendorStatCard buildStatCard(BigDecimal current, BigDecimal previous, String currency) {
        double trendPercent = 0.0;
        String direction = "flat";

        if (previous != null && previous.compareTo(BigDecimal.ZERO) > 0) {
            trendPercent = current.subtract(previous)
                    .divide(previous, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
            direction = trendPercent > 0 ? "up" : trendPercent < 0 ? "down" : "flat";
            trendPercent = Math.abs(trendPercent);
        }

        return VendorStatCard.builder()
                .value(current)
                .currency(currency)
                .trendPercent(trendPercent)
                .trendDirection(direction)
                .build();
    }

    // ─── Revenue series ─────────────────────────────────────────────

    private VendorRevenueSeriesResponse buildRevenueSeries(UUID vendorId, String range, LocalDateTime now) {
        if (range == null) range = "week";

        return switch (range.toLowerCase()) {
            case "month" -> buildMonthlyDayRevenue(vendorId, now);
            case "year" -> buildYearlyRevenue(vendorId, now);
            default -> buildWeeklyRevenue(vendorId, now);
        };
    }

    private VendorRevenueSeriesResponse buildWeeklyRevenue(UUID vendorId, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusDays(7).atStartOfDay();

        List<Object[]> raw = orderDetailRepository.getDailyRevenueByVendorId(vendorId, weekStart, weekEnd);
        Map<LocalDate, BigDecimal> revenueMap = new LinkedHashMap<>();
        for (Object[] row : raw) {
            LocalDate date;
            if (row[0] instanceof java.sql.Date sqlDate) {
                date = sqlDate.toLocalDate();
            } else if (row[0] instanceof java.time.LocalDate localDate) {
                date = localDate;
            } else if (row[0] instanceof java.time.LocalDateTime localDateTime) {
                date = localDateTime.toLocalDate();
            } else {
                date = LocalDate.parse(row[0].toString());
            }
            BigDecimal revenue = new BigDecimal(row[1].toString());
            revenueMap.put(date, revenue);
        }

        List<VendorRevenuePoint> points = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            String label = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            BigDecimal revenue = revenueMap.getOrDefault(day, BigDecimal.ZERO);
            points.add(VendorRevenuePoint.builder().label(label).revenue(revenue).build());
        }

        return VendorRevenueSeriesResponse.builder().range("week").points(points).build();
    }

    private VendorRevenueSeriesResponse buildMonthlyDayRevenue(UUID vendorId, LocalDateTime now) {
        LocalDate firstOfMonth = now.toLocalDate().withDayOfMonth(1);
        LocalDate firstOfNextMonth = firstOfMonth.plusMonths(1);
        LocalDateTime monthStart = firstOfMonth.atStartOfDay();
        LocalDateTime monthEnd = firstOfNextMonth.atStartOfDay();

        List<Object[]> raw = orderDetailRepository.getDailyRevenueByVendorId(vendorId, monthStart, monthEnd);
        Map<LocalDate, BigDecimal> revenueMap = new LinkedHashMap<>();
        for (Object[] row : raw) {
            LocalDate date;
            if (row[0] instanceof java.sql.Date sqlDate) {
                date = sqlDate.toLocalDate();
            } else if (row[0] instanceof java.time.LocalDate localDate) {
                date = localDate;
            } else if (row[0] instanceof java.time.LocalDateTime localDateTime) {
                date = localDateTime.toLocalDate();
            } else {
                date = LocalDate.parse(row[0].toString());
            }
            BigDecimal revenue = new BigDecimal(row[1].toString());
            revenueMap.put(date, revenue);
        }

        List<VendorRevenuePoint> points = new ArrayList<>();
        int daysInMonth = firstOfMonth.lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate day = firstOfMonth.withDayOfMonth(d);
            BigDecimal revenue = revenueMap.getOrDefault(day, BigDecimal.ZERO);
            points.add(VendorRevenuePoint.builder().label(String.valueOf(d)).revenue(revenue).build());
        }

        return VendorRevenueSeriesResponse.builder().range("month").points(points).build();
    }

    private VendorRevenueSeriesResponse buildYearlyRevenue(UUID vendorId, LocalDateTime now) {
        int year = now.getYear();
        LocalDateTime yearStart = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime yearEnd = LocalDate.of(year + 1, 1, 1).atStartOfDay();

        List<Object[]> raw = orderDetailRepository.getMonthlyRevenueByVendorId(vendorId, yearStart, yearEnd);
        Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();
        for (Object[] row : raw) {
            String monthKey = row[0].toString();
            BigDecimal revenue = new BigDecimal(row[1].toString());
            revenueMap.put(monthKey, revenue);
        }

        List<VendorRevenuePoint> points = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Month month = Month.of(m);
            String key = String.format("%d-%02d", year, m);
            String label = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            BigDecimal revenue = revenueMap.getOrDefault(key, BigDecimal.ZERO);
            points.add(VendorRevenuePoint.builder().label(label).revenue(revenue).build());
        }

        return VendorRevenueSeriesResponse.builder().range("year").points(points).build();
    }

    // ─── Workshop status ────────────────────────────────────────────

    private List<VendorWorkshopStatusItem> buildWorkshopStatus(UUID vendorId) {
        List<VendorWorkshopStatusItem> items = new ArrayList<>();
        for (WorkshopStatus status : WorkshopStatus.values()) {
            long count = workshopTemplateRepository.countByVendorIdAndStatusAndDeletedAtIsNull(vendorId, status);
            String displayName = switch (status) {
                case ACTIVE -> "Active";
                case PENDING -> "Pending";
                case DRAFT -> "Draft";
                case REJECTED -> "Rejected";
            };
            items.add(VendorWorkshopStatusItem.builder().name(displayName).value(count).build());
        }
        return items;
    }

    // ─── Recent transactions ────────────────────────────────────────

    private List<VendorTransactionItem> buildTransactions(UUID vendorId, int limit) {
        List<Object[]> rawTransactions = transactionRepository.findRecentTransactionsWithTicketsByVendorIdNative(vendorId, PageRequest.of(0, limit));

        return rawTransactions.stream().map(row -> {
            String transactionId = row[0] != null ? (row[0] instanceof byte[] ? asUuid((byte[]) row[0]).toString() : row[0].toString()) : null;
            String status = row[1] != null ? row[1].toString() : "UNKNOWN";
            String workshopName = row[2] != null ? row[2].toString() : "N/A";
            String customerName = row[3] != null ? row[3].toString() : "N/A";
            BigDecimal amount = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;
            String currency = row[5] != null ? row[5].toString() : "VND";

            LocalDateTime paidAt = null;
            if (row[6] != null) {
                if (row[6] instanceof java.sql.Timestamp) {
                    paidAt = ((java.sql.Timestamp) row[6]).toLocalDateTime();
                } else if (row[6] instanceof LocalDateTime) {
                    paidAt = (LocalDateTime) row[6];
                } else {
                    paidAt = LocalDateTime.parse(row[6].toString().replace(" ", "T"));
                }
            }

            String ticketCodes = row[7] != null ? row[7].toString() : null;

            String statusLabel = switch (status) {
                case "SUCCESS" -> "completed";
                case "PENDING" -> "pending";
                case "REFUNDED" -> "refunded";
                case "FAILED" -> "failed";
                default -> "unknown";
            };

            return VendorTransactionItem.builder()
                    .id(transactionId)
                    .workshopName(workshopName)
                    .customerName(customerName)
                    .amount(amount)
                    .currency(currency)
                    .paidAt(paidAt)
                    .status(statusLabel)
                    .ticketCodes(ticketCodes)
                    .build();
        }).collect(Collectors.toList());
    }

    private UUID asUuid(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            return null;
        }
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }

    // ─── Workshop reviews ───────────────────────────────────────────

    private List<VendorWorkshopReviewItem> buildWorkshopReviews(UUID vendorId, LocalDateTime now, int limit) {
        LocalDateTime weekAgo = now.minusWeeks(1);
        List<Object[]> raw = reviewRepository.findWorkshopReviewSummariesByVendorId(vendorId, weekAgo);

        return raw.stream()
                .limit(limit)
                .map(row -> VendorWorkshopReviewItem.builder()
                        .workshopId((UUID) row[0])
                        .workshopName((String) row[1])
                        .totalReviews(((Number) row[2]).longValue())
                        .averageRating(BigDecimal.valueOf(((Number) row[3]).doubleValue()).setScale(1, RoundingMode.HALF_UP))
                        .newReviewsInWindow(((Number) row[4]).longValue())
                        .build()
                ).collect(Collectors.toList());
    }

    // ─── Sessions (calendar) ────────────────────────────────────────

    private VendorSessionsResponse buildSessions(UUID vendorId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.atStartOfDay();

        List<WorkshopSession> sessions = workshopSessionRepository
                .findByVendorIdAndStartTimeBetween(vendorId, from, to);

        Map<LocalDate, List<VendorSessionItem>> byDate = new LinkedHashMap<>();

        for (WorkshopSession ws : sessions) {
            LocalDate day = ws.getStartTime().toLocalDate();
            int remaining = (ws.getMaxParticipants() != null ? ws.getMaxParticipants() : 0)
                    - (ws.getCurrentEnrolled() != null ? ws.getCurrentEnrolled() : 0);

            VendorSessionItem item = VendorSessionItem.builder()
                    .workshopId(ws.getWorkshopTemplate().getId())
                    .workshopName(ws.getWorkshopTemplate().getName())
                    .sessionId(ws.getId())
                    .startAt(ws.getStartTime())
                    .endAt(ws.getEndTime())
                    .remainingSlots(Math.max(remaining, 0))
                    .build();

            byDate.computeIfAbsent(day, k -> new ArrayList<>()).add(item);
        }

        List<LocalDate> highlightDates = new ArrayList<>(byDate.keySet());
        Collections.sort(highlightDates);

        return VendorSessionsResponse.builder()
                .highlightDates(highlightDates)
                .byDate(byDate)
                .build();
    }

    // ─── Helpers ────────────────────────────────────────────────────

    private BigDecimal safeDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
