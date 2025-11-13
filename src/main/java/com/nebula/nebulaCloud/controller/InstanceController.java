package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.dto.InstanceRequest;
import com.nebula.nebulaCloud.dto.InstanceResponse;
import com.nebula.nebulaCloud.service.InstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instances")
@RequiredArgsConstructor
public class InstanceController {

    private final InstanceService instanceService;

    @PostMapping
    public ResponseEntity<InstanceResponse> createInstance(@RequestBody InstanceRequest request) {
        return instanceService.create(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<InstanceResponse>> getAllByUserId(@PathVariable Long id) {
        return instanceService.getAllByUserId(id);
    }

    @GetMapping("/{userId}/{engineId}")
    public ResponseEntity<List<InstanceResponse>> getAllByEngineId(@PathVariable Long userId, @PathVariable Long engineId) {
        return instanceService.getAllByEngineId(userId, engineId);
    }
}
