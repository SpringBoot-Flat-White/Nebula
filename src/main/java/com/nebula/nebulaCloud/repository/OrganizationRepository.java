package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
