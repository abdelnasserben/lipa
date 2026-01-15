package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.PaymentAuditCommand;
import com.lipa.application.port.out.PaymentAuditPort;
import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AuditEventJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentAuditAdapter implements PaymentAuditPort {

    private final AuditEventJpaRepository repo;

    public PaymentAuditAdapter(AuditEventJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void record(PaymentAuditCommand command) {
        AuditEventEntity audit = new AuditEventEntity();
        audit.setId(UUID.randomUUID());

        // Actor
        audit.setActorType(parseActorType(command.actorType()));
        audit.setActorId(command.actorId());

        // Target
        audit.setAction(command.action());
        audit.setTargetType(parseTargetType(command.targetType()));
        audit.setTargetId(command.targetId());

        audit.setMetadata(command.metadata());
        audit.setCreatedAt(command.createdAt());

        repo.save(audit);
    }

    private AuditEventEntity.ActorType parseActorType(String raw) {
        if (raw == null) return AuditEventEntity.ActorType.SYSTEM;
        try {
            return AuditEventEntity.ActorType.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return AuditEventEntity.ActorType.SYSTEM;
        }
    }

    private AuditEventEntity.TargetType parseTargetType(String raw) {
        if (raw == null) return AuditEventEntity.TargetType.TRANSACTION;
        try {
            return AuditEventEntity.TargetType.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return AuditEventEntity.TargetType.TRANSACTION;
        }
    }
}
