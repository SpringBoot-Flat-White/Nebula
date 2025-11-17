package com.nebula.nebulaCloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for creating or updating individual profiles.
 */
@Data
public class IndividualRequest {

    // TODO: Individual's full name
    @NotBlank(message = "Full name is required")
    private String fullName;

    // TODO: Owner ID reference
    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    // TODO: Associated user ID
    @NotNull(message = "User ID is required")
    private Long userId;
}
