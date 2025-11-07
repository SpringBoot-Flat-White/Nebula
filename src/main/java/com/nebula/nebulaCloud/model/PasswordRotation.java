package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a record of password rotation for a specific user database.
 *
 * Each entry logs the old and new encrypted passwords,
 * allowing traceability and security auditing for password updates.
 */
@Entity
@Table(name = "password_rotations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordRotation {

    /**
     * Unique identifier for the password rotation record (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user database whose password was rotated.
     */
    @ManyToOne
    @JoinColumn(name = "user_db_id", nullable = false)
    private UserDb userDb;

    /**
     * The old encrypted password before rotation.
     */
    @Column(name = "old_password_enc", length = 255)
    private String oldPasswordEnc;

    /**
     * The new encrypted password after rotation.
     */
    @Column(name = "new_password_enc", length = 255)
    private String newPasswordEnc;

    /**
     * Timestamp indicating when the password was rotated.
     */
    @Column(name = "rotated_at")
    private LocalDateTime rotatedAt;
}
