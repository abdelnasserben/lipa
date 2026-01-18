package com.lipa.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepositoryPort {

    Optional<AuthUserView> findByUsername(String username);

    record AuthUserView(
            UUID id,
            String username,
            String passwordHash,
            String role,
            String status
    ) {}
}
