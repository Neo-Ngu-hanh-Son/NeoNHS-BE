package fpt.project.NeoNHS.entity;

import fpt.project.NeoNHS.enums.TicketCatalogStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ticket_catalogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TicketCatalog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String customerType;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal originalPrice;

    private String applyOnDays;

    private LocalDateTime validFromDate;

    private LocalDateTime validToDate;

    private Integer totalQuota;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCatalogStatus status = TicketCatalogStatus.ACTIVE;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id")
    private Attraction attraction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @OneToMany(mappedBy = "ticketCatalog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    @OneToMany(mappedBy = "ticketCatalog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "ticketCatalog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems;
}
