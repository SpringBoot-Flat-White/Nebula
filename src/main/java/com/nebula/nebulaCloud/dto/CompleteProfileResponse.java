package com.nebula.nebulaCloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO after completing profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileResponse {

    private String email;
    private String fullName;
    private Boolean profileCompleted;
    private String message;
}

