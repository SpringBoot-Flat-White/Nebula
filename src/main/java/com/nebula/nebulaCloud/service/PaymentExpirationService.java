package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.model.Payment;
import com.nebula.nebulaCloud.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service that automatically marks old pending payments as FAILED.
 * <p>
 * This avoids leaving payments in PENDING status forever when the user
 * abandons the checkout and no webhook is received from Mercado Pago.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationService {

    private final PaymentRepository paymentRepository;

    /**
     * Runs every 5 minutes and marks as FAILED any payment that:
     * - is currently in PENDING status, and
     * - was created more than 60 minutes ago.
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void expireOldPendingPayments() {
        LocalDateTime limit = LocalDateTime.now().minusMinutes(60);
        List<Payment> pendingPayments =
                paymentRepository.findByStatusAndCreatedAtBefore(Payment.Status.PENDING, limit);

        if (pendingPayments.isEmpty()) {
            return;
        }

        log.info("Expiring {} pending payments older than 30 minutes", pendingPayments.size());

        pendingPayments.forEach(payment -> payment.setStatus(Payment.Status.FAILED));
        paymentRepository.saveAll(pendingPayments);
    }
}
