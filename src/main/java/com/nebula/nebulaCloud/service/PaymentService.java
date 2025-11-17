package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.dto.PaymentRequest;
import com.nebula.nebulaCloud.dto.PaymentResponse;
import com.nebula.nebulaCloud.model.Plan;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.repository.PaymentRepository;
import com.nebula.nebulaCloud.repository.PlanRepository;
import com.nebula.nebulaCloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Domain service for managing payments and plan changes.
 * Coordinates between the Mercado Pago service and database repositories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final MercadoPagoService mercadoPagoService;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    /**
     * Processes user plan change by creating a payment preference.
     * 
     * @param userId User ID.
     * @param request Payment information.
     * @return PaymentResponse with preference data.
     */
    @Transactional
    public PaymentResponse processPaymentForPlanChange(Long userId, PaymentRequest request) {
        try {
            // 1. Validate that user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Validate that plan exists
            Plan plan = planRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            // 3. Validate that the plan is not free (business rule)
            if (plan.getIsFree() != null && plan.getIsFree()) {
                throw new RuntimeException("Cannot change to a free plan through payment");
            }

            // 4. Create payment preference in Mercado Pago
            String payerEmail = request.getPayerEmail() != null ? 
                    request.getPayerEmail() : user.getEmail();
            
            PaymentResponse paymentResponse = mercadoPagoService
                    .createPaymentPreference(plan, payerEmail);

            // 5. Register payment attempt in DB with PENDING status
            com.nebula.nebulaCloud.model.Payment payment = 
                    com.nebula.nebulaCloud.model.Payment.builder()
                    .user(user)
                    .plan(plan)
                    .status(com.nebula.nebulaCloud.model.Payment.Status.PENDING)
                    .transactionId(paymentResponse.getPreferenceId())
                    .mercadoPagoPaymentId(null)  // Will be set when webhook is received
                    .amount(plan.getPrice())
                    .createdAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);
            
            log.info("Payment preference created for user: {} plan: {}", 
                    userId, request.getPlanId());

            return paymentResponse;

        } catch (Exception e) {
            log.error("Error processing payment for user: {}", userId, e);
            throw new RuntimeException("Error processing payment: " + e.getMessage(), e);
        }
    }

    /**
     * Processes Mercado Pago webhook notification to confirm payment.
     * 
     * @param paymentId Payment ID in Mercado Pago.
     */
    @Transactional
    public void processPaymentNotification(Long paymentId) {
        try {
            // 1. Get payment details from Mercado Pago
            Map<String, Object> mercadoPagoPayment = mercadoPagoService.getPaymentDetails(paymentId);
            
            String mpPaymentId = paymentId.toString();
            String mpStatus = mercadoPagoPayment.get("status").toString();
            String externalReference = (String) mercadoPagoPayment.get("external_reference");

            log.debug("Processing webhook - paymentId: {}, status: {}, externalRef: {}", 
                    paymentId, mpStatus, externalReference);

            // 2. Try to find payment by mercadoPagoPaymentId first (if already linked)
            java.util.Optional<com.nebula.nebulaCloud.model.Payment> paymentOptional = 
                    paymentRepository.findByMercadoPagoPaymentId(mpPaymentId);
            
            com.nebula.nebulaCloud.model.Payment payment;
            
            if (paymentOptional.isEmpty()) {
                // Payment not yet linked, search by transactionId (preference ID)
                // Try extracting the plan ID from external_reference
                // Format is: USER_<email>_PLAN_<planId>
                if (externalReference != null && externalReference.contains("PLAN_")) {
                    // This is a newly created payment, find by all pending payments and match
                    java.util.List<com.nebula.nebulaCloud.model.Payment> pendingPayments = 
                            paymentRepository.findAll()
                                    .stream()
                                    .filter(p -> p.getStatus() == com.nebula.nebulaCloud.model.Payment.Status.PENDING 
                                              && p.getMercadoPagoPaymentId() == null)
                                    .toList();
                    
                    log.info("🔍 Found {} pending payments without mercadoPagoPaymentId", pendingPayments.size());
                    
                    if (pendingPayments.isEmpty()) {
                        throw new RuntimeException(
                                "No pending payments found for MP Payment ID: " + paymentId);
                    }
                    
                    // If multiple pending payments, use the most recent one
                    payment = pendingPayments.stream()
                            .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()))
                            .orElseThrow(() -> new RuntimeException(
                                    "Could not find matching pending payment for " + paymentId));
                    
                    log.info("✅ Matched webhook payment {} to pending payment {} for user: {}", 
                            paymentId, payment.getId(), payment.getUser().getId());
                } else {
                    throw new RuntimeException(
                            "Payment not found in DB for MP Payment ID: " + paymentId);
                }
            } else {
                payment = paymentOptional.get();
                log.info("✅ Found payment {} already linked with mercadoPagoPaymentId", payment.getId());
            }

            // 3. Store the MP Payment ID for future reference
            if (payment.getMercadoPagoPaymentId() == null) {
                payment.setMercadoPagoPaymentId(mpPaymentId);
            }

            // 4. Verify if payment was approved
            if (mercadoPagoService.isPaymentApproved(mpStatus)) {
                // Payment was approved
                payment.setStatus(com.nebula.nebulaCloud.model.Payment.Status.APPROVED);
                
                // 5. Update user's plan
                User user = payment.getUser();
                user.setPlan(payment.getPlan());
                userRepository.save(user);
                
                // 6. Send plan purchase confirmation email
                try {
                    emailService.sendPlanPurchaseConfirmation(
                            user.getEmail(),
                            payment.getPlan().getName(),
                            payment.getAmount(),
                            payment.getPlan().getMaxInstances()
                    );
                } catch (Exception emailError) {
                    log.error("Failed to send plan purchase email for user: {} but payment was successful", user.getId(), emailError);
                    // Don't fail the payment if email fails - payment is already successful
                }
                
                log.info("✅ PAYMENT APPROVED - Payment ID: {} - User: {} - Plan changed to: {}", 
                        paymentId, user.getId(), payment.getPlan().getId());
            } else {
                // Payment failed or pending
                payment.setStatus(com.nebula.nebulaCloud.model.Payment.Status.FAILED);
                log.warn("⚠️ PAYMENT NOT APPROVED - Payment ID: {} - Status from MP: {} - User: {}", 
                        paymentId, mpStatus, payment.getUser().getId());
            }

            // 7. Save status change
            paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Error processing payment notification: {}", paymentId, e);
            throw new RuntimeException("Error processing notification: " + e.getMessage(), e);
        }
    }

    /**
     * Gets payment history for a user.
     * 
     * @param userId User ID.
     * @return List of user's payments.
     */
    public java.util.List<com.nebula.nebulaCloud.model.Payment> getPaymentHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return paymentRepository.findByUser(user);
    }

    /**
     * Gets transaction history for a user as DTOs.
     * 
     * @param userId User ID.
     * @return List of transaction responses.
     */
    public java.util.List<com.nebula.nebulaCloud.dto.TransactionResponse> getTransactionHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        java.util.List<com.nebula.nebulaCloud.model.Payment> payments = paymentRepository.findByUser(user);
        
        return payments.stream()
                .map(payment -> com.nebula.nebulaCloud.dto.TransactionResponse.builder()
                        .id(payment.getId())
                        .planName(payment.getPlan().getName())
                        .amount(payment.getAmount())
                        .status(payment.getStatus().name())
                        .transactionId(payment.getTransactionId())
                        .mercadoPagoPaymentId(payment.getMercadoPagoPaymentId())
                        .createdAt(payment.getCreatedAt())
                        .build())
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // Most recent first
                .collect(java.util.stream.Collectors.toList());
    }
}
