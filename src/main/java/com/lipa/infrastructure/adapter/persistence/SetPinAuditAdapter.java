package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.port.out.SetPinAuditPort;
import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AuditEventJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class SetPinAuditAdapter extends AbstractJpaAuditAdapter implements SetPinAuditPort {

    public SetPinAuditAdapter(AuditEventJpaRepository repo) {
        super(repo, AuditEventEntity.TargetType.CARD);
    }

    @Override
    public void record(String action, UUID targetId, Map<String, Object> metadata, Instant createdAt) {
        recordInternal(action, targetId, metadata, createdAt);
    }
}
