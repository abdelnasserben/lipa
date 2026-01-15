package com.lipa.application.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Application-level audit command (the infra adapter decides how to store it).
 */
public record PaymentAuditCommand(
        String actorType,
        UUID actorId,
        String action,
        String targetType,
        UUID targetId,
        Map<String, Object> metadata,
        Instant createdAt
) {
}
