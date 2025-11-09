package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents a user account in the system.
 * This entity is mapped to the 'users' table and is used by Spring Security for authentication.
 * It implements UserDetails to integrate with Spring Security's framework.
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

//    /**
//     * Defines a many-to-one relationship with the Plan entity.
//     * A user can have one subscribed plan, but a plan can have many users.
//     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "plan_id")
//    private Plan plan;

    /**
     * Defines the inverse side of the one-to-one relationship with Individual.
     * 'mappedBy = "user"' tells Hibernate that the relationship is managed by the 'user' field
     * in the Individual entity. This is crucial for a correct bidirectional mapping.
     * CascadeType.ALL means operations (save, delete) on a User will propagate to the associated Individual.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Individual individual;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Organization organization;


    // --- Métodos de UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Puedes implementar roles aquí si los necesitas. Por ahora, devolvemos una lista vacía.
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        // Usamos el email como el nombre de usuario para Spring Security.
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }



    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
