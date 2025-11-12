package com.nebula.nebulaCloud.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContainerResponse {
    private String ip;
    private Long id;
    private String status; // e.g. Up X minutes, Exited (0) X hours ago
    private Integer ports;
    private String engine;
    private LocalDateTime createAt;// raw ports info
}
