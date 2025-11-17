package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.dto.IndividualRequest;
import com.nebula.nebulaCloud.dto.IndividualResponse;
import com.nebula.nebulaCloud.service.IndividualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing individual user profiles.
 */
@RestController
@RequestMapping("/api/individuals")
@RequiredArgsConstructor
public class IndividualController {

    private final IndividualService individualService;

    // TODO: Create a new individual profile
    @PostMapping
    public ResponseEntity<IndividualResponse> create(@Valid @RequestBody IndividualRequest request) {
        return ResponseEntity.ok(individualService.create(request));
    }

    // TODO: Get all individual profiles
    @GetMapping
    public ResponseEntity<List<IndividualResponse>> getAll() {
        return ResponseEntity.ok(individualService.findAll());
    }

    // TODO: Get individual profile by ID
    @GetMapping("/{id}")
    public ResponseEntity<IndividualResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(individualService.findById(id));
    }

    // TODO: Delete an individual profile
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        individualService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
