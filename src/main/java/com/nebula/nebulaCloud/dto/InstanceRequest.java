package com.nebula.nebulaCloud.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for creating a new database instance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceRequest {

    // TODO: User ID who owns the instance
    @NotNull(message = "User ID is required")
    private Long user;

    // TODO: Database engine type ID
    @NotNull(message = "Engine ID is required")
    private Long engineId;

    // TODO: Name of the database to create
    private String databaseName;

    // TODO: Database username
    @Size(max = 100, message = "Username must be at most 100 characters")
    private String dbUser;

    // TODO: Database password (plain text, will be encrypted before saving)
    @Size(max = 255, message = "Password must be at most 255 characters")
    private String dbPassword;

}
