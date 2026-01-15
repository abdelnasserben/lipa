package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.CashInPersistCommand;
import com.lipa.application.dto.CashInPersistResult;
import com.lipa.application.port.out.CashInPersistencePort;
import com.lipa.infrastructure.persistence.jpa.entity.AuditEventEntity;
import com.lipa.infrastructure.persistence.jpa.entity.LedgerEntryEntity;
import com.lipa.infrastructure.persistence.jpa.entity.TransactionEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AccountJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.AuditEventJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.LedgerEntryJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CashInPersistenceAdapter implements CashInPersistencePort {

    private final AccountJpaRepository accountRepo;
    private final TransactionJpaRepository transactionRepo;
    private final LedgerEntryJpaRepository ledgerRepo;
    private final AuditEventJpaRepository auditRepo;

    public CashInPersistenceAdapter(AccountJpaRepository accountRepo,
                                    TransactionJpaRepository transactionRepo,
                                    LedgerEntryJpaRepository ledgerRepo,
                                    AuditEventJpaRepository auditRepo) {
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.ledgerRepo = ledgerRepo;
        this.auditRepo = auditRepo;
    }

    @Override
    public CashInPersistResult persist(CashInPersistCommand command) {
        // Defensive: the use case has already validated existence, but we keep this adapter robust.
        var client = accountRepo.findById(command.clientAccountId())
                .orElseThrow(() -> new IllegalStateException("Client account missing id=" + command.clientAccountId()));

        var technical = accountRepo.findById(command.technicalAccountId())
                .orElseThrow(() -> new IllegalStateException("Technical account missing id=" + command.technicalAccountId()));

        // Transaction
        TransactionEntity txn = new TransactionEntity();
        txn.setId(UUID.randomUUID());
        txn.setType(TransactionEntity.TransactionType.CASH_IN);
        txn.setStatus(TransactionEntity.TransactionStatus.SUCCESS);
        txn.setAmount(command.amount());
        txn.setCurrency(command.currency());
        txn.setIdempotencyKey(command.idempotencyKey());
        txn.setDescription(command.description());
        txn.setCreatedAt(command.createdAt());

        txn = transactionRepo.save(txn);

        // Ledger entries
        LedgerEntryEntity creditClient = new LedgerEntryEntity();
        creditClient.setId(UUID.randomUUID());
        creditClient.setTransaction(txn);
        creditClient.setAccount(client);
        creditClient.setDirection(LedgerEntryEntity.Direction.CREDIT);
        creditClient.setAmount(command.amount());
        creditClient.setCreatedAt(command.createdAt());
        ledgerRepo.save(creditClient);

        LedgerEntryEntity debitTechnical = new LedgerEntryEntity();
        debitTechnical.setId(UUID.randomUUID());
        debitTechnical.setTransaction(txn);
        debitTechnical.setAccount(technical);
        debitTechnical.setDirection(LedgerEntryEntity.Direction.DEBIT);
        debitTechnical.setAmount(command.amount());
        debitTechnical.setCreatedAt(command.createdAt());
        ledgerRepo.save(debitTechnical);

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
                "currency", command.currency(),
                "idempotencyKey", command.idempotencyKey()
        ));
        audit.setCreatedAt(command.createdAt());
        auditRepo.save(audit);

        return new CashInPersistResult(txn.getId(), txn.getStatus().name());
    }
}
