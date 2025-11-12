package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a database instance created for a specific user.
 *
 * Each instance is linked to a container (database engine),
 * a user database (credentials), and the user who owns it.
 */
@Entity
@Table(name = "instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Instance {

    /**
     * Defines the possible lifecycle states of an instance.
     */
    public enum Status {
        CREATING, RUNNING, SUSPENDED, DELETED, ERROR
    }

    /**
     * Unique identifier for the instance (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * User-defined name for the instance.
     */
    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * The container where this instance is deployed.
     * Each instance belongs to exactly one container.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    @ToString.Exclude
    private Container container;

    /**
     * Database credentials associated with this instance.
     * Each instance has exactly one set of database credentials.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_db_id", nullable = false)
    @ToString.Exclude
    private UserDb userDb;

    /**
     * The user who owns this instance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    /**
     * Current operational status of the instance.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /**
     * Name of the database created in the instance.
     */
    @Column(name = "database_name", length = 100)
    private String databaseName;

    /**
     * Timestamp when the instance was created.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the instance was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = Status.CREATING;
        }
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
