package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an individual user profile linked to a base User entity.
 *
 * Stores personal details specific to individual accounts.
 */
@Entity
@Table(name = "individuals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Individual {

    /**
     * Unique identifier for the individual (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Full name of the individual.
     */
    @Column(name = "full_name", length = 120)
    private String fullName;

    /**
     * Associated user account.
     * Defines a one-to-one relationship with the User entity.
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
