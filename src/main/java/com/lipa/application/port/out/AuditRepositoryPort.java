package com.lipa.application.port.out;

import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;

public interface AuditRepositoryPort {
    AuditEventEntity save(AuditEventEntity entity);
}
