package com.lipa.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Snapshot minimal d'une carte pour la vérification PIN.
 * Aucun type JPA ici : uniquement des primitives + UUID.
 */
public record CardPinSnapshot(
        UUID id,
        String uid,
        String status,
        String pinHash,
        int pinFailCount,
        Instant pinBlockedUntil
) {
}
