package com.nebula.nebulaCloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO used to create a new Organization.
 */
@Data
public class OrganizationRequest {

    @NotBlank(message = "The organization name is required")
    private String name;

    @NotNull(message = "The owner ID is required")
    private Long ownerId;

    @NotNull(message = "The user ID is required")
    private Long userId;
}