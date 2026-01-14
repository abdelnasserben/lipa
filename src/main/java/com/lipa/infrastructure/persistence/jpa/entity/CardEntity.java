package com.lipa.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="card", indexes = {
        @Index(name="idx_card_account_id", columnList="account_id")
})
@Getter
@Setter
@NoArgsConstructor
public class CardEntity {

    @Id
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(nullable=false, length=80, unique=true)
    private String uid;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="account_id", nullable=false)
    private AccountEntity account;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private CardStatus status;

    @Column(length=255)
    private String pinHash;

    @Column(nullable=false)
    private int pinFailCount;

    private Instant pinBlockedUntil;

    @Column(nullable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        var now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public enum CardStatus { ACTIVE, BLOCKED, LOST }
}

