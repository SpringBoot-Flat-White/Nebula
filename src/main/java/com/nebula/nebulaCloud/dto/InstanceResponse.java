package com.nebula.nebulaCloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for instance response data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceResponse {

    private Long id;
    private String status;
    private String databaseName;
    private LocalDateTime createdAt;

    // Container information
    private Long containerId;
    private String containerIp;
    private Integer containerPort;
    private String engineName;

    // Database connection information (no password exposed)
    private String dbUsername;
    private String password;

    // User information
    private Long userId;

}
