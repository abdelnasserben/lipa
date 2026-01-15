package com.lipa.api.dto;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        String role,
        Instant issuedAt,
        Instant expiresAt
) {}
