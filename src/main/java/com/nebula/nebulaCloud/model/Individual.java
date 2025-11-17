package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an individual user profile.
 */
@Entity
@Data
@Table(name = "individuals")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Individual {

    // TODO: Individual unique ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Individual's full name
    @Column(name = "full_name", length = 120)
    private String fullName;

    // TODO: Associated user account
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // TODO: Organization owned by this individual
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Organization organization;
}
