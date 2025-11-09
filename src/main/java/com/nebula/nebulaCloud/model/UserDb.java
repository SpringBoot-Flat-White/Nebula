package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a database user associated with an application user.
 *
 * Each record stores credentials (encrypted) for a database account
 * that belongs to a specific user, ensuring isolation between users'
 * databases inside their containers.
 */
@Entity
@Table(name = "user_dbs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDb {

    /**
     * Unique identifier for the user database (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username of the database user associated with the application user.
     */
    @Column(name = "db_user", nullable = false, length = 100)
    private String dbUser;

    /**
     * Encrypted password for the database user.
     */
    @Column(name = "db_password_enc", nullable = false, length = 255)
    private String dbPasswordEnc;

    /**
     * The application user who owns this database user.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
