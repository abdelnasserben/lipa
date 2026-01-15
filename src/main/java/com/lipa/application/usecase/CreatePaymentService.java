package com.lipa.application.usecase;

import com.lipa.application.dto.*;
import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.CreatePaymentUseCase;
import com.lipa.application.port.in.VerifyPinUseCase;
import com.lipa.application.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class CreatePaymentService implements CreatePaymentUseCase {

    private final PaymentIdempotencyPort idempotencyPort;
    private final VerifyPinUseCase verifyPinUseCase;
    private final PaymentCardPort cardPort;
    private final PaymentAccountPort accountPort;
    private final PaymentLedgerPort ledgerPort;
    private final PaymentPersistencePort persistencePort;
    private final PaymentAuditPort auditPort;
    private final TimeProviderPort time;

    public CreatePaymentService(PaymentIdempotencyPort idempotencyPort,
                                VerifyPinUseCase verifyPinUseCase,
                                PaymentCardPort cardPort,
                                PaymentAccountPort accountPort,
                                PaymentLedgerPort ledgerPort,
                                PaymentPersistencePort persistencePort,
                                PaymentAuditPort auditPort,
                                TimeProviderPort time) {
        this.idempotencyPort = idempotencyPort;
        this.verifyPinUseCase = verifyPinUseCase;
        this.cardPort = cardPort;
        this.accountPort = accountPort;
        this.ledgerPort = ledgerPort;
        this.persistencePort = persistencePort;
        this.auditPort = auditPort;
        this.time = time;
    }

    @Override
    @Transactional
    public Result create(Command command) {
        validate(command);

        String uid = command.cardUid().trim();
        String currency = command.currency().trim();
        String idemKey = command.idempotencyKey().trim();
        Instant now = time.now();

        // 1) Idempotency
        var existing = idempotencyPort.findByIdempotencyKey(idemKey);
        if (existing.isPresent()) {
            return new Result(existing.get().transactionId(), existing.get().status());
        }

        // 2) Vérifier PIN (use case existant)
        var pinResult = verifyPinUseCase.verify(new VerifyPinUseCase.Command(uid, command.pin()));
        if (!pinResult.success()) {
            if (pinResult.cardBlocked()) {
                throw new BusinessRuleException("PIN blocked or invalid");
            }
            throw new BusinessRuleException("Invalid PIN");
        }

        // 3) Charger la carte (snapshot)
        CardSnapshot card = cardPort.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Card not found for uid=" + uid));

        if (!"ACTIVE".equalsIgnoreCase(card.status())) {
            throw new BusinessRuleException("Card is not active");
        }
        if (card.accountId() == null) {
            throw new BusinessRuleException("Card has no account linked");
        }

        // 4) Verrou DB sur le compte payeur + contrôles statuts
        UUID payerId = card.accountId();

        AccountSnapshot payer = accountPort.findByIdForUpdate(payerId)
                .orElseThrow(() -> new NotFoundException("Payer account not found id=" + payerId));

        if (!"ACTIVE".equalsIgnoreCase(payer.status())) {
            throw new BusinessRuleException("Payer account is not active");
        }

        AccountSnapshot merchant = accountPort.findById(command.merchantAccountId())
                .orElseThrow(() -> new NotFoundException("Merchant account not found id=" + command.merchantAccountId()));

        if (!"ACTIVE".equalsIgnoreCase(merchant.status())) {
            throw new BusinessRuleException("Merchant account is not active");
        }

        // 5) Contrôle de solde (strict)
        BigDecimal credits = defaultZero(ledgerPort.sumCredits(payerId));
        BigDecimal debits = defaultZero(ledgerPort.sumDebits(payerId));
        BigDecimal balance = credits.subtract(debits);

        if (balance.compareTo(command.amount()) < 0) {
            // Audit refusé (utile pour le backoffice / investigations)
            auditPort.record(new PaymentAuditCommand(
                    "CLIENT",
                    payerId,
                    "PAYMENT_REFUSED_INSUFFICIENT_FUNDS",
                    "ACCOUNT",
                    payerId,
                    Map.of(
                            "cardUid", uid,
                            "balance", balance.toPlainString(),
                            "amount", command.amount().toPlainString(),
                            "currency", currency,
                            "merchantAccountId", command.merchantAccountId().toString(),
                            "idempotencyKey", idemKey
                    ),
                    now
            ));

            throw new BusinessRuleException("Insufficient funds");
        }

        // 6) Persister transaction + ledger (infra)
        PaymentPersistResult persisted = persistencePort.persist(new PaymentPersistCommand(
                payerId,
                command.merchantAccountId(),
                command.amount(),
                currency,
                idemKey,
                command.description(),
                now
        ));

        // 7) Audit succès
        auditPort.record(new PaymentAuditCommand(
                "CLIENT",
                payerId,
                "PAYMENT_CREATED",
                "TRANSACTION",
                persisted.transactionId(),
                Map.of(
                        "cardUid", uid,
                        "payerAccountId", payerId.toString(),
                        "merchantAccountId", command.merchantAccountId().toString(),
                        "amount", command.amount().toPlainString(),
                        "currency", currency,
                        "idempotencyKey", idemKey
                ),
                now
        ));

        return new Result(persisted.transactionId(), persisted.status());
    }

    private void validate(Command command) {
        if (command.cardUid() == null || command.cardUid().trim().isEmpty()) {
            throw new BusinessRuleException("cardUid is required");
        }
        if (command.pin() == null || command.pin().trim().isEmpty()) {
            throw new BusinessRuleException("pin is required");
        }
        if (command.merchantAccountId() == null) {
            throw new BusinessRuleException("merchantAccountId is required");
        }
        if (command.amount() == null) {
            throw new BusinessRuleException("amount is required");
        }
        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("amount must be > 0");
        }
        if (command.currency() == null || command.currency().trim().length() != 3) {
            throw new BusinessRuleException("currency must be 3 letters");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().trim().isEmpty()) {
            throw new BusinessRuleException("idempotencyKey is required");
        }
    }

    private BigDecimal defaultZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
