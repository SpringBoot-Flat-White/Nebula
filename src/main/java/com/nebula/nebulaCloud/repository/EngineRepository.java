package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Engine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EngineRepository extends JpaRepository<Engine, Long> {
    Optional<Engine> findByName(String name);
}
