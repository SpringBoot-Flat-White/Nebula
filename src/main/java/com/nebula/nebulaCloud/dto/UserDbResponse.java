package com.nebula.nebulaCloud.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDbResponse {
    private Long id;
    private String dbUser;
    private Long userId;
}
