package com.lipa.application.usecase;

import com.lipa.application.dto.AccountSnapshot;
import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.CreatePaymentUseCase;
import com.lipa.application.port.in.VerifyPinUseCase;
import com.lipa.application.port.out.*;
import com.lipa.application.util.BalanceCalculator;
import com.lipa.application.util.DomainRules;
import com.lipa.application.util.InputRules;
import com.lipa.application.util.MoneyRules;
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

    private final CardRepositoryPort cards;
    private final AccountSnapshotPort accounts;
    private final AccountReadPort ledger; // includes AccountBalancePort

    private final PaymentPersistencePort persistencePort;
    private final PaymentFeePort paymentFeePort;
    private final PaymentAuditPort auditPort;
    private final TimeProviderPort time;

    public CreatePaymentService(PaymentIdempotencyPort idempotencyPort,
                                VerifyPinUseCase verifyPinUseCase,
                                CardRepositoryPort cards,
                                AccountSnapshotPort accounts,
                                AccountReadPort ledger,
                                PaymentPersistencePort persistencePort,
                                PaymentFeePort paymentFeePort,
                                PaymentAuditPort auditPort,
                                TimeProviderPort time) {
        this.idempotencyPort = idempotencyPort;
        this.verifyPinUseCase = verifyPinUseCase;
        this.cards = cards;
        this.accounts = accounts;
        this.ledger = ledger;
        this.persistencePort = persistencePort;
        this.paymentFeePort = paymentFeePort;
        this.auditPort = auditPort;
        this.time = time;
    }

    @Override
    @Transactional
    public Result create(Command command) {
        Validated v = validate(command);
        Instant now = time.now();

        // 1) Verify PIN FIRST (prevents idempotency from bypassing authentication)
        var pinResult = verifyPinUseCase.verify(new VerifyPinUseCase.Command(v.cardUid, v.pin));
        if (!pinResult.success()) {
            if (pinResult.cardBlocked()) {
                throw new BusinessRuleException("PIN blocked or invalid");
            }
            throw new BusinessRuleException("Invalid PIN");
        }

        // 2) Load card AFTER PIN (still validate card status + linked account)
        var card = cards.findByUid(v.cardUid)
                .orElseThrow(() -> new NotFoundException("Card not found for uid=" + v.cardUid));

        DomainRules.requireStatusActive("Card", card.status().name());
        DomainRules.requireNotNull(card.accountId(), "Card has no account linked");

        UUID payerId = card.accountId();

        // 3) Idempotency AFTER PIN + card validation
        var existing = idempotencyPort.findByIdempotencyKey(v.idempotencyKey);
        if (existing.isPresent()) {
            return new Result(existing.get().transactionId(), existing.get().status());
        }

        // 4) Lock payer account + status checks
        AccountSnapshot payer = accounts.findByIdForUpdate(payerId)
                .orElseThrow(() -> new NotFoundException("Payer account not found id=" + payerId));
        DomainRules.requireStatusActive("Payer account", payer.status());

        AccountSnapshot merchant = accounts.findById(command.merchantAccountId())
                .orElseThrow(() -> new NotFoundException("Merchant account not found id=" + command.merchantAccountId()));
        DomainRules.requireStatusActive("Merchant account", merchant.status());

        // 5) Balance check (DRY)
        BigDecimal balance = BalanceCalculator.balanceOf(ledger, payerId);
        BigDecimal fee = paymentFeePort.quote(command.amount()).feeAmount();
        BigDecimal amountWithFee = command.amount().add(fee);

        if (balance.compareTo(amountWithFee) < 0) {
            auditPort.record(new PaymentAuditPort.Command(
                    "CLIENT",
                    payerId,
                    "PAYMENT_REFUSED_INSUFFICIENT_FUNDS",
                    "ACCOUNT",
                    payerId,
                    Map.of(
                            "cardUid", v.cardUid,
                            "balance", balance.toPlainString(),
                            "amount", command.amount().toPlainString(),
                            "currency", v.currency,
                            "merchantAccountId", command.merchantAccountId().toString(),
                            "idempotencyKey", v.idempotencyKey
                    ),
                    now
            ));
            throw new BusinessRuleException("Insufficient funds");
        }

        // 6) Persist transaction + ledger
        var persisted = persistencePort.persist(new PaymentPersistencePort.PersistCommand(
                payerId,
                command.merchantAccountId(),
                command.amount(),
                v.currency,
                v.idempotencyKey,
                v.description,
                now
        ));

        // 7) Audit success
        auditPort.record(new PaymentAuditPort.Command(
                "CLIENT",
                payerId,
                "PAYMENT_CREATED",
                "TRANSACTION",
                persisted.transactionId(),
                Map.of(
                        "cardUid", v.cardUid,
                        "payerAccountId", payerId.toString(),
                        "merchantAccountId", command.merchantAccountId().toString(),
                        "amount", command.amount().toPlainString(),
                        "currency", v.currency,
                        "idempotencyKey", v.idempotencyKey
                ),
                now
        ));

        return new Result(persisted.transactionId(), persisted.status());
    }

    private Validated validate(Command command) {
        String cardUid = InputRules.requireTrimmedNotBlank(command.cardUid(), "cardUid");
        String pin = InputRules.requireTrimmedNotBlank(command.pin(), "pin");

        if (command.merchantAccountId() == null) {
            throw new BusinessRuleException("merchantAccountId is required");
        }

        MoneyRules.requirePositive(command.amount(), "amount");

        String currency = MoneyRules.normalizeCurrency(command.currency());
        String idemKey = InputRules.requireTrimmedNotBlank(command.idempotencyKey(), "idempotencyKey");
        String description = InputRules.trimToNull(command.description());

        return new Validated(cardUid, pin, currency, idemKey, description);
    }

    private record Validated(String cardUid, String pin, String currency, String idempotencyKey, String description) {}
}
