package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_checkins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime checkinTime;

    private String checkinMethod;

    @Column(columnDefinition = "TEXT")
    private String note;

    private Integer earnedPoints;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkin_id", nullable = false)
    private CheckinPoint checkinPoint;

    @OneToMany(mappedBy = "userCheckIn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CheckinImage> checkinImages;
}
