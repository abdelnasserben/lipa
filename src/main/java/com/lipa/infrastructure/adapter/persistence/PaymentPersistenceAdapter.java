package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.dto.PaymentPersistCommand;
import com.lipa.application.dto.PaymentPersistResult;
import com.lipa.application.port.out.PaymentPersistencePort;
import com.lipa.infrastructure.persistence.jpa.entity.LedgerEntryEntity;
import com.lipa.infrastructure.persistence.jpa.entity.TransactionEntity;
import com.lipa.infrastructure.persistence.jpa.repo.AccountJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.LedgerEntryJpaRepository;
import com.lipa.infrastructure.persistence.jpa.repo.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentPersistenceAdapter implements PaymentPersistencePort {

    private final AccountJpaRepository accountRepo;
    private final TransactionJpaRepository transactionRepo;
    private final LedgerEntryJpaRepository ledgerRepo;

    public PaymentPersistenceAdapter(AccountJpaRepository accountRepo,
                                     TransactionJpaRepository transactionRepo,
                                     LedgerEntryJpaRepository ledgerRepo) {
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.ledgerRepo = ledgerRepo;
    }

    @Override
    public PaymentPersistResult persist(PaymentPersistCommand command) {
        var payer = accountRepo.findById(command.payerAccountId())
                .orElseThrow(() -> new IllegalStateException("Payer account missing id=" + command.payerAccountId()));

        var merchant = accountRepo.findById(command.merchantAccountId())
                .orElseThrow(() -> new IllegalStateException("Merchant account missing id=" + command.merchantAccountId()));

        // Transaction
        TransactionEntity txn = new TransactionEntity();
        txn.setId(UUID.randomUUID());
        txn.setType(TransactionEntity.TransactionType.PAYMENT);
        txn.setStatus(TransactionEntity.TransactionStatus.SUCCESS);
        txn.setAmount(command.amount());
        txn.setCurrency(command.currency());
        txn.setIdempotencyKey(command.idempotencyKey());
        txn.setDescription(command.description());
        txn.setCreatedAt(command.createdAt());

        txn = transactionRepo.save(txn);

        // Ledger: payer DEBIT
        LedgerEntryEntity debit = new LedgerEntryEntity();
        debit.setId(UUID.randomUUID());
        debit.setTransaction(txn);
        debit.setAccount(payer);
        debit.setDirection(LedgerEntryEntity.Direction.DEBIT);
        debit.setAmount(command.amount());
        debit.setCreatedAt(command.createdAt());
        ledgerRepo.save(debit);

        // Ledger: merchant CREDIT
        LedgerEntryEntity credit = new LedgerEntryEntity();
        credit.setId(UUID.randomUUID());
        credit.setTransaction(txn);
        credit.setAccount(merchant);
        credit.setDirection(LedgerEntryEntity.Direction.CREDIT);
        credit.setAmount(command.amount());
        credit.setCreatedAt(command.createdAt());
        ledgerRepo.save(credit);

        return new PaymentPersistResult(txn.getId(), txn.getStatus().name());
    }
}
