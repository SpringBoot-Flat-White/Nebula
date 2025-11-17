package com.nebula.nebulaCloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to create a payment in Mercado Pago.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    
    /**
     * ID of the plan that the user wants to contract.
     */
    private Long planId;
    
    /**
     * Email of the payer (optional, can be different from authenticated user).
     */
    private String payerEmail;
    
    /**
     * Phone of the payer (optional).
     */
    private String payerPhone;
}
