package com.nebula.nebulaCloud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user authentication (login) requests.
 * This class defines the structure and validation rules for the credentials
 * required to authenticate a user.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    /**
     * The user's registered email address.
     * Must be a valid email format and not be empty.
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is not valid.")
    private String email;

    /**
     * The user's plain-text password.
     * Must not be empty.
     */
    @NotBlank(message = "Password is required.")
    private String password;
}