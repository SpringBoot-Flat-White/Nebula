package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents an organization entity in the system.
 *
 * An organization groups multiple users under a single entity,
 * and is owned by an individual user.
 */
@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    /**
     * Unique identifier for the organization (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the organization.
     */
    @Column(nullable = false, length = 120)
    private String name;

    /**
     * The owner of the organization.
     * Mapped to an individual user.
     */
    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Individual owner;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false )
    private  User user;

    /**
     * Timestamp when the organization was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
