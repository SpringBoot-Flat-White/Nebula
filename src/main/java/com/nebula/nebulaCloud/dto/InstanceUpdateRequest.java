package com.nebula.nebulaCloud.dto;

import com.nebula.nebulaCloud.model.Instance;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanceUpdateRequest {

    @NotNull(message = "User ID is required")
    private Long user;

    @NotNull(message = "Instance ID is required")
    private Long instanceId;

    @NotNull(message = "Status is required")
    private Instance.Status status;
}
