package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Container;
import com.nebula.nebulaCloud.model.Engine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing container entities.
 */
@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {

    // TODO: Find all containers by status
    List<Container> findByStatus(String status);

    // TODO: Find container by associated engine
    Optional<Container> findByEngine(Engine engine);

}
