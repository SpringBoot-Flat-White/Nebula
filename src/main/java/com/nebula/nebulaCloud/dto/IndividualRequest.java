package com.nebula.nebulaCloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO used to create or update an Individual.
 */
@Data
public class IndividualRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotNull(message = "User ID is required")
    private Long userId;
}
