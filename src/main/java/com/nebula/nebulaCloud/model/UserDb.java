package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_dbs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "db_user", nullable = false, length = 100)
    private String dbUser;

    @Column(name = "db_password_enc", nullable = false, length = 255)
    private String dbPasswordEnc;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
