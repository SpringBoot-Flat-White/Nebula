package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.dto.ContainerRequest;
import com.nebula.nebulaCloud.dto.ContainerResponse;
import com.nebula.nebulaCloud.model.Container;
import com.nebula.nebulaCloud.model.Engine;
import com.nebula.nebulaCloud.repository.ContainerRepository;
import com.nebula.nebulaCloud.repository.EngineRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContainerService {

    private final ContainerRepository containerRepository;
    private final EngineRepository engineRepository;

    // =====================
    // CREATE
    // =====================
    @Transactional
    public ResponseEntity<ContainerResponse> create(ContainerRequest request) {

        Engine engine = engineRepository.findById(request.getEngineId()).orElseThrow(
                () -> new RuntimeException("Engine not found")
        );

        // Validar si ya existe el contenedor por nombre
        containerRepository.findByEngine(engine).ifPresent(c -> {
            throw new IllegalStateException("Container with name '" + engine + "' already exists.");
        });


        // Crear la entidad
        Container container = Container.builder()
                .ip(request.getIp())
                .port(request.getPort())
                .engine(engine)
                .createdAt(LocalDateTime.now())
                .build();

        containerRepository.save(container);

        // Construir respuesta
        ContainerResponse response = ContainerResponse.builder()
                .ip(container.getIp())
                .id(container.getId())
                .ports(container.getPort())
                .engine(engine.getName())
                .status(container.getStatus().name())
                .createAt(container.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =====================
    // READ ALL
    // =====================
    public ResponseEntity<List<ContainerResponse>> findAll() {
        List<ContainerResponse> list = containerRepository.findAll()
                .stream()
                .map(c -> ContainerResponse.builder()
                        .id(c.getId())
                        .ip(c.getIp())
                        .engine(c.getEngine().getName())
                        .status(c.getStatus().name())
                        .createAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    // =====================
    // READ BY ID
    // =====================
    public ResponseEntity<ContainerResponse> findById(Long id) {
        Container c = containerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Container not found"));

        ContainerResponse response = ContainerResponse.builder()
                .id(c.getId())
                .ip(c.getIp())
                .ports(c.getPort())
                .engine(c.getEngine().getName())
                .status(c.getStatus().name())
                .createAt(c.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    // =====================
    // UPDATE STATUS
    // =====================
    /*
    @Transactional
    public ResponseEntity<ContainerResponse> updateStatus(Long id, String status) {
        Container container = containerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Container not found"));

        container.setStatus(status.toUpperCase());
        container.setUpdatedAt(LocalDateTime.now());

        containerRepository.save(container);

        ContainerResponse response = ContainerResponse.builder()
                .id(container.getId())
                .name(container.getName())
                .image(container.getImage())
                .engine(container.getEngine())
                .status(container.getStatus())
                .createdAt(container.getCreatedAt())
                .updatedAt(container.getUpdatedAt())
                .build();

        return ResponseEntity.ok(response);
    }*/

    // =====================
    // DELETE
    // =====================
    @Transactional
    public ResponseEntity<String> delete(Long id) {
        if (!containerRepository.existsById(id)) {
            throw new RuntimeException("Container not found");
        }
        containerRepository.deleteById(id);
        return ResponseEntity.ok("Container deleted successfully");
    }
}

