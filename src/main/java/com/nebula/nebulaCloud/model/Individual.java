package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an individual user's profile information.
 * This entity is mapped to the 'individuals' table and holds the user's full name.
 * It has a one-to-one relationship with the User entity.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "individuals")
public class Individual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", length = 120)
    private String fullName;

    /**
     * This defines the owning side of the one-to-one relationship with the User.
     * The 'user_id' column in this table will hold the foreign key.
     * fetch = FetchType.LAZY means the User object is loaded only when accessed.
     * optional = false ensures that an Individual must be associated with a User.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
