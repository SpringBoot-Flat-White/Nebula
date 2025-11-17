package com.nebula.nebulaCloud.dto;

import com.nebula.nebulaCloud.model.Instance;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating instance status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceUpdateRequest {

    // TODO: User ID who owns the instance
    @NotNull(message = "User ID is required")
    private Long user;

    // TODO: Instance ID to update
    @NotNull(message = "Instance ID is required")
    private Long instanceId;

    // TODO: New status (RUNNING or SUSPENDED)
    @NotNull(message = "Status is required")
    private Instance.Status status;
}
