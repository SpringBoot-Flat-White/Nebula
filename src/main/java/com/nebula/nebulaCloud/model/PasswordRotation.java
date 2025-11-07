package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_rotations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordRotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_db_id", nullable = false)
    private UserDb userDb;

    @Column(name = "old_password_enc", length = 255)
    private String oldPasswordEnc;

    @Column(name = "new_password_enc", length = 255)
    private String newPasswordEnc;

    @Column(name = "rotated_at")
    private LocalDateTime rotatedAt;
}

