package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.dto.IndividualRequest;
import com.nebula.nebulaCloud.dto.IndividualResponse;
import com.nebula.nebulaCloud.service.IndividualService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/individuals")
@RequiredArgsConstructor
public class IndividualController {

    private final IndividualService individualService;

    @PostMapping
    public ResponseEntity<IndividualResponse> create(@Valid @RequestBody IndividualRequest request) {
        return ResponseEntity.ok(individualService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<IndividualResponse>> getAll() {
        return ResponseEntity.ok(individualService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndividualResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(individualService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        individualService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
