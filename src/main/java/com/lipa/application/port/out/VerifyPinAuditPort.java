package com.lipa.application.port.out;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface VerifyPinAuditPort {

    void record(String action, UUID cardId, Map<String, Object> metadata, Instant createdAt);
}
