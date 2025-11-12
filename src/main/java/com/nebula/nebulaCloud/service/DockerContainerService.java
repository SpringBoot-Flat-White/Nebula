package com.nebula.nebulaCloud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@Slf4j
public class DockerContainerService {

    private String executeCommand(String... command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Docker command failed: " + String.join(" ", command) + "\nOutput:\n" + output);
            }

        } catch (Exception e) {
            log.error("Error executing Docker command", e);
            throw new RuntimeException("Error executing Docker command: " + e.getMessage());
        }

        return output.toString().trim();
    }

    // ======================
    // CREATE CONTAINER
    // ======================
    public String createContainer(String name, String image, int port) {
        String command = String.format("docker run -d --name %s -p %d:%d %s", name, port, port, image);
        log.info("Creating container: {}", command);
        return executeCommand("bash", "-c", command);
    }

    // ======================
    // START CONTAINER
    // ======================
    public String startContainer(String name) {
        log.info("Starting container {}", name);
        return executeCommand("bash", "-c", "docker start " + name);
    }

    // ======================
    // STOP CONTAINER
    // ======================
    public String stopContainer(String name) {
        log.info("Stopping container {}", name);
        return executeCommand("bash", "-c", "docker stop " + name);
    }

    // ======================
    // REMOVE CONTAINER
    // ======================
    public String removeContainer(String name) {
        log.info("Removing container {}", name);
        return executeCommand("bash", "-c", "docker rm -f " + name);
    }

    // ======================
    // LIST CONTAINERS
    // ======================
    public String listContainers(boolean all) {
        String flag = all ? "-a" : "";
        return executeCommand("bash", "-c", "docker ps " + flag);
    }
}
