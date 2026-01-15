package com.lipa.application.usecase;

import com.lipa.application.dto.CardPinSnapshot;
import com.lipa.application.dto.VerifyPinCardUpdate;
import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.VerifyPinUseCase;
import com.lipa.application.port.out.PinHasherPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.application.port.out.VerifyPinAuditPort;
import com.lipa.application.port.out.VerifyPinCardPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class VerifyPinService implements VerifyPinUseCase {

    private static final int MAX_FAILS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final VerifyPinCardPort cardPort;
    private final PinHasherPort hasher;
    private final VerifyPinAuditPort auditPort;
    private final TimeProviderPort time;

    public VerifyPinService(VerifyPinCardPort cardPort,
                            PinHasherPort hasher,
                            VerifyPinAuditPort auditPort,
                            TimeProviderPort time) {
        this.cardPort = cardPort;
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

        CardPinSnapshot card = cardPort.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Card not found for uid=" + uid));

        if (!"ACTIVE".equalsIgnoreCase(card.status())) {
            auditPort.record(
                    "PIN_VERIFY_DENIED",
                    card.id(),
                    Map.of("uid", uid, "reason", "CARD_NOT_ACTIVE"),
                    time.now()
            );
            return new Result(false, true);
        }

        Instant now = time.now();

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

        boolean ok = hasher.matches(pin, card.pinHash());
        if (ok) {
            cardPort.applyUpdate(new VerifyPinCardUpdate(
                    card.id(),
                    0,
                    null,
                    now
            ));

            auditPort.record(
                    "PIN_VERIFY_SUCCESS",
                    card.id(),
                    Map.of("uid", uid),
                    now
            );
            return new Result(true, false);
        }

        int newFails = card.pinFailCount() + 1;
        boolean blocked = false;
        Instant blockedUntil = null;

        if (newFails >= MAX_FAILS) {
            blocked = true;
            blockedUntil = now.plus(BLOCK_DURATION);
        }

        cardPort.applyUpdate(new VerifyPinCardUpdate(
                card.id(),
                newFails,
                blockedUntil,
                now
        ));

        auditPort.record(
                "PIN_VERIFY_FAILED",
                card.id(),
                Map.of(
                        "uid", uid,
                        "failCount", String.valueOf(newFails),
                        "blocked", String.valueOf(blocked),
                        "blockedUntil", blockedUntil == null ? "" : blockedUntil.toString()
                ),
                now
        );

        return new Result(false, blocked);
    }

    private String normalizeUid(String uid) {
        if (uid == null) throw new BusinessRuleException("cardUid is required");
        String trimmed = uid.trim();
        if (trimmed.isEmpty()) throw new BusinessRuleException("cardUid is required");
        return trimmed;
    }
}
