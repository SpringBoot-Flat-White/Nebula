package com.nebula.nebulaCloud.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO returned when fetching Individual data.
 */
@Data
@Builder
public class IndividualResponse {

    private Long id;
    private String fullName;
    private Long userId;

}
