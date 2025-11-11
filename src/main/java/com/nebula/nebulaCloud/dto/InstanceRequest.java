package com.nebula.nebulaCloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Data Transfer Object for creating a new instance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceRequest {

    @NotNull(message = "User ID is required")
    private Long user;

    @NotNull(message = "Container ID is required")
    private Long containerId;

    @NotNull(message = "Engine ID is required")
    private Long engineId;

    @NotBlank(message = "Database name is required")
    private String databaseName;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must be at most 100 characters")
    private String dbUser;

    /*
    @NotBlank(message = "Encrypted database password is required")
    @Size(max = 255, message = "Encrypted password must be at most 255 characters")
    private String dbPasswordEnc;*/

}
