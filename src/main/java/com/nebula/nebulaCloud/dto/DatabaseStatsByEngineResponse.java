package com.nebula.nebulaCloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for database statistics by engine
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseStatsByEngineResponse {
    private String engineName;
    private Long count;
}

