package fpt.project.NeoNHS.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base entity class containing common audit fields for all main entities.
 * Provides soft delete support via deletedAt and deletedBy fields.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Timestamp when the entity was soft deleted.
     * NULL means the entity is active, non-null means it has been deleted.
     */
    private LocalDateTime deletedAt;

    /**
     * UUID of the user who performed the soft delete.
     */
    private UUID deletedBy;
}
