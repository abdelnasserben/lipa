package com.lipa.application.dto;

import java.util.UUID;

/**
 * Snapshot minimal d'une carte pour l'usage "Set PIN".
 * Aucun type JPA : uniquement primitives/UUID.
 */
public record SetPinCardSnapshot(
        UUID id,
        String uid,
        String status
) {
}
