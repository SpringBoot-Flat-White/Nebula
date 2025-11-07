package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a computing engine or host where containers are deployed.
 *
 * Each engine acts as a node that can manage multiple container instances.
 */
@Entity
@Table(name = "engines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Engine {

    /**
     * Unique identifier for the engine (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Descriptive and unique name of the engine.
     * Used to identify the host or node in the system.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
