package com.lipa.application.port.in;

import java.time.Instant;
import java.util.UUID;

public interface CreateAgentUserUseCase {

    Result create(Command command);

    record Command(
            String username,
            String password,
            String role
    ) {}

    record Result(
            UUID userId,
            String username,
            String role,
            String status,
            Instant createdAt
    ) {}
}
