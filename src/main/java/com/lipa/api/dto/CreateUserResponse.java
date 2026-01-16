package com.lipa.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateUserResponse(
        UUID userId,
        String username,
        String role,
        String status,
        Instant createdAt
) {}
