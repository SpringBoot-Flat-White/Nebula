package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Container;
import com.nebula.nebulaCloud.model.Engine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {


    List<Container> findByStatus(String status);

    Optional<Container> findByEngine(Engine engine);

}
