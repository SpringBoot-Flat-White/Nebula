package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Payment;
import com.nebula.nebulaCloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar operaciones de persistencia con la entidad Payment.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Busca un pago por su transaction ID de Mercado Pago (Preference ID).
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Busca un pago por su Mercado Pago Payment ID (usado en webhooks).
     */
    Optional<Payment> findByMercadoPagoPaymentId(String mercadoPagoPaymentId);
    
    /**
     * Busca todos los pagos de un usuario.
     */
    List<Payment> findByUser(User user);

    /**
     * Busca pagos por estado creados antes de una fecha/hora dada.
     * Usado para expirar pagos pendientes antiguos.
     */
    List<Payment> findByStatusAndCreatedAtBefore(Payment.Status status, LocalDateTime createdAt);

    /**
     * Busca pagos por usuario y estado.
     */
    List<Payment> findByUserAndStatus(User user, Payment.Status status);
}
