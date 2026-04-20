package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "point_panoramas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PointPanorama extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(name = "panorama_image_url", length = 2048)
    private String panoramaImageUrl;

    @Column(name = "default_yaw")
    @Builder.Default
    private Double defaultYaw = 0.0;

    @Column(name = "default_pitch")
    @Builder.Default
    private Double defaultPitch = 0.0;

    // Is this panorama the first panorama that user see.
    @Builder.Default
    @Column(name = "is_default")
    private Boolean isDefault = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id")
    private Point point;

    @Builder.Default
    @OneToMany(
            mappedBy = "pointPanorama",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<PanoramaHotSpot> panoramaHotSpots = new ArrayList<>();
}
