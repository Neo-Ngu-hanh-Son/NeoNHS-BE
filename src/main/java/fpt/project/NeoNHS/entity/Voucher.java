package fpt.project.NeoNHS.entity;

import fpt.project.NeoNHS.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Voucher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ===== Voucher Type & Scope =====

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType voucherType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherScope scope;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicableProduct applicableProduct = ApplicableProduct.ALL;

    // ===== Discount fields (for DISCOUNT type) =====

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxDiscountValue;

    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderValue;

    // ===== Gift fields (for GIFT_PRODUCT type) =====

    private String giftDescription;

    private String giftImageUrl;

    // ===== Bonus points fields (for BONUS_POINTS type) =====

    private Integer bonusPointsValue;

    // ===== Time & Usage =====

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer usageLimit;

    @Builder.Default
    private Integer usageCount = 0;

    private Integer maxUsagePerUser;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherStatus status = VoucherStatus.ACTIVE;

    // ===== Relationships =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private VendorProfile vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_ticket_catalog_id")
    private TicketCatalog freeTicketCatalog;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserVoucher> userVouchers;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
}
