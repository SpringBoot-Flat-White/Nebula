package com.nebula.nebulaCloud.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO used to return organization data in API responses.
 */
@Data
@Builder
public class OrganizationResponse {
    private Long id;
    private String name;
    private Long ownerId;
    private Long userId;
    private LocalDateTime createdAt;
}

