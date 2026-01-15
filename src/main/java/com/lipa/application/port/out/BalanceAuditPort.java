package com.lipa.application.port.out;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface BalanceAuditPort {

    void record(
            String action,
            UUID accountId,
            Map<String, Object> metadata,
            Instant createdAt
    );
}
