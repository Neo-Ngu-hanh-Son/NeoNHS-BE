package fpt.project.NeoNHS.entity;

import fpt.project.NeoNHS.enums.TicketStatus;
import fpt.project.NeoNHS.enums.TicketType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String qrCode;

    @Column(unique = true)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime issueDate;

    private LocalDateTime expiryDate;

    private LocalDateTime redeemedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_catalog_id")
    private TicketCatalog ticketCatalog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_session_id")
    private WorkshopSession workshopSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id")
    private OrderDetail orderDetail;
}
