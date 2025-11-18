package com.nebula.nebulaCloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for database statistics by date
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseStatsResponse {
    private LocalDate date;
    private Long count;
}

