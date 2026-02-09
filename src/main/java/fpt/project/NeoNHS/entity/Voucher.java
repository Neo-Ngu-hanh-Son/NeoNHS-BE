package fpt.project.NeoNHS.entity;

import fpt.project.NeoNHS.enums.DiscountType;
import fpt.project.NeoNHS.enums.VoucherStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal discountValue;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxDiscountValue;

    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderValue;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer usageLimit;

    @Builder.Default
    private Integer usageCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherStatus status = VoucherStatus.ACTIVE;

    // Relationships
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserVoucher> userVouchers;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
}
