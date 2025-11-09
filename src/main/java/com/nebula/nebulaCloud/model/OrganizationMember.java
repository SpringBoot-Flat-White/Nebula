package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents the membership relationship between a user and an organization.
 *
 * Each record links a user to an organization. The unique constraint ensures
 * that a user cannot belong to the same organization more than once.
 */
@Entity
@Table(name = "organization_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"org_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationMember {

    /**
     * Unique identifier for the membership record (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The organization to which the user belongs.
     */
    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    /**
     * The user who is a member of the organization.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
