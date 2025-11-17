package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing organization entities.
 */
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    // TODO: Add custom query methods if needed
}
