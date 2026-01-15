package com.lipa.application.usecase;

import com.lipa.application.exception.BusinessRuleException;
import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.in.CreateCashInUseCase;
import com.lipa.application.port.out.*;
import com.lipa.infrastructure.persistence.jpa.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class CreateCashInService implements CreateCashInUseCase {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final LedgerEntryRepositoryPort ledgerRepository;
    private final AuditRepositoryPort auditRepository;
    private final TimeProviderPort time;

    public CreateCashInService(AccountRepositoryPort accountRepository,
                               TransactionRepositoryPort transactionRepository,
                               LedgerEntryRepositoryPort ledgerRepository,
                               AuditRepositoryPort auditRepository,
                               TimeProviderPort time) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.auditRepository = auditRepository;
        this.time = time;
    }

    @Override
    @Transactional
    public Result create(Command command) {
        validate(command);

        // Idempotency
        var existing = transactionRepository.findByIdempotencyKey(command.idempotencyKey().trim());
        if (existing.isPresent()) {
            return new Result(existing.get().getId(), existing.get().getStatus().name());
        }

        // Load accounts
        AccountEntity client = accountRepository.findById(command.clientAccountId())
                .orElseThrow(() -> new NotFoundException("Client account not found id=" + command.clientAccountId()));

        AccountEntity technical = accountRepository.findById(command.technicalAccountId())
                .orElseThrow(() -> new NotFoundException("Technical account not found id=" + command.technicalAccountId()));

        if (client.getStatus() != AccountEntity.AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Client account is not active");
        }
        if (technical.getStatus() != AccountEntity.AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Technical account is not active");
        }
        if (technical.getType() != AccountEntity.AccountType.TECHNICAL) {
            throw new BusinessRuleException("Source account must be TECHNICAL");
        }

        Instant now = time.now();

        // Create transaction
        TransactionEntity txn = new TransactionEntity();
        txn.setId(UUID.randomUUID());
        txn.setType(TransactionEntity.TransactionType.CASH_IN);
        txn.setStatus(TransactionEntity.TransactionStatus.SUCCESS);
        txn.setAmount(command.amount());
        txn.setCurrency(command.currency().trim());
        txn.setIdempotencyKey(command.idempotencyKey().trim());
        txn.setDescription(command.description());
        txn.setCreatedAt(now);

        txn = transactionRepository.save(txn);

        // Ledger entries
        // CREDIT client
        LedgerEntryEntity creditClient = new LedgerEntryEntity();
        creditClient.setId(UUID.randomUUID());
        creditClient.setTransaction(txn);
        creditClient.setAccount(client);
        creditClient.setDirection(LedgerEntryEntity.Direction.CREDIT);
        creditClient.setAmount(command.amount());
        creditClient.setCreatedAt(now);
        ledgerRepository.save(creditClient);

        // DEBIT technical
        LedgerEntryEntity debitTechnical = new LedgerEntryEntity();
        debitTechnical.setId(UUID.randomUUID());
        debitTechnical.setTransaction(txn);
        debitTechnical.setAccount(technical);
        debitTechnical.setDirection(LedgerEntryEntity.Direction.DEBIT);
        debitTechnical.setAmount(command.amount());
        debitTechnical.setCreatedAt(now);
        ledgerRepository.save(debitTechnical);

        // Audit
        AuditEventEntity audit = new AuditEventEntity();
        audit.setId(UUID.randomUUID());
        audit.setActorType(AuditEventEntity.ActorType.SYSTEM);
        audit.setAction("CASH_IN_CREATED");
        audit.setTargetType(AuditEventEntity.TargetType.TRANSACTION);
        audit.setTargetId(txn.getId());
        audit.setMetadata(Map.of(
                "clientAccountId", client.getId().toString(),
                "technicalAccountId", technical.getId().toString(),
                "amount", command.amount().toPlainString(),
                "currency", command.currency().trim(),
                "idempotencyKey", command.idempotencyKey().trim()
        ));
        audit.setCreatedAt(now);
        auditRepository.save(audit);

        return new Result(txn.getId(), txn.getStatus().name());
    }

    private void validate(Command command) {
        if (command.clientAccountId() == null) {
            throw new BusinessRuleException("clientAccountId is required");
        }
        if (command.technicalAccountId() == null) {
            throw new BusinessRuleException("technicalAccountId is required");
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
