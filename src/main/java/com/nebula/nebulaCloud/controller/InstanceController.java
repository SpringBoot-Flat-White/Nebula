package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.dto.InstanceRequest;
import com.nebula.nebulaCloud.dto.InstanceResponse;
import com.nebula.nebulaCloud.service.InstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instances")
@RequiredArgsConstructor
public class InstanceController {

    private final InstanceService instanceService;

    @PostMapping
    public ResponseEntity<InstanceResponse> createInstance(@RequestBody InstanceRequest request) {
        return instanceService.create(request);
    }
}
