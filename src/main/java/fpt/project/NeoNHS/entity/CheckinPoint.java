package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "checkin_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CheckinPoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String position;

    private String thumbnailUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(unique = true)
    private String qrCode;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    private Integer rewardPoints;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id", nullable = false)
    private Point point;

    @OneToMany(mappedBy = "checkinPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCheckIn> userCheckIns;

    // ─── Panorama fields ───

    @Column(name = "panorama_image_url", length = 2048)
    private String panoramaImageUrl;

    @Column(name = "default_yaw")
    @Builder.Default
    private Double defaultYaw = 0.0;

    @Column(name = "default_pitch")
    @Builder.Default
    private Double defaultPitch = 0.0;

    @Builder.Default
    @OneToMany(mappedBy = "checkinPoint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PanoramaHotSpot> panoramaHotSpots = new ArrayList<>();

}
