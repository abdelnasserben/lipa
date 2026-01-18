package com.lipa.infrastructure.adapter.persistence;

import com.lipa.infrastructure.persistence.entity.AuditEventEntity;
import com.lipa.infrastructure.persistence.repo.AuditEventJpaRepository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Shared JPA implementation for audit recording.
 *
 * <p>Several use-cases audit the same way (SYSTEM actor, an action, a target type + id, metadata, createdAt).
 * This base class centralizes the persistence logic and keeps per-use-case adapters minimal.</p>
 */
abstract class AbstractJpaAuditAdapter {

    private final AuditEventJpaRepository auditRepo;
    private final AuditEventEntity.TargetType targetType;

    protected AbstractJpaAuditAdapter(AuditEventJpaRepository auditRepo, AuditEventEntity.TargetType targetType) {
        this.auditRepo = auditRepo;
        this.targetType = targetType;
    }

    protected void recordInternal(String action, UUID targetId, Map<String, Object> metadata, Instant createdAt) {
        AuditEventEntity audit = new AuditEventEntity();
        audit.setId(UUID.randomUUID());

        audit.setActorType(AuditEventEntity.ActorType.SYSTEM);
        audit.setActorId(null);

        audit.setAction(action);

        audit.setTargetType(targetType);
        audit.setTargetId(targetId);

        audit.setMetadata(metadata == null ? Map.of() : metadata);
        audit.setCreatedAt(createdAt == null ? Instant.now() : createdAt);

        auditRepo.save(audit);
    }
}
