package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a container instance managed by the platform.
 *
 * Each container is linked to an Engine and stores connection
 * details such as IP, port, and its current lifecycle status.
 */
@Entity
@Table(name = "containers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Container {

    /**
     * Defines the possible lifecycle states of a container.
     */
    public enum Status {
        CREATING, RUNNING, SUSPENDED, DELETED
    }

    /**
     * Unique identifier for the container (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * IP address assigned to the container instance.
     */
    @Column(nullable = false, length = 100)
    private String ip;

    /**
     * Port exposed by the container for external connections.
     */
    @Column(nullable = false)
    private Integer port;

    /**
     * The engine that owns or manages this container.
     * Many containers can belong to a single engine.
     */
    @ManyToOne
    @JoinColumn(name = "engine_id", nullable = false)
    private Engine engine;

    /**
     * Current operational status of the container.
     */
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * Timestamp of when the container was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
