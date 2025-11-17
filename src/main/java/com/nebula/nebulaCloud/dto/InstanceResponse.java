package com.nebula.nebulaCloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for instance response data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceResponse {

    // TODO: Instance unique ID
    private Long id;

    // TODO: Instance status (RUNNING, SUSPENDED, etc.)
    private String status;

    // TODO: Database name
    private String databaseName;

    // TODO: Instance creation timestamp
    private LocalDateTime createdAt;

    // TODO: Container ID
    private Long containerId;

    // TODO: Container IP address
    private String containerIp;

    // TODO: Container port
    private Integer containerPort;

    // TODO: Engine name (MySQL, PostgreSQL, etc.)
    private String engineName;

    // TODO: Database username (password not exposed)
    private String dbUsername;

    // TODO: Database password
    private String password;

    // TODO: User ID who owns the instance
    private Long userId;

}
