package com.nebula.nebulaCloud.dto;

import com.nebula.nebulaCloud.model.Engine;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for creating container requests.
 */
@Data
public class ContainerRequest {

    // TODO: Container IP address
    @NotBlank(message = "IP address is required")
    private String ip;

    // TODO: Container port number
    @NotNull(message = "Port number is required")
    private Integer port;

    // TODO: Associated engine ID
    @NotNull(message = "Engine type is required")
    private Long engineId;
}

