package com.lipa.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface TokenIssuerPort {

    Token issue(Subject subject, Instant now);

    record Subject(
            UUID userId,
            String username,
            String role
    ) {}

    record Token(
            String token,
            long expiresInSeconds,
            Instant issuedAt,
            Instant expiresAt
    ) {}
}
