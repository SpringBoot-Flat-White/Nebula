package com.nebula.nebulaCloud.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for organization response data.
 */
@Data
@Builder
public class OrganizationResponse {
    // TODO: Organization unique ID
    private Long id;

    // TODO: Organization name
    private String name;

    // TODO: Owner ID reference
    private Long ownerId;

    // TODO: Associated user ID
    private Long userId;

    // TODO: Organization creation timestamp
    private LocalDateTime createdAt;
}

