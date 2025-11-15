package com.nebula.nebulaCloud.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.nebula.nebulaCloud.dto.PaymentResponse;
import com.nebula.nebulaCloud.model.Plan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that encapsulates the integration logic with the Mercado Pago API.
 * Handles payment preference creation, payment queries, etc.
 * Uses the official Mercado Pago SDK.
 */
@Service
@Slf4j
public class MercadoPagoService {

    @Value("${mercado-pago.access-token}")
    private String accessToken;
    
    @Value("${mercado-pago.webhook-url}")
    private String webhookUrl;
    
    @Value("${application.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Initialize Mercado Pago SDK with access token.
     */
    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            log.info("Mercado Pago SDK initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing Mercado Pago SDK", e);
            throw new RuntimeException("Failed to initialize Mercado Pago SDK", e);
        }
    }

    /**
     * Creates a payment preference in Mercado Pago using the official SDK.
     * 
     * @param plan The plan to be paid.
     * @param userEmail Email of the user making the payment.
     * @return PaymentResponse with preference details.
     */
    public PaymentResponse createPaymentPreference(Plan plan, String userEmail) {
        try {
            // Create preference client
            PreferenceClient client = new PreferenceClient();
            
            // Create preference item
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(plan.getId().toString())
                    .title(plan.getName() + " - Plan")
                    .description("Upgrade to " + plan.getName() + " plan")
                    .quantity(1)
                    .currencyId("COP") // Colombian Peso (Peso Colombiano)
                    .unitPrice(plan.getPrice())
                    .build();
            
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);
            
            // Create payer
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(userEmail)
                    .build();
            
            // Create back URLs
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(baseUrl + "/api/v1/payments/success")
                    .failure(baseUrl + "/api/v1/payments/failure")
                    .pending(baseUrl + "/api/v1/payments/pending")
                    .build();
            
            log.debug("Creating preference with back URLs - Success: {}, Failure: {}, Pending: {}", 
                    baseUrl + "/api/v1/payments/success",
                    baseUrl + "/api/v1/payments/failure",
                    baseUrl + "/api/v1/payments/pending");
            
            // Configure payment methods
            // Allow multiple payment methods for Colombia
            PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                    .excludedPaymentTypes(List.of())
                    .excludedPaymentMethods(List.of())
                    .build();
            
            // Note: By default, Mercado Pago allows:
            // - Credit cards (all brands)
            // - Debit cards
            // - PSE (bank transfers)
            // - Cash payments (Efecty, Baloto, etc.)
            // To exclude specific methods, uncomment and modify:
            // List<PreferencePaymentTypeRequest> excludedPaymentTypes = new ArrayList<>();
            // excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("ticket").build()); // Exclude cash
            // excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("bank_transfer").build()); // Exclude PSE
            // paymentMethods = PreferencePaymentMethodsRequest.builder()
            //         .excludedPaymentTypes(excludedPaymentTypes)
            //         .installments(12)
            //         .defaultInstallments(1)
            //         .build();
            
            // Create preference request
            // Allows multiple payment methods (credit cards, debit, PSE, cash, etc.)
            // User will need to click "Return to merchant" button after payment
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .backUrls(backUrls)
                    .notificationUrl(webhookUrl)
                    .paymentMethods(paymentMethods) // Allow multiple payment methods
                    .statementDescriptor("NebulaCloud Plan") // Name that appears on card statement
                    .externalReference("USER_" + userEmail + "_PLAN_" + plan.getId()) // Reference for tracking
                    .expirationDateTo(OffsetDateTime.now().plusMinutes(29)) // 29 minutes from now
                    .build();
            
            // Create preference
            Preference preference = client.create(preferenceRequest);
            
            log.info("Payment preference created successfully for plan: {} user: {} preferenceId: {}", 
                    plan.getId(), userEmail, preference.getId());

            // Return response with checkout URL
            return PaymentResponse.builder()
                    .preferenceId(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .successUrl(baseUrl + "/api/v1/payments/success")
                    .failureUrl(baseUrl + "/api/v1/payments/failure")
                    .message("Preference created successfully")
                    .build();

        } catch (MPApiException apiException) {
            log.error("Mercado Pago API error: Status={}, Content={}", 
                    apiException.getStatusCode(), apiException.getApiResponse().getContent());
            throw new RuntimeException("Mercado Pago API error: " + apiException.getApiResponse().getContent(), apiException);
        } catch (MPException mpException) {
            log.error("Mercado Pago error", mpException);
            throw new RuntimeException("Error creating payment preference: " + mpException.getMessage(), mpException);
        } catch (Exception e) {
            log.error("Error creating payment preference in Mercado Pago", e);
            throw new RuntimeException("Error creating payment preference: " + e.getMessage(), e);
        }
    }

    /**
     * Gets payment details from Mercado Pago using the official SDK.
     * 
     * @param paymentId Payment ID in Mercado Pago.
     * @return Map with payment details.
     */
    public Map<String, Object> getPaymentDetails(Long paymentId) {
        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(paymentId);
            
            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("id", payment.getId());
            paymentDetails.put("status", payment.getStatus());
            paymentDetails.put("status_detail", payment.getStatusDetail());
            paymentDetails.put("transaction_amount", payment.getTransactionAmount());
            paymentDetails.put("date_created", payment.getDateCreated());
            paymentDetails.put("date_approved", payment.getDateApproved());
            paymentDetails.put("external_reference", payment.getExternalReference());
            
            log.info("Payment details obtained for paymentId: {} status: {} external_reference: {}", 
                    paymentId, payment.getStatus(), payment.getExternalReference());
            return paymentDetails;
            
        } catch (MPApiException apiException) {
            log.error("Mercado Pago API error getting payment: Status={}, Content={}", 
                    apiException.getStatusCode(), apiException.getApiResponse().getContent());
            throw new RuntimeException("Mercado Pago API error: " + apiException.getApiResponse().getContent(), apiException);
        } catch (MPException mpException) {
            log.error("Mercado Pago error getting payment: {}", paymentId, mpException);
            throw new RuntimeException("Error querying payment: " + mpException.getMessage(), mpException);
        } catch (Exception e) {
            log.error("Error getting payment details: {}", paymentId, e);
            throw new RuntimeException("Error querying payment: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies if a payment was approved.
     * 
     * @param paymentStatus Payment status from Mercado Pago.
     * @return true if status is APPROVED.
     */
    public boolean isPaymentApproved(String paymentStatus) {
        return paymentStatus != null && paymentStatus.equalsIgnoreCase("approved");
    }
}
