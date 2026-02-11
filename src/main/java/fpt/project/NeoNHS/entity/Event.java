package fpt.project.NeoNHS.entity;

import fpt.project.NeoNHS.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String fullDescription;

    private String locationName;

    private String latitude;

    private String longitude;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isTicketRequired = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    private Integer maxParticipants;

    @Builder.Default
    private Integer currentEnrolled = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.UPCOMING;

    // Relationships
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventTag> eventTags;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketCatalog> ticketCatalogs;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
}
