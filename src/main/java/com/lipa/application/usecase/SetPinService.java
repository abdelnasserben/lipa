package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.SetPinUseCase;
import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.application.port.out.PinHasherPort;
import com.lipa.application.port.out.SetPinAuditPort;
import com.lipa.application.port.out.TimeProviderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class SetPinService implements SetPinUseCase {

    private final CardRepositoryPort cards;
    private final PinHasherPort hasher;
    private final SetPinAuditPort auditPort;
    private final TimeProviderPort time;

    public SetPinService(CardRepositoryPort cards,
                         PinHasherPort hasher,
                         SetPinAuditPort auditPort,
                         TimeProviderPort time) {
        this.cards = cards;
        this.hasher = hasher;
        this.auditPort = auditPort;
        this.time = time;
    }

    @Override
    @Transactional
    public void setPin(Command command) {
        String uid = normalizeUid(command.cardUid());
        validatePin(command.rawPin());

        var card = cards.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Card not found for uid=" + uid));

        if (!"ACTIVE".equalsIgnoreCase(card.status().name())) {
            throw new BusinessRuleException("Card is not active");
        }

        Instant now = time.now();

        var updated = card.withPinSet(hasher.hash(command.rawPin()), now);
        cards.save(updated);

        auditPort.record(
                "PIN_SET",
                updated.id(),
                Map.of(
                        "uid", uid,
                        "reason", safeReason(command.reason())
                ),
                now
        );
    }

    private void validatePin(String pin) {
        if (pin == null) throw new BusinessRuleException("PIN is required");
        String t = pin.trim();
        if (t.length() < 4 || t.length() > 8) {
            throw new BusinessRuleException("PIN length must be between 4 and 8");
        }
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) {
                throw new BusinessRuleException("PIN must contain only digits");
            }
        }
    }

    private String normalizeUid(String uid) {
        if (uid == null) throw new BusinessRuleException("cardUid is required");
        String trimmed = uid.trim();
        if (trimmed.isEmpty()) throw new BusinessRuleException("cardUid is required");
        return trimmed;
    }

    private String safeReason(String reason) {
        if (reason == null) return "N/A";
        String r = reason.trim();
        if (r.isEmpty()) return "N/A";
        return r.length() > 120 ? r.substring(0, 120) : r;
    }
}
