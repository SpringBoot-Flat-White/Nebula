package com.nebula.nebulaCloud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDbRequest {

    @NotBlank(message = "Database username is required")
    private String dbUser;

    @NotNull(message = "User ID is required")
    private Long userId;
}
