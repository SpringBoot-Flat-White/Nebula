package com.nebula.nebulaCloud;

import com.nebula.nebulaCloud.config.EngineConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(EngineConfig.class)
public class NebulaCloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(NebulaCloudApplication.class, args);
	}

}
