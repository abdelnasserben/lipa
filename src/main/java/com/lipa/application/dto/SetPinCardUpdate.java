package com.lipa.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Commande de mise à jour de l'état PIN (hash + reset fails + unblock).
 */
public record SetPinCardUpdate(
        UUID cardId,
        String pinHash,
        int pinFailCount,
        Instant pinBlockedUntil,
        Instant updatedAt
) {
}
