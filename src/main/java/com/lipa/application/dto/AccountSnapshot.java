package com.lipa.application.dto;

import java.util.UUID;

/**
 * Lightweight view of an account used by application use cases.
 *
 * <p>Intentionally avoids exposing JPA entities to keep the application layer
 * independent from the persistence implementation.</p>
 */
public record AccountSnapshot(
        UUID id,
        String type,
        String status
) {
}
