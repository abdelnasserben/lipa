package com.lipa.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class Card {

    private final UUID id;
    private final String uid;
    private final UUID accountId;
    private final CardStatus status;

    private final String pinHash;
    private final int pinFailCount;
    private final Instant pinBlockedUntil;

    private final Instant createdAt;
    private final Instant updatedAt;

    private Card(UUID id,
                 String uid,
                 UUID accountId,
                 CardStatus status,
                 String pinHash,
                 int pinFailCount,
                 Instant pinBlockedUntil,
                 Instant createdAt,
                 Instant updatedAt) {
        this.id = id;
        this.uid = uid;
        this.accountId = accountId;
        this.status = status;
        this.pinHash = pinHash;
        this.pinFailCount = pinFailCount;
        this.pinBlockedUntil = pinBlockedUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Card enrolled(UUID id, String uid, UUID accountId, Instant now) {
        return new Card(
                id,
                uid,
                accountId,
                CardStatus.ACTIVE,
                null,
                0,
                null,
                now,
                now
        );
    }

    public UUID id() {
        return id;
    }

    public String uid() {
        return uid;
    }

    public UUID accountId() {
        return accountId;
    }

    public CardStatus status() {
        return status;
    }

    public String pinHash() {
        return pinHash;
    }

    public int pinFailCount() {
        return pinFailCount;
    }

    public Instant pinBlockedUntil() {
        return pinBlockedUntil;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
