package com.nebula.nebulaCloud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nebula.nebulaCloud.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for authentication responses.
 * This class encapsulates the data returned to the client upon successful
 * registration or login, primarily the JWT access token.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    /**
     * The email address of the authenticated user.
     */
    private String email;

    /**
     * The full name of the authenticated user.
     */
    private String fullName;

    /**
     * The type of the user account (e.g., INDIVIDUAL, COMPANY).
     */
    private UserType userType;

    /**
    * user_id
    */
    private Long userId;

    /**
    * user plan id
    */
    private Long planId;
}