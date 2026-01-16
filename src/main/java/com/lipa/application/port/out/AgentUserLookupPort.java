package com.lipa.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface AgentUserLookupPort {
    Optional<UUID> findByUsername(String username);
}
