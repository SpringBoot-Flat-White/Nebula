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

    @NotNull(message = "Engine ID is required")
    private Long engineId;


    private String databaseName;


    @Size(max = 100, message = "Username must be at most 100 characters")
    private String dbUser;



    @Size(max = 255, message = "Encrypted password must be at most 255 characters")
    private String dbPasswordEnc;

}
