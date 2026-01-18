package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.port.in.EnrollCardUseCase;
import com.lipa.application.port.out.CardRepositoryPort;
import com.lipa.application.port.out.EnrollCardAuditPort;
import com.lipa.application.port.out.TimeProviderPort;
import com.lipa.domain.model.Account;
import com.lipa.domain.model.Card;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class EnrollCardService implements EnrollCardUseCase {

    private final CardRepositoryPort cards;
    private final EnrollCardAuditPort auditPort;
    private final TimeProviderPort time;

    public EnrollCardService(CardRepositoryPort cards,
                             EnrollCardAuditPort auditPort,
                             TimeProviderPort time) {
        this.cards = cards;
        this.auditPort = auditPort;
        this.time = time;
    }

    @Override
    @Transactional
    public Result enroll(Command command) {
        String uid = normalizeUid(command.cardUid());

        if (cards.existsByUid(uid)) {
            throw new BusinessRuleException("Card already enrolled for uid=" + uid);
        }

        Instant now = time.now();

        UUID accountId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        Account account = Account.newClient(accountId, command.displayName(), command.phone(), now);
        Card card = Card.enrolled(cardId, uid, accountId, now);

        cards.createAccountAndCard(account, card);

        auditPort.record(
                "CARD_ENROLLED",
                cardId,
                Map.of(
                        "uid", uid,
                        "accountId", accountId.toString()
                ),
                now
        );

        return new Result(accountId, cardId, uid);
    }

    private String normalizeUid(String uid) {
        if (uid == null) throw new BusinessRuleException("cardUid is required");
        String trimmed = uid.trim();
        if (trimmed.isEmpty()) throw new BusinessRuleException("cardUid is required");
        return trimmed;
    }
}
