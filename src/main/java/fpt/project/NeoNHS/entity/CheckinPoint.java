package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
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
}
