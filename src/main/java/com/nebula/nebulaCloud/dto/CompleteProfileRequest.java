package com.nebula.nebulaCloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for completing user profile after OAuth2 registration.
 *
 * This is used when a user logs in via OAuth2 for the first time
 * and needs to provide their real information (since OAuth2 providers
 * may give incomplete or fake names like "xxgamexx").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    // Add other fields as needed (phone, address, etc.)
}

