package com.nebula.nebulaCloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for creating database user requests.
 */
@Data
public class UserDbRequest {

    // TODO: Database username
    @NotBlank(message = "Database username is required")
    private String dbUser;

    // TODO: Associated user ID
    @NotNull(message = "User ID is required")
    private Long userId;
}
