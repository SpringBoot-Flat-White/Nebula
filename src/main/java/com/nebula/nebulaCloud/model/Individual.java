package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

/**
 */
@Entity
@Data
@Table(name = "individuals")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Individual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", length = 120)
    private String fullName;

    /**
     */
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Organization organization;
}
