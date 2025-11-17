package com.nebula.nebulaCloud.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for database engine connection details.
 * Maps properties from application.properties with prefix "engines".
 */
@ConfigurationProperties(prefix = "engines")
@Getter
@Setter
public class EngineConfig {

    // TODO: Load MySQL connection configuration from properties
    private EngineDetails mysql;

    // TODO: Load PostgreSQL connection configuration from properties
    private EngineDetails postgres;

    // TODO: Load SQL Server connection configuration from properties
    private EngineDetails sqlserver;

    // TODO: Load MongoDB connection configuration from properties
    private EngineDetails mongodb;

    // TODO: Load Cassandra connection configuration from properties
    private EngineDetails cassandra;

    // TODO: Load Redis connection configuration from properties
    private EngineDetails redis;

}

