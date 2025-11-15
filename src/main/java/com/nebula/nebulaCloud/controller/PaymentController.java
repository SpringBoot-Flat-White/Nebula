package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.dto.PaymentRequest;
import com.nebula.nebulaCloud.dto.PaymentResponse;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing payment operations and plan changes.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Endpoints for managing payments and plan changes")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Initiates the plan change process for the authenticated user.
     * 
     * @param request DTO with payment plan information.
     * @param authentication Authenticated user data.
     * @return Payment preference information with Mercado Pago checkout link.
     */
    @PostMapping("/change-plan")
    @Operation(summary = "Change user plan",
            description = "Initiates the payment process to change the authenticated user's plan")
    public ResponseEntity<PaymentResponse> changePlan(
            @RequestBody PaymentRequest request,
            Authentication authentication
    ) {
        // 1. Get the user from the principal (User entity)
        User user = (User) authentication.getPrincipal();
        
        // 2. Process the payment
        PaymentResponse response = paymentService.processPaymentForPlanChange(user.getId(), request);

        return ResponseEntity.ok(response);
    }

    /**
     * Webhook to receive Mercado Pago notifications.
     * 
     * @param paymentId Payment ID from Mercado Pago.
     * @return HTTP 200 OK if processed successfully.
     */
    @PostMapping("/webhook")
    @Operation(summary = "Mercado Pago Webhook",
            description = "Receives notifications of payment status changes")
    public ResponseEntity<String> handlePaymentNotification(
            @RequestParam("data.id") Long paymentId,
            @RequestBody Map<String,Object> data
    ) {
        try {
            org.slf4j.LoggerFactory.getLogger(this.getClass())
                    .info("🔔 WEBHOOK RECEIVED - Payment ID: {}", paymentId);
            
            paymentService.processPaymentNotification(paymentId);
            
            org.slf4j.LoggerFactory.getLogger(this.getClass())
                    .info("✅ WEBHOOK PROCESSED - Payment ID: {}", paymentId);
            
            return ResponseEntity.ok("Notification processed");
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(this.getClass())
                    .error("❌ WEBHOOK ERROR - Payment ID: {} - Error: {}", paymentId, e.getMessage(), e);
            
            return ResponseEntity.status(500).body("Error processing notification: " + e.getMessage());
        }
    }

    /**
     * Redirect URLs after payment (for the client).
     */
    @GetMapping("/success")
    @Operation(summary = "Payment successful",
            description = "Successful payment confirmation page")
    public ResponseEntity<String> paymentSuccess() {
        return ResponseEntity.ok("Payment successful! Your plan has been updated.");
    }

    @GetMapping("/failure")
    @Operation(summary = "Payment rejected",
            description = "Rejected payment page")
    public ResponseEntity<String> paymentFailure() {
        return ResponseEntity.ok("Payment was rejected. Please try again.");
    }

    @GetMapping("/pending")
    @Operation(summary = "Payment pending",
            description = "Pending payment page")
    public ResponseEntity<String> paymentPending() {
        return ResponseEntity.ok("Your payment is pending confirmation.");
    }

    /**
     * Gets the transaction history for the authenticated user.
     * 
     * @param authentication Authenticated user data.
     * @return List of transactions.
     */
    @GetMapping("/transactions")
    @Operation(summary = "Get user transaction history",
            description = "Retrieves all transactions/payments for the authenticated user")
    public ResponseEntity<java.util.List<com.nebula.nebulaCloud.dto.TransactionResponse>> getTransactions(
            Authentication authentication
    ) {
        // Get the user from the principal
        User user = (User) authentication.getPrincipal();
        
        // Get transaction history
        java.util.List<com.nebula.nebulaCloud.dto.TransactionResponse> transactions = 
                paymentService.getTransactionHistory(user.getId());

        return ResponseEntity.ok(transactions);
    }
}
