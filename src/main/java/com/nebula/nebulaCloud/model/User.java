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
import java.util.Objects;

/**
 * Represents a user entity in the database.
 *
 * This class serves a dual purpose:
 * 1. As a JPA entity to map the 'users' table from the database.
 * 2. As an implementation of Spring Security's UserDetails interface,
 *    making it the core user representation for authentication and authorization.
 *
 * Best Practice: Avoid using Lombok's @Data annotation on JPA entities
 * as it can cause issues with performance and entity lifecycle due to its
 * generation of `equals()`, `hashCode()`, and `toString()` methods.
 * It's better to be explicit with the needed annotations.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    /**
     * The unique identifier for the user. Generated automatically.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user's email address. It must be unique and is used as the
     * username for Spring Security authentication.
     */
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /**
     * The user's hashed password. The field name in the database is 'password_hash'.
     */
    @Column(name = "password_hash", nullable = false)
    private String password;

    /**
     * The type of the user account (e.g., INDIVIDUAL, ORGANIZATION).
     * Stored as a string in the database for clarity.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;

    /**
     * The timestamp when the user account was created. Managed automatically by JPA.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp when the user account was last updated. Managed automatically by JPA.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * The ID of the plan associated with the user.
     * Note: This is mapped as a simple Long. If a 'Plan' entity is created,
     * this should be replaced with a @ManyToOne relationship.
     */
    @Column(name = "plan_id")
    private Long planId;


    // --- UserDetails Interface Implementation ---

    /**
     * Returns the authorities granted to the user. For this basic setup, we are not
     * managing roles, so it returns an empty collection.
     *
     * @return A collection of granted authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // In a real application, you would map user roles to SimpleGrantedAuthority objects here.
        // e.g., return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return Collections.emptyList();
    }

    /**
     * Returns the username used to authenticate the user. In this application,
     * the email is used as the username.
     *
     * @return The user's email.
     */
    @Override
    public String getUsername() {
        return this.email;
    }

    /**
     * Indicates whether the user's account has expired. An expired account cannot be
     * authenticated. For this implementation, accounts never expire.
     *
     * @return true always.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be
     * authenticated. For this implementation, accounts are never locked.
     *
     * @return true always.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired. Expired
     * credentials prevent authentication. For this implementation, credentials never expire.
     *
     * @return true always.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be
     * authenticated. For this implementation, users are always enabled.
     *
     * @return true always.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- Safe equals() and hashCode() Implementation ---

    /**
     * Professional practice for JPA entities: `equals()` and `hashCode()` should be
     * based on the business key or the primary key, and should be consistent across
     * all entity lifecycle states (transient, managed, detached).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        // Use a constant value for transient entities, or the class hashcode.
        // This avoids issues when an entity is added to a Set before it's persisted.
        return getClass().hashCode();
    }
}
