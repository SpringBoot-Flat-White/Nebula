package com.nebula.nebulaCloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for representing a transaction/payment in the user's transaction history.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    
    /**
     * Payment ID.
     */
    private Long id;
    
    /**
     * Name of the plan associated with this transaction.
     */
    private String planName;
    
    /**
     * Amount paid.
     */
    private BigDecimal amount;
    
    /**
     * Transaction status (APPROVED, PENDING, FAILED, PAUSED).
     */
    private String status;
    
    /**
     * Transaction ID from payment processor (Preference ID).
     */
    private String transactionId;
    
    /**
     * Mercado Pago Payment ID.
     */
    private String mercadoPagoPaymentId;
    
    /**
     * Date when the transaction was created.
     */
    private LocalDateTime createdAt;
}
