package fpt.project.NeoNHS.entity;

import fpt.project.NeoNHS.enums.WorkshopStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workshop_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkshopTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String fullDescription;

    private Integer estimatedDuration;

    @Column(precision = 12, scale = 2)
    private BigDecimal defaultPrice;

    private Integer minParticipants;

    private Integer maxParticipants;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkshopStatus status = WorkshopStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectReason;

    private UUID approvedBy;

    private LocalDateTime approvedAt;

    @Builder.Default
    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Builder.Default
    private Integer totalReview = 0;

    private UUID transactionId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private VendorProfile vendor;

    @OneToMany(mappedBy = "workshopTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkshopSession> workshopSessions;

    @OneToMany(mappedBy = "workshopTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkshopImage> workshopImages;

    @OneToMany(mappedBy = "workshopTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkshopTag> workshopTags;

    @OneToMany(mappedBy = "workshopTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
}
