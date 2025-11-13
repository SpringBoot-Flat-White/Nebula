package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.model.Engine;
import com.nebula.nebulaCloud.service.EngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engines")
@RequiredArgsConstructor
public class EngineController {

    private final EngineService engineService;

    @GetMapping
    public ResponseEntity<List<Engine>> getAll() {
        return engineService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Engine> getById(@PathVariable Long id) {
        return engineService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Engine> create(@RequestBody String request) {
        return engineService.create(request);
    }

    /*
    @PutMapping("/{id}")
    public ResponseEntity<Engine> update(@PathVariable Long id, @RequestBody Engine request) {
        return engineService.update(id, request);
    }*/

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return engineService.delete(id);
    }
}
