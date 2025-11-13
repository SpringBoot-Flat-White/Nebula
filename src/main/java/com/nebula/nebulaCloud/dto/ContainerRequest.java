package com.nebula.nebulaCloud.dto;

import com.nebula.nebulaCloud.model.Engine;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContainerRequest {

    @NotBlank(message = "IP address is required")
    private String ip;

    @NotNull(message = "Port number is required")
    private Integer port;

    @NotNull(message = "Engine type is required")
    private Long engineId;
}

