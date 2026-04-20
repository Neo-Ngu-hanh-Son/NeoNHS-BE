package fpt.project.NeoNHS.entity;

import fpt.project.NeoNHS.enums.PanoramaHotSpotType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "panorama_hot_spots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanoramaHotSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "yaw", nullable = false)
    private Double yaw;

    @Column(name = "pitch", nullable = false)
    private Double pitch;

    @Column(name = "tooltip", nullable = false, length = 100)
    private String tooltip;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "type", nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PanoramaHotSpotType type = PanoramaHotSpotType.INFO;

    // If type is "LINK", this tells us where to go
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_panorama_id")
    private PointPanorama targetPanorama;

    // This is the panorama that this hot spot belongs to
    @ManyToOne
    @JoinColumn(name = "point_panorama_id")
    private PointPanorama pointPanorama;

//    // A hot spot belongs to EITHER a Point OR a CheckinPoint (one must be set)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "point_id")
//    private Point point;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "checkin_point_id")
//    private CheckinPoint checkinPoint;

}
