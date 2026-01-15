package com.lipa.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class Account {

    private final UUID id;
    private final AccountType type;
    private final AccountStatus status;
    private final String displayName;
    private final String phone;
    private final Instant createdAt;

    private Account(UUID id,
                    AccountType type,
                    AccountStatus status,
                    String displayName,
                    String phone,
                    Instant createdAt) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.displayName = displayName;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public static Account newClient(UUID id, String displayName, String phone, Instant now) {
        return new Account(
                id,
                AccountType.CLIENT,
                AccountStatus.ACTIVE,
                displayName,
                phone,
                now
        );
    }

    public UUID id() {
        return id;
    }

    public AccountType type() {
        return type;
    }

    public AccountStatus status() {
        return status;
    }

    public String displayName() {
        return displayName;
    }

    public String phone() {
        return phone;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
