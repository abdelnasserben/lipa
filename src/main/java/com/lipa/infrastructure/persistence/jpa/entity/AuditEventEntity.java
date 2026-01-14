package com.lipa.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "audit_event",
        indexes = {
                @Index(name = "idx_audit_event_created_at", columnList = "created_at"),
                @Index(name = "idx_audit_event_action", columnList = "action")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AuditEventEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 30)
    private ActorType actorType;

    @Column(name = "actor_id", columnDefinition = "uuid")
    private UUID actorId;

    @Column(nullable = false, length = 80)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30)
    private TargetType targetType;

    @Column(name = "target_id", columnDefinition = "uuid")
    private UUID targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> metadata;

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
        if (metadata == null) {
            metadata = Map.of();
        }
    }

    public enum ActorType {CLIENT, MERCHANT, AGENT, ADMIN, SYSTEM}

    public enum TargetType {ACCOUNT, CARD, TRANSACTION}
}
