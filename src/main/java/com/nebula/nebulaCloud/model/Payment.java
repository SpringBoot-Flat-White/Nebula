package com.nebula.nebulaCloud.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    public enum Status {
        APPROVED, PENDING, FAILED, PAUSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(precision = 10, scale = 2)
    private Double amount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

