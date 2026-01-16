package com.lipa.application.port.out;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Generic audit port used by multiple use-cases.
 *
 * <p>Concrete ports (EnrollCardAuditPort, SetPinAuditPort, etc.) extend this interface to keep
 * the hexagonal boundaries explicit while avoiding duplicated method signatures.</p>
 */
public interface AuditPort {

    void record(
            String action,
            UUID targetId,
            Map<String, Object> metadata,
            Instant createdAt
    );
}
