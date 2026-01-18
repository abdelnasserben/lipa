package com.lipa.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AgentUserRepositoryPort {

    Optional<UUID> findIdByUsername(String username);

    UUID create(CreateUserCommand command);

    enum Role { AGENT, ADMIN }
    enum Status { ACTIVE, DISABLED }

    record CreateUserCommand(
            String username,
            String passwordHash,
            Role role,
            Status status,
            Instant now
    ) {}
}
