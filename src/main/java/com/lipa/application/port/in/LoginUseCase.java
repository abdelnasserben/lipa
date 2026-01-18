package com.lipa.application.port.in;

import java.time.Instant;

public interface LoginUseCase {

    Result login(Command command);

    record Command(String username, String password) {}

    record Result(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            String username,
            String role,
            Instant issuedAt,
            Instant expiresAt
    ) {}
}
