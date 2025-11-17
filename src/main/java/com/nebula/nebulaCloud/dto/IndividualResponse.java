package com.nebula.nebulaCloud.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for individual profile response data.
 */
@Data
@Builder
public class IndividualResponse {

    // TODO: Individual's unique ID
    private Long id;

    // TODO: Individual's full name
    private String fullName;

    // TODO: Associated user ID
    private Long userId;

}
