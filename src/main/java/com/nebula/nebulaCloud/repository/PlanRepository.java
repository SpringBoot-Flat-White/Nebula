package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de persistencia con la entidad Plan.
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    /**
     * Busca un plan por su nombre.
     */
    Optional<Plan> findByName(String name);
}
