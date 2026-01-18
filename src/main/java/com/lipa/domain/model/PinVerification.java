package com.lipa.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Résultat métier d'une tentative de vérification PIN.
 * - success : PIN correct
 * - cardBlocked : l'action est bloquée (statut non actif ou blocage PIN en cours ou blocage déclenché)
 * - updatedCard : nouvel état carte à persister (si changement)
 * - blockedUntil : utile pour audit/logs (peut être null)
 */
public record PinVerification(
        boolean success,
        boolean cardBlocked,
        Card updatedCard,
        Instant blockedUntil
) {
    public PinVerification {
        Objects.requireNonNull(updatedCard, "updatedCard is required");
    }
}
