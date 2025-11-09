package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment made by a user for a specific plan.
 *
 * Each payment stores the transaction details, amount, and status,
 * allowing the system to track billing and subscription activities.
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    /**
     * Enum representing the possible states of a payment.
     */
    public enum Status {
        APPROVED, PENDING, FAILED, PAUSED
    }

    /**
     * Unique identifier for the payment (auto-generated).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who made the payment.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The plan associated with the payment.
     */
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    /**
     * Current status of the payment (e.g., PENDING, APPROVED).
     */
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * Identifier returned by the external transaction processor.
     */
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    /**
     * Total amount paid for the plan.
     *
     * precision = 10 → allows up to 10 digits in total.
     * scale = 2 → reserves 2 digits for decimal places.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Timestamp indicating when the payment record was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
