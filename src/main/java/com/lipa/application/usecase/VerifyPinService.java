package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.VerifyPinUseCase;
import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.application.port.out.PinHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.port.out.VerifyPinAuditPort;
import com.lipa.domain.model.PinPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class VerifyPinService implements VerifyPinUseCase {

    private final CardRepositoryPort cards;
    private final PinHasherPort hasher;
    private final VerifyPinAuditPort auditPort;
    private final TimeProviderPort time;

    private final PinPolicy policy = PinPolicy.standard();

    public VerifyPinService(CardRepositoryPort cards,
                            PinHasherPort hasher,
                            VerifyPinAuditPort auditPort,
                            TimeProviderPort time) {
        this.cards = cards;
        this.hasher = hasher;
        this.auditPort = auditPort;
        this.time = time;
    }

    @Override
    @Transactional
    public Result verify(Command command) {
        String uid = normalizeUid(command.cardUid());
        String pin = command.rawPin();

        if (pin == null || pin.trim().isEmpty()) {
            throw new BusinessRuleException("PIN is required");
        }

        var card = cards.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Card not found for uid=" + uid));

        Instant now = time.now();

        // Domaine décide l'issue (y compris statut non actif / blocage)
        boolean pinMatches = card.pinHash() != null && !card.pinHash().isBlank()
                && hasher.matches(pin, card.pinHash());

        var decision = card.verifyPinAttempt(pinMatches, policy, now);

        if (!decision.updatedCard().equals(card)) {
            // persist only if state changed (failcount reset/increment, block/unblock, updatedAt)
            cards.save(decision.updatedCard());
        }

        // Audit + mapping vers le résultat UseCase (contrat inchangé)
        if (!"ACTIVE".equalsIgnoreCase(card.status().name())) {
            auditPort.record(
                    "PIN_VERIFY_DENIED",
                    card.id(),
                    Map.of("uid", uid, "reason", "CARD_NOT_ACTIVE"),
                    now
            );
            return new Result(false, true);
        }

        if (card.pinBlockedUntil() != null && now.isBefore(card.pinBlockedUntil())) {
            auditPort.record(
                    "PIN_VERIFY_DENIED",
                    card.id(),
                    Map.of(
                            "uid", uid,
                            "reason", "PIN_BLOCKED",
                            "blockedUntil", card.pinBlockedUntil().toString()
                    ),
                    now
            );
            return new Result(false, true);
        }

        if (card.pinHash() == null || card.pinHash().isBlank()) {
            auditPort.record(
                    "PIN_VERIFY_DENIED",
                    card.id(),
                    Map.of("uid", uid, "reason", "PIN_NOT_SET"),
                    now
            );
            return new Result(false, false);
        }

        if (decision.success()) {
            auditPort.record(
                    "PIN_VERIFY_SUCCESS",
                    card.id(),
                    Map.of("uid", uid),
                    now
            );
            return new Result(true, false);
        }

        auditPort.record(
                "PIN_VERIFY_FAILED",
                card.id(),
                Map.of(
                        "uid", uid,
                        "failCount", String.valueOf(decision.updatedCard().pinFailCount()),
                        "blocked", String.valueOf(decision.cardBlocked()),
                        "blockedUntil", decision.blockedUntil() == null ? "" : decision.blockedUntil().toString()
                ),
                now
        );

        return new Result(false, decision.cardBlocked());
    }

    private String normalizeUid(String uid) {
        if (uid == null) throw new BusinessRuleException("cardUid is required");
        String trimmed = uid.trim();
        if (trimmed.isEmpty()) throw new BusinessRuleException("cardUid is required");
        return trimmed;
    }
}
