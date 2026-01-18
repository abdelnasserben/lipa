package com.lipa.application.port.out;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface PaymentAuditPort {

    void record(Command command);

    /**
     * Application-level audit command (infra decides how to store it).
     */
    record Command(
            String actorType,
            UUID actorId,
            String action,
            String targetType,
            UUID targetId,
            Map<String, Object> metadata,
            Instant createdAt
    ) {}
}
