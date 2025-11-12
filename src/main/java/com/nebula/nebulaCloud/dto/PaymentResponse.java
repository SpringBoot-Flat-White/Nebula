package com.nebula.nebulaCloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response after processing a payment.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    
    /**
     * Unique ID of the payment preference in Mercado Pago.
     */
    private String preferenceId;
    
    /**
     * URL to redirect user to Mercado Pago (checkout).
     */
    private String initPoint;
    
    /**
     * Redirect URL after successful payment.
     */
    private String successUrl;
    
    /**
     * Redirect URL if payment fails.
     */
    private String failureUrl;
    
    /**
     * Status message.
     */
    private String message;
}
