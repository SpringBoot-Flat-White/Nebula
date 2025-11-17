package com.nebula.nebulaCloud.dto;

import com.nebula.nebulaCloud.model.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new organization.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationRequest {

    // TODO: Email address for the organization account
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is not valid.")
    @Size(max = 120, message = "Email must not exceed 120 characters.")
    private String email;

    // TODO: Plain-text password for the account
    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    private String password;

    // TODO: User account type (defaults to ORGANIZATION)
    @Builder.Default
    private UserType userType = UserType.ORGANIZATION;

    // TODO: Optional plan ID for subscription
    private Long planId;

    // TODO: Organization name
    @NotBlank(message = "The organization name is required")
    @Size(max = 120, message = "Name must not exceed 120 characters.")
    private String name;

    // TODO: Owner ID reference
    @NotNull(message = "The owner ID is required")
    private Long ownerId;

}