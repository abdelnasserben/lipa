package com.lipa.application.dto;

import java.util.UUID;

/**
 * Lightweight view of a card used by application use cases.
 * Avoids exposing JPA entities to keep the application layer independent.
 */
public record CardSnapshot(
        UUID id,
        String uid,
        String status,
        UUID accountId
) {
}
