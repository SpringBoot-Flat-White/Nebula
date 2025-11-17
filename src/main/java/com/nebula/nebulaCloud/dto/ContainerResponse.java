package com.nebula.nebulaCloud.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for container response data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContainerResponse {
    // TODO: Container IP address
    private String ip;

    // TODO: Container unique ID
    private Long id;

    // TODO: Container status (e.g. Up X minutes, Exited)
    private String status;

    // TODO: Container exposed ports
    private Integer ports;

    // TODO: Associated engine name
    private String engine;

    // TODO: Container creation timestamp
    private LocalDateTime createAt;
}
