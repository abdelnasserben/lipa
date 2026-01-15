package com.lipa.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Commande de mise à jour de l'état PIN d'une carte (failCount, blockedUntil).
 */
public record VerifyPinCardUpdate(
        UUID cardId,
        int pinFailCount,
        Instant pinBlockedUntil,
        Instant updatedAt
) {
}
