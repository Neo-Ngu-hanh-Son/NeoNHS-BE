package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private Integer orderIndex;

    private Integer estTimeSpent;

    @Column(columnDefinition = "TEXT")
    private String historyText;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType type = PointType.DEFAULT;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "panorama_image_url", length = 2048)
    private String panoramaImageUrl;

    @Column(name = "default_yaw")
    @Builder.Default
    private Double defaultYaw = 0.0;

    @Column(name = "default_pitch")
    @Builder.Default
    private Double defaultPitch = 0.0;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;

    @OneToMany(mappedBy = "point", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CheckinPoint> checkinPoints;

    @OneToMany(mappedBy = "point", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserVisitedPoint> userVisitedPoints;

    @Builder.Default
    @OneToMany(mappedBy = "point", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PanoramaHotSpot> panoramaHotSpots = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "point",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<PointHistoryAudio> historyAudios = new ArrayList<>();

}
