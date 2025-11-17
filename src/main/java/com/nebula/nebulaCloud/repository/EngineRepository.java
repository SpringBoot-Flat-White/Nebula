package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Engine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for managing engine entities.
 */
public interface EngineRepository extends JpaRepository<Engine, Long> {
    // TODO: Find engine by name
    Optional<Engine> findByName(String name);
}
