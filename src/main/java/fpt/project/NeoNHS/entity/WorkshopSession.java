package fpt.project.NeoNHS.entity;

import fpt.project.NeoNHS.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workshop_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkshopSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    private Integer maxParticipants;

    @Builder.Default
    private Integer currentEnrolled = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id", nullable = false)
    private WorkshopTemplate workshopTemplate;

    @OneToMany(mappedBy = "workshopSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    @OneToMany(mappedBy = "workshopSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "workshopSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems;
}
