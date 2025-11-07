package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "individuals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Individual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

