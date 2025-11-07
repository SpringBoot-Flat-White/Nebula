package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "max_instances", nullable = false)
    private Integer maxInstances;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_free")
    private Boolean isFree;
}
