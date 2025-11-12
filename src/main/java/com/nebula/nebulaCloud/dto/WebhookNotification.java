package com.nebula.nebulaCloud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the webhook notification received from Mercado Pago.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebhookNotification {
    
    /**
     * Event type (payment.created, payment.updated, etc.).
     */
    private String type;
    
    /**
     * ID of the action that changed.
     */
    @JsonProperty("id")
    private String actionId;
    
    /**
     * Datos del evento.
     */
    private NotificationData data;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationData {
        
        /**
         * ID del pago en Mercado Pago.
         */
        private String id;
    }
}
