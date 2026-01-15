package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.SetPinAuditPort;
import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AuditEventJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class SetPinAuditAdapter implements SetPinAuditPort {

    private final AuditEventJpaRepository repo;

    public SetPinAuditAdapter(AuditEventJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void record(String action, UUID cardId, Map<String, Object> metadata, Instant createdAt) {
        AuditEventEntity audit = new AuditEventEntity();
        audit.setId(UUID.randomUUID());

        audit.setActorType(AuditEventEntity.ActorType.SYSTEM);
        audit.setActorId(null);

        audit.setAction(action);

        audit.setTargetType(AuditEventEntity.TargetType.CARD);
        audit.setTargetId(cardId);

        audit.setMetadata(metadata == null ? Map.of() : metadata);
        audit.setCreatedAt(createdAt == null ? Instant.now() : createdAt);

        repo.save(audit);
    }
}
