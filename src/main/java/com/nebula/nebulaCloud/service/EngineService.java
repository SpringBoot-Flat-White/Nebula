package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.exception.DuplicateResourceException;
import com.nebula.nebulaCloud.exception.ResourceNotFoundException;
import com.nebula.nebulaCloud.model.Engine;
import com.nebula.nebulaCloud.repository.EngineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing database engines.
 */
@Service
@RequiredArgsConstructor
public class EngineService {

    private final EngineRepository engineRepository;
    private final DockerContainerService dockerContainerService;

    // TODO: Get all engines
    public ResponseEntity<List<Engine>> getAll() {
        return ResponseEntity.ok(engineRepository.findAll());
    }

    // TODO: Get engine by ID
    public ResponseEntity<Engine> getById(Long id) {
        return engineRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // TODO: Create a new engine
    public ResponseEntity<Engine> create(String request) {
        engineRepository.findByName(request.toUpperCase())
                .ifPresent(e -> {
                    throw new DuplicateResourceException("Engine '" + request + "' already exists");
                });

        // Crear contenedor físico del motor si no existe
        /*dockerContainerService.createContainer(
                request.getContainerName(),
                request.getImage(),
                request.getPort()
        );*/

        Engine engine = Engine.builder()
                .name(request)
                .build();

        Engine engineR = engineRepository.save(engine);
        return ResponseEntity.status(HttpStatus.CREATED).body(engineR);
    }

    // TODO: Update engine configuration
    /*
    public ResponseEntity<Engine> update(Long id, Engine update) {
        Engine engine = engineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Engine not found"));

        engine.setHost(update.getHost());
        engine.setPort(update.getPort());
        engine.setStatus(update.getStatus());
        engine.setDescription(update.getDescription());
        engineRepository.save(engine);

        return ResponseEntity.ok(engine);
    }*/

    // TODO: Delete an engine by ID
    public ResponseEntity<Void> delete(Long id) {
        Engine engine = engineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Engine not found with ID: " + id));

        /*
        // Detener y eliminar contenedor Docker físico
        dockerContainerService.removeContainer(engine.getContainerName());*/

        engineRepository.delete(engine);
        return ResponseEntity.noContent().build();
    }
}
