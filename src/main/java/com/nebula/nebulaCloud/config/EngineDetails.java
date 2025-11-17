package com.nebula.nebulaCloud.config;

import lombok.Data;

/**
 * Holds connection details for a specific database engine.
 */
@Data
public class EngineDetails {
    // TODO: Database host address
    private String host;

    // TODO: Database port number
    private int port;

    // TODO: Root/admin username for database connections
    private String rootUser;

    // TODO: Root/admin password for database connections
    private String rootPassword;
}
