package com.nebula.nebulaCloud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending notifications to Discord via webhooks.
 * 
 * This service handles asynchronous notifications to Discord channels
 * without blocking the main application flow. If Discord is unavailable,
 * errors are logged but do not affect the application's core functionality.
 */
@Slf4j
@Service
public class DiscordNotificationService {

    @Value("${discord.webhook.url:}")
    private String webhookUrl;

    @Value("${discord.webhook.enabled:false}")
    private boolean webhookEnabled;

    private final RestTemplate restTemplate;

    public DiscordNotificationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends a notification to Discord when a new user registers.
     * 
     * This method is asynchronous and will not block the registration process.
     * If the webhook is disabled or fails, the error is logged but the registration
     * continues.
     * 
     * @param email    The email of the newly registered user
     * @param fullName The full name of the user
     * @param userType The type of user (INDIVIDUAL or ORGANIZATION)
     */
    @Async
    public void sendUserRegistrationNotification(String email, String fullName, String userType) {
        // Check if webhook is enabled
        if (!webhookEnabled) {
            log.debug("Discord webhook is disabled. Skipping notification.");
            return;
        }

        // Check if webhook URL is configured
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("Discord webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            // Build the Discord embed message
            Map<String, Object> embed = buildRegistrationEmbed(email, fullName, userType);

            // Create the webhook payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", List.of(embed));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Send POST request to Discord webhook
            restTemplate.postForEntity(webhookUrl, request, String.class);

            log.info("Successfully sent Discord notification for new user: {}", email);

        } catch (Exception e) {
            // Log error but don't throw exception - we don't want to break registration
            log.error("Failed to send Discord notification for user {}: {}", email, e.getMessage());
        }
    }

    /**
     * Builds a Discord embed object for user registration notification.
     * 
     * Discord embeds allow for rich, formatted messages with colors, fields, and
     * timestamps.
     * 
     * @param email    User's email
     * @param fullName User's full name
     * @param userType User type (INDIVIDUAL/ORGANIZATION)
     * @return Map representing the Discord embed structure
     */
    private Map<String, Object> buildRegistrationEmbed(String email, String fullName, String userType) {
        Map<String, Object> embed = new HashMap<>();

        // Set embed title and description
        embed.put("title", "🎉 Nuevo Usuario Registrado");
        embed.put("description", "Un nuevo usuario se ha registrado en Nebula Cloud");

        // Set color (green for success) - Discord uses decimal color codes
        embed.put("color", 5763719); // #57F287 (Discord green)

        // Add fields with user information
        Map<String, Object> emailField = new HashMap<>();
        emailField.put("name", "📧 Email");
        emailField.put("value", email);
        emailField.put("inline", false);

        Map<String, Object> nameField = new HashMap<>();
        nameField.put("name", "👤 Nombre Completo");
        nameField.put("value", fullName != null && !fullName.isEmpty() ? fullName : "No proporcionado");
        nameField.put("inline", false);

        Map<String, Object> typeField = new HashMap<>();
        typeField.put("name", "🏷️ Tipo de Usuario");
        typeField.put("value", formatUserType(userType));
        typeField.put("inline", false);

        embed.put("fields", List.of(emailField, nameField, typeField));

        // Add timestamp
        embed.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        // Add footer
        Map<String, Object> footer = new HashMap<>();
        footer.put("text", "Nebula Cloud - Sistema de Notificaciones");
        embed.put("footer", footer);

        return embed;
    }

    /**
     * Formats the user type for display in Discord.
     * 
     * @param userType The raw user type string
     * @return Formatted user type string
     */
    private String formatUserType(String userType) {
        if (userType == null) {
            return "Desconocido";
        }

        return switch (userType.toUpperCase()) {
            case "INDIVIDUAL" -> "👤 Individual";
            case "ORGANIZATION" -> "🏢 Organización";
            default -> userType;
        };
    }
}
