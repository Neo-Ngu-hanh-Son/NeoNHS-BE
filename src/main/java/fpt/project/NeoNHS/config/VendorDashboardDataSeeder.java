package fpt.project.NeoNHS.config;

import fpt.project.NeoNHS.entity.*;
import fpt.project.NeoNHS.enums.*;
import fpt.project.NeoNHS.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds fake data so the vendor dashboard endpoints have something to display.
 * Runs AFTER DataInitializer (order = 2) and is guarded by an idempotency check
 * on workshop_templates count for the test vendor.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class VendorDashboardDataSeeder implements CommandLineRunner {

    private final VendorProfileRepository vendorProfileRepository;
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Random RNG = new Random(42);

    @Override
    public void run(String... args) {
        var vendorOpt = vendorProfileRepository.findByUserEmail("vendor@neonhs.com");
        if (vendorOpt.isEmpty()) return;

        VendorProfile vendor = vendorOpt.get();
        if (workshopTemplateRepository.countByVendorIdAndDeletedAtIsNull(vendor.getId()) > 0) return;

        log.info("Seeding vendor dashboard demo data...");

        List<User> tourists = seedTouristUsers();
        List<WorkshopTemplate> templates = seedWorkshopTemplates(vendor);
        List<WorkshopSession> sessions = seedWorkshopSessions(templates);
        seedOrdersAndTransactions(vendor, tourists, sessions);
        seedReviews(tourists, templates);
        seedVouchers(vendor);

        log.info("Vendor dashboard demo data seeded successfully.");
    }

    // ─── Tourist users ──────────────────────────────────────────────

    private List<User> seedTouristUsers() {
        String[][] people = {
                {"Nguyen Van An", "an.nguyen@example.com"},
                {"Tran Thi Bich", "bich.tran@example.com"},
                {"Le Hoang Cuong", "cuong.le@example.com"},
                {"Pham Minh Duc", "duc.pham@example.com"},
                {"Vo Thi Em", "em.vo@example.com"},
                {"Hoang Quoc Fai", "fai.hoang@example.com"},
                {"Dang Thi Giang", "giang.dang@example.com"},
                {"Bui Thanh Hai", "hai.bui@example.com"},
        };

        List<User> tourists = new ArrayList<>();
        for (String[] p : people) {
            if (userRepository.findByEmail(p[1]).isPresent()) {
                tourists.add(userRepository.findByEmail(p[1]).get());
                continue;
            }
            User u = User.builder()
                    .fullname(p[0])
                    .email(p[1])
                    .passwordHash(passwordEncoder.encode("tourist123"))
                    .role(UserRole.TOURIST)
                    .isActive(true).isVerified(true).isBanned(false)
                    .build();
            tourists.add(userRepository.save(u));
        }
        return tourists;
    }

    // ─── Workshop templates (mixed statuses) ────────────────────────

    private List<WorkshopTemplate> seedWorkshopTemplates(VendorProfile vendor) {
        Object[][] data = {
                {"Pottery Making", "Learn traditional Vietnamese pottery techniques", WorkshopStatus.ACTIVE, 120, 150000},
                {"Marble Sculpting", "Hands-on marble sculpting with local artisans", WorkshopStatus.ACTIVE, 180, 250000},
                {"Lantern Crafting", "Create your own Hoi An-style lantern", WorkshopStatus.ACTIVE, 90, 120000},
                {"Incense Making", "Traditional incense crafting workshop", WorkshopStatus.ACTIVE, 60, 80000},
                {"Silk Painting", "Vietnamese silk painting masterclass", WorkshopStatus.ACTIVE, 150, 200000},
                {"Bamboo Weaving", "Learn the art of bamboo basket weaving", WorkshopStatus.PENDING, 90, 100000},
                {"Conical Hat Making", "Craft your own nón lá", WorkshopStatus.PENDING, 75, 90000},
                {"Woodblock Printing", "Traditional woodblock printing on fabric", WorkshopStatus.DRAFT, 120, 180000},
                {"Ceramic Glazing", "Explore ceramic glazing techniques", WorkshopStatus.DRAFT, 100, 160000},
                {"Stone Carving Intro", "Beginner stone carving experience", WorkshopStatus.REJECTED, 60, 70000},
        };

        List<WorkshopTemplate> templates = new ArrayList<>();
        for (Object[] d : data) {
            WorkshopTemplate wt = WorkshopTemplate.builder()
                    .name((String) d[0])
                    .shortDescription((String) d[1])
                    .fullDescription((String) d[1] + ". Join us for an unforgettable cultural experience at Ngu Hanh Son.")
                    .status((WorkshopStatus) d[2])
                    .estimatedDuration((int) d[3])
                    .defaultPrice(BigDecimal.valueOf((int) d[4]))
                    .minParticipants(2)
                    .maxParticipants(15)
                    .isPublished(d[2] == WorkshopStatus.ACTIVE)
                    .vendor(vendor)
                    .build();
            templates.add(workshopTemplateRepository.save(wt));
        }
        return templates;
    }

    // ─── Workshop sessions (calendar data) ──────────────────────────

    private List<WorkshopSession> seedWorkshopSessions(List<WorkshopTemplate> templates) {
        List<WorkshopSession> allSessions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        List<WorkshopTemplate> activeTemplates = templates.stream()
                .filter(t -> t.getStatus() == WorkshopStatus.ACTIVE)
                .toList();

        for (WorkshopTemplate wt : activeTemplates) {
            int duration = wt.getEstimatedDuration() != null ? wt.getEstimatedDuration() : 120;

            // Past sessions (for completed orders / revenue)
            for (int daysAgo = 1; daysAgo <= 12; daysAgo += 2) {
                LocalDateTime start = now.minusDays(daysAgo).withHour(9).withMinute(0).withSecond(0).withNano(0);
                WorkshopSession ws = WorkshopSession.builder()
                        .workshopTemplate(wt)
                        .startTime(start)
                        .endTime(start.plusMinutes(duration))
                        .price(wt.getDefaultPrice())
                        .maxParticipants(15)
                        .currentEnrolled(RNG.nextInt(8) + 3)
                        .status(SessionStatus.COMPLETED)
                        .build();
                allSessions.add(workshopSessionRepository.save(ws));
            }

            // Upcoming sessions (for calendar display)
            for (int daysAhead = 1; daysAhead <= 14; daysAhead += 3) {
                int hour = 9 + (daysAhead % 3) * 3;
                LocalDateTime start = now.plusDays(daysAhead).withHour(hour).withMinute(0).withSecond(0).withNano(0);
                WorkshopSession ws = WorkshopSession.builder()
                        .workshopTemplate(wt)
                        .startTime(start)
                        .endTime(start.plusMinutes(duration))
                        .price(wt.getDefaultPrice())
                        .maxParticipants(15)
                        .currentEnrolled(RNG.nextInt(6))
                        .status(SessionStatus.SCHEDULED)
                        .build();
                allSessions.add(workshopSessionRepository.save(ws));
            }
        }
        return allSessions;
    }

    // ─── Orders + Transactions (revenue data) ───────────────────────

    private void seedOrdersAndTransactions(VendorProfile vendor, List<User> tourists, List<WorkshopSession> sessions) {
        List<WorkshopSession> completedSessions = sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .toList();

        int txIndex = 0;
        for (WorkshopSession ws : completedSessions) {
            int enrolled = ws.getCurrentEnrolled() != null ? ws.getCurrentEnrolled() : 1;
            int bookings = Math.min(enrolled, 3);

            for (int b = 0; b < bookings; b++) {
                User tourist = tourists.get(txIndex % tourists.size());
                BigDecimal price = ws.getPrice() != null ? ws.getPrice() : new BigDecimal("150000");
                int qty = RNG.nextInt(2) + 1;
                BigDecimal total = price.multiply(BigDecimal.valueOf(qty));

                fpt.project.NeoNHS.entity.Order order = fpt.project.NeoNHS.entity.Order.builder()
                        .user(tourist)
                        .totalAmount(total)
                        .finalAmount(total)
                        .build();
                fpt.project.NeoNHS.entity.Order savedOrder = orderRepository.save(order);

                OrderDetail od = OrderDetail.builder()
                        .order(savedOrder)
                        .workshopSession(ws)
                        .quantity(qty)
                        .unitPrice(price)
                        .commissionAmount(total.multiply(new BigDecimal("0.10")))
                        .netAmount(total.multiply(new BigDecimal("0.90")))
                        .build();
                savedOrder.setOrderDetails(List.of(od));
                orderRepository.save(savedOrder);

                TransactionStatus txStatus = (txIndex % 10 == 9) ? TransactionStatus.REFUNDED : TransactionStatus.SUCCESS;
                LocalDateTime paidAt = ws.getStartTime().minusHours(RNG.nextInt(24) + 1);

                Transaction tx = Transaction.builder()
                        .order(savedOrder)
                        .amount(total)
                        .paymentGateway("PAYOS")
                        .description("Workshop booking: " + ws.getWorkshopTemplate().getName())
                        .transactionDate(paidAt)
                        .status(txStatus)
                        .build();
                transactionRepository.save(tx);

                txIndex++;
            }
        }
        log.info("  Seeded {} orders/transactions", txIndex);
    }

    // ─── Reviews ────────────────────────────────────────────────────

    private void seedReviews(List<User> tourists, List<WorkshopTemplate> templates) {
        List<WorkshopTemplate> activeTemplates = templates.stream()
                .filter(t -> t.getStatus() == WorkshopStatus.ACTIVE)
                .toList();

        String[] comments = {
                "Amazing experience! Highly recommend.",
                "Great instructor, learned a lot.",
                "Fun activity, perfect for families.",
                "A bit crowded but still enjoyable.",
                "Wonderful cultural experience!",
                "Would definitely come back again.",
                "Very professional and well organized.",
                "The materials were top quality.",
                "Best workshop I've ever attended!",
                "Nice atmosphere, friendly staff.",
        };

        int reviewCount = 0;
        for (WorkshopTemplate wt : activeTemplates) {
            int numReviews = RNG.nextInt(8) + 5;
            for (int i = 0; i < numReviews; i++) {
                User tourist = tourists.get((reviewCount) % tourists.size());
                int rating = 3 + RNG.nextInt(3);

                Review review = Review.builder()
                        .user(tourist)
//                        .workshopTemplate(wt)
                        .reviewTypeId(wt.getId())
                        .reviewTypeFlg(1)
                        .rating(rating)
                        .comment(comments[reviewCount % comments.length])
                        .status(ReviewStatus.VISIBLE)
                        .build();

                reviewRepository.save(review);

                reviewCount++;
            }
        }

        // Update averageRating on templates
        for (WorkshopTemplate wt : activeTemplates) {
            List<Review> reviews = reviewRepository.findAll().stream()
//                    .filter(r -> r.getWorkshopTemplate() != null && r.getWorkshopTemplate().getId().equals(wt.getId()))
                    .toList();
            //todo
            if (!reviews.isEmpty()) {
                double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
                wt.setAverageRating(BigDecimal.valueOf(avg).setScale(2, java.math.RoundingMode.HALF_UP));
                wt.setTotalRatings(reviews.size());
                workshopTemplateRepository.save(wt);
            }
        }

        log.info("  Seeded {} reviews", reviewCount);
    }

    // ─── Vouchers ───────────────────────────────────────────────────

    private void seedVouchers(VendorProfile vendor) {
        User vendorUser = vendor.getUser();
        LocalDateTime now = LocalDateTime.now();

        Object[][] data = {
                {"WELCOME10", "10% off your first workshop", DiscountType.PERCENT, 10, 50000},
                {"SUMMER20", "20% off summer workshops", DiscountType.PERCENT, 20, 100000},
                {"FLAT50K", "50,000 VND off any workshop", DiscountType.FIXED, 50000, 0},
                {"MARBLE15", "15% off marble sculpting", DiscountType.PERCENT, 15, 75000},
                {"CRAFT25", "25,000 VND off craft workshops", DiscountType.FIXED, 25000, 0},
        };

        int count = 0;
        for (Object[] d : data) {
            if (voucherRepository.existsByCode((String) d[0])) continue;

            Voucher v = Voucher.builder()
                    .code((String) d[0])
                    .description((String) d[1])
                    .voucherType(VoucherType.DISCOUNT)
                    .scope(VoucherScope.VENDOR)
                    .discountType((DiscountType) d[2])
                    .discountValue(BigDecimal.valueOf((int) d[3]))
                    .maxDiscountValue(BigDecimal.valueOf((int) d[4]))
                    .minOrderValue(BigDecimal.valueOf(50000))
                    .startDate(now.minusDays(30))
                    .endDate(now.plusDays(60))
                    .usageLimit(100)
                    .maxUsagePerUser(2)
                    .createdByUser(vendorUser)
                    .vendor(vendor)
                    .build();
            voucherRepository.save(v);
            count++;
        }
        log.info("  Seeded {} vouchers", count);
    }
}
