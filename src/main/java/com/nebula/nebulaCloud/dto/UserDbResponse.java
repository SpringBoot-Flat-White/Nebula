package com.nebula.nebulaCloud.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for database user response data.
 */
@Data
@Builder
public class UserDbResponse {
    // TODO: Database user unique ID
    private Long id;

    // TODO: Database username
    private String dbUser;

    // TODO: Associated user ID
    private Long userId;
}
