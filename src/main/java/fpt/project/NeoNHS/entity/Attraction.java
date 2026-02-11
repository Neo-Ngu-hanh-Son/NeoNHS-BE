package fpt.project.NeoNHS.entity;

import fpt.project.NeoNHS.enums.AttractionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attractions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Attraction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String mapImageUrl;

    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AttractionStatus status;

    private String thumbnailUrl;

    private LocalTime openHour;

    private LocalTime closeHour;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    // Relationships
    @OneToMany(mappedBy = "attraction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Point> points;

    @OneToMany(mappedBy = "attraction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketCatalog> ticketCatalogs;
}

