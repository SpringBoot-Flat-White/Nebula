package com.nebula.nebulaCloud.config;

import lombok.Data;

@Data
public class EngineDetails {
    private String host;
    private int port;
    private String rootUser;
    private String rootPassword;
}
