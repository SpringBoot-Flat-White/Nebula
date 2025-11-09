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
 * DTO used to create a new Organization.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationRequest {

    /**
     * The email address for the new account. It will also serve as the username.
     * Must be a valid email format and not be empty.
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is not valid.")
    @Size(max = 120, message = "Email must not exceed 120 characters.")
    private String email;

    /**
     * The plain-text password for the new account.
     * Must not be empty and should have a length between 8 and 100 characters.
     */
    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    private String password;

    /**
     * The type of the user account.
     * If not provided, it defaults to ORGANIZATION.
     */
    @Builder.Default
    private UserType userType = UserType.ORGANIZATION;

    /**
     * The optional ID of the plan to which the user is subscribing upon registration.
     */
    private Long planId;

    /**
     * The full name of the user.
     * This field is mandatory.
     */
    @NotBlank(message = "The organization name is required")
    @Size(max = 120, message = "Name must not exceed 120 characters.")
    private String name;


    @NotNull(message = "The owner ID is required")
    private Long ownerId;


}