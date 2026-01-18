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

    /**
     * Reconstruit une carte depuis persistance (immutable).
     */
    public static Card restored(UUID id,
                                String uid,
                                UUID accountId,
                                CardStatus status,
                                String pinHash,
                                int pinFailCount,
                                Instant pinBlockedUntil,
                                Instant createdAt,
                                Instant updatedAt) {
        return new Card(
                id,
                uid,
                accountId,
                status,
                pinHash,
                pinFailCount,
                pinBlockedUntil,
                createdAt,
                updatedAt
        );
    }

    public Card withPinSet(String newPinHash, Instant now) {
        return new Card(
                this.id,
                this.uid,
                this.accountId,
                this.status,
                newPinHash,
                0,
                null,
                this.createdAt,
                now
        );
    }

    /**
     * Décide métier d'une tentative PIN.
     * @param pinMatches résultat technique (hasher.matches) fourni par l'application
     */
    public PinVerification verifyPinAttempt(boolean pinMatches, PinPolicy policy, Instant now) {
        if (status != CardStatus.ACTIVE) {
            return new PinVerification(false, true, this, null);
        }

        if (pinBlockedUntil != null && now.isBefore(pinBlockedUntil)) {
            return new PinVerification(false, true, this, pinBlockedUntil);
        }

        if (pinHash == null || pinHash.isBlank()) {
            return new PinVerification(false, false, this, null);
        }

        if (pinMatches) {
            Card updated = new Card(
                    this.id,
                    this.uid,
                    this.accountId,
                    this.status,
                    this.pinHash,
                    0,
                    null,
                    this.createdAt,
                    now
            );
            return new PinVerification(true, false, updated, null);
        }

        int newFails = this.pinFailCount + 1;
        Instant blockedUntil = null;
        boolean blocked = false;

        if (newFails >= policy.maxFails()) {
            blocked = true;
            blockedUntil = now.plus(policy.blockDuration());
        }

        Card updated = new Card(
                this.id,
                this.uid,
                this.accountId,
                this.status,
                this.pinHash,
                newFails,
                blockedUntil,
                this.createdAt,
                now
        );

        return new PinVerification(false, blocked, updated, blockedUntil);
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
