package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a subscription plan available to users.
 *
 * Each plan defines the number of instances a user can create,
 * its price, and whether it is a free or paid plan.
 */
@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    /**
     * Unique identifier for the plan (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the plan. Must be unique (e.g., "Free", "Pro", "Enterprise").
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Maximum number of instances a user is allowed to create under this plan.
     */
    @Column(name = "max_instances", nullable = false)
    private Integer maxInstances;

    /**
     * Price of the plan.
     *
     * precision = 10 → allows up to 10 total digits.
     * scale = 2 → reserves 2 digits for decimals.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Indicates whether the plan is free.
     */
    @Column(name = "is_free")
    private Boolean isFree;
}
