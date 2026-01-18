package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.AuditRepositoryPort;
import com.lipa.infrastructure.persistence.entity.AuditEventEntity;
import com.lipa.infrastructure.persistence.repo.AuditEventJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditRepositoryAdapter implements AuditRepositoryPort {

    private final AuditEventJpaRepository repo;

    public AuditRepositoryAdapter(AuditEventJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public AuditEventEntity save(AuditEventEntity entity) {
        return repo.save(entity);
    }
}
