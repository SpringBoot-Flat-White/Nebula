package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents an application or service instance deployed within a container.
 *
 * Each instance is linked to a specific container, database, and user.
 * It stores metadata such as creation and update timestamps.
 */
@Entity
@Table(name = "instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instance {

    /**
     * Unique identifier for the instance (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable name for the instance.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Container where this instance is running.
     * Defines a many-to-one relationship since a container
     * can host multiple instances.
     */
    @ManyToOne
    @JoinColumn(name = "container_id", nullable = false)
    private Container container;

    /**
     * Database linked to this instance.
     * Multiple instances can share the same user database.
     */
    @ManyToOne
    @JoinColumn(name = "user_db_id", nullable = false)
    private UserDb userDb;

    /**
     * Owner of the instance.
     * Each instance belongs to a user.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Timestamp when the instance was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp when the instance was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
