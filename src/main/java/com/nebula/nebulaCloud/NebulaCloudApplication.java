package com.nebula.nebulaCloud;

import com.nebula.nebulaCloud.config.EngineConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(EngineConfig.class)
@EnableScheduling
public class NebulaCloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(NebulaCloudApplication.class, args);
	}

}
