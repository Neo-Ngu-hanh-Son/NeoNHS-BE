package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import fpt.project.NeoNHS.enums.PointType;

import java.time.LocalDateTime;

@Entity
@Table(name = "points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String history;

    private String historyAudioUrl;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private Integer orderIndex;

    private Integer estTimeSpent;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType type = PointType.DEFAULT;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;

    @OneToMany(mappedBy = "point", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CheckinPoint> checkinPoints;

    @OneToMany(mappedBy = "point", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserVisitedPoint> userVisitedPoints;
}
