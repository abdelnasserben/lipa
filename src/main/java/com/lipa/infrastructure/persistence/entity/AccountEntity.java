package com.lipa.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="account")
@Getter @Setter @NoArgsConstructor
public class AccountEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private AccountStatus status;

    @Column(length=120)
    private String displayName;

    @Column(length=32)
    private String phone;

    @Column(nullable=false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public enum AccountType { CLIENT, MERCHANT, TECHNICAL }
    public enum AccountStatus { ACTIVE, SUSPENDED, BLOCKED }
}
