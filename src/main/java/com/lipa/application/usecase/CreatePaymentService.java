package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.CreatePaymentUseCase;
import com.lipa.application.port.in.VerifyPinUseCase;
import com.lipa.application.port.out.*;
import com.lipa.infrastructure.persistence.jpa.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class CreatePaymentService implements CreatePaymentUseCase {

    private final CardRepositoryPort cardRepository;
    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final LedgerEntryRepositoryPort ledgerRepository;
    private final AuditRepositoryPort auditRepository;
    private final VerifyPinUseCase verifyPinUseCase;
    private final TimeProviderPort time;

    public CreatePaymentService(CardRepositoryPort cardRepository,
                                AccountRepositoryPort accountRepository,
                                TransactionRepositoryPort transactionRepository,
                                LedgerEntryRepositoryPort ledgerRepository,
                                AuditRepositoryPort auditRepository,
                                VerifyPinUseCase verifyPinUseCase,
                                TimeProviderPort time) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.auditRepository = auditRepository;
        this.verifyPinUseCase = verifyPinUseCase;
        this.time = time;
    }

    @Override
    @Transactional
    public Result create(Command command) {
        validate(command);

        // 1) Idempotency: si la transaction existe déjà, on la renvoie
        var existing = transactionRepository.findByIdempotencyKey(command.idempotencyKey().trim());
        if (existing.isPresent()) {
            return new Result(existing.get().getId(), existing.get().getStatus().name());
        }

        // 2) Vérifier PIN
        var pinResult = verifyPinUseCase.verify(new VerifyPinUseCase.Command(command.cardUid(), command.pin()));
        if (!pinResult.success()) {
            if (pinResult.cardBlocked()) {
                throw new BusinessRuleException("PIN blocked or invalid");
            }
            throw new BusinessRuleException("Invalid PIN");
        }

        // 3) Charger carte + comptes
        CardEntity card = cardRepository.findByUid(command.cardUid().trim())
                .orElseThrow(() -> new NotFoundException("Card not found for uid=" + command.cardUid()));

        if (card.getStatus() != CardEntity.CardStatus.ACTIVE) {
            throw new BusinessRuleException("Card is not active");
        }

        AccountEntity payer = card.getAccount();
        if (payer == null) {
            throw new BusinessRuleException("Card has no account linked");
        }

        AccountEntity merchant = accountRepository.findById(command.merchantAccountId())
                .orElseThrow(() -> new NotFoundException("Merchant account not found id=" + command.merchantAccountId()));

        if (merchant.getStatus() != AccountEntity.AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Merchant account is not active");
        }

        Instant now = time.now();

        // 4) Créer la transaction
        TransactionEntity txn = new TransactionEntity();
        txn.setId(UUID.randomUUID());
        txn.setType(TransactionEntity.TransactionType.PAYMENT);
        txn.setStatus(TransactionEntity.TransactionStatus.SUCCESS);
        txn.setAmount(command.amount());
        txn.setCurrency(command.currency().trim());
        txn.setIdempotencyKey(command.idempotencyKey().trim());
        txn.setDescription(command.description());
        txn.setCreatedAt(now);

        txn = transactionRepository.save(txn);

        // 5) Écritures ledger (source de vérité)
        LedgerEntryEntity debit = new LedgerEntryEntity();
        debit.setId(UUID.randomUUID());
        debit.setTransaction(txn);
        debit.setAccount(payer);
        debit.setDirection(LedgerEntryEntity.Direction.DEBIT);
        debit.setAmount(command.amount());
        debit.setCreatedAt(now);
        ledgerRepository.save(debit);

        LedgerEntryEntity credit = new LedgerEntryEntity();
        credit.setId(UUID.randomUUID());
        credit.setTransaction(txn);
        credit.setAccount(merchant);
        credit.setDirection(LedgerEntryEntity.Direction.CREDIT);
        credit.setAmount(command.amount());
        credit.setCreatedAt(now);
        ledgerRepository.save(credit);

        // 6) Audit
        AuditEventEntity audit = new AuditEventEntity();
        audit.setId(UUID.randomUUID());
        audit.setActorType(AuditEventEntity.ActorType.CLIENT);
        audit.setActorId(payer.getId());
        audit.setAction("PAYMENT_CREATED");
        audit.setTargetType(AuditEventEntity.TargetType.TRANSACTION);
        audit.setTargetId(txn.getId());
        audit.setMetadata(Map.of(
                "cardUid", command.cardUid().trim(),
                "payerAccountId", payer.getId().toString(),
                "merchantAccountId", merchant.getId().toString(),
                "amount", command.amount().toPlainString(),
                "currency", command.currency().trim(),
                "idempotencyKey", command.idempotencyKey().trim()
        ));
        audit.setCreatedAt(now);
        auditRepository.save(audit);

        return new Result(txn.getId(), txn.getStatus().name());
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
}
