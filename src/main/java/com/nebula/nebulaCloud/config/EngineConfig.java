package com.nebula.nebulaCloud.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "engines")
@Getter
@Setter
public class EngineConfig {

    private EngineDetails mysql;
    private EngineDetails postgres;
    private EngineDetails sqlserver;
    private EngineDetails mongodb;
    private EngineDetails cassandra;
    private EngineDetails redis;


}

