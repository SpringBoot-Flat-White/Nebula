package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.dto.InstanceRequest;
import com.nebula.nebulaCloud.dto.InstanceResponse;
import com.nebula.nebulaCloud.dto.InstanceUpdateRequest;
import com.nebula.nebulaCloud.service.InstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing database instances.
 */
@RestController
@RequestMapping("/api/instances")
@RequiredArgsConstructor
public class InstanceController {

    private final InstanceService instanceService;

    // TODO: Get all instances
    @GetMapping
    public ResponseEntity<List<InstanceResponse>> getAll() {
        return instanceService.getAll();
    }

    // TODO: Create a new database instance
    @PostMapping
    public ResponseEntity<InstanceResponse> createInstance(@RequestBody InstanceRequest request) {
        return instanceService.create(request);
    }

    // TODO: Get all instances for a specific user
    @GetMapping("/{id}")
    public ResponseEntity<List<InstanceResponse>> getAllByUserId(@PathVariable Long id) {
        return instanceService.getAllByUserId(id);
    }

    // TODO: Get instances filtered by user and engine
    @GetMapping("/{userId}/{engineId}")
    public ResponseEntity<List<InstanceResponse>> getAllByEngineId(@PathVariable Long userId, @PathVariable Long engineId) {
        return instanceService.getAllByEngineId(userId, engineId);
    }

    // TODO: Update instance status (suspend/resume)
    @PutMapping
    public ResponseEntity<InstanceResponse> updateInstance(@RequestBody InstanceUpdateRequest request) {
        return instanceService.updateInstance(request);
    }
}
