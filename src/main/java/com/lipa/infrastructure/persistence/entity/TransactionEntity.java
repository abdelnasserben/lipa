package com.lipa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "transaction",
        indexes = {
                @Index(name = "ux_transaction_idempotency_key", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_transaction_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TransactionEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Column(length = 280)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (currency == null) {
            currency = "KMF";
        }
    }


    public enum TransactionType {PAYMENT, CASH_IN, FEE}

    public enum TransactionStatus {SUCCESS, FAILED}
}
