package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.model.Engine;
import com.nebula.nebulaCloud.service.EngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing database engines.
 */
@RestController
@RequestMapping("/api/engines")
@RequiredArgsConstructor
public class EngineController {

    private final EngineService engineService;

    // TODO: Retrieve all available database engines
    @GetMapping
    public ResponseEntity<List<Engine>> getAll() {
        return engineService.getAll();
    }

    // TODO: Get specific engine by ID
    @GetMapping("/{id}")
    public ResponseEntity<Engine> getById(@PathVariable Long id) {
        return engineService.getById(id);
    }

    // TODO: Create a new database engine
    @PostMapping
    public ResponseEntity<Engine> create(@RequestBody String request) {
        return engineService.create(request);
    }

    /*
    // TODO: Update engine configuration
    @PutMapping("/{id}")
    public ResponseEntity<Engine> update(@PathVariable Long id, @RequestBody Engine request) {
        return engineService.update(id, request);
    }*/

    // TODO: Delete an engine by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return engineService.delete(id);
    }
}
