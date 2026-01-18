package com.lipa.infrastructure.adapter.persistence;

import com.lipa.application.exception.NotFoundException;
import com.lipa.application.port.out.PaymentPersistencePort;
import com.lipa.infrastructure.persistence.entity.LedgerEntryEntity;
import com.lipa.infrastructure.persistence.entity.TransactionEntity;
import com.lipa.infrastructure.persistence.repo.AccountJpaRepository;
import com.lipa.infrastructure.persistence.repo.LedgerEntryJpaRepository;
import com.lipa.infrastructure.persistence.repo.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentPersistenceAdapter implements PaymentPersistencePort {

    private final AccountJpaRepository accountRepo;
    private final TransactionJpaRepository transactionRepo;
    private final LedgerEntryJpaRepository ledgerRepo;
    private final PlatformFeeEngineAdapter feeEngine;

    public PaymentPersistenceAdapter(AccountJpaRepository accountRepo,
                                     TransactionJpaRepository transactionRepo,
                                     LedgerEntryJpaRepository ledgerRepo,
                                     PlatformFeeEngineAdapter feeEngine) {
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.ledgerRepo = ledgerRepo;
        this.feeEngine = feeEngine;
    }

    @Override
    public PersistResult persist(PersistCommand command) {

        var payer = accountRepo.findById(command.payerAccountId())
                .orElseThrow(() -> new NotFoundException("Payer account not found id=" + command.payerAccountId()));

        var merchant = accountRepo.findById(command.merchantAccountId())
                .orElseThrow(() -> new NotFoundException("Merchant account not found id=" + command.merchantAccountId()));

        // Fees (DRY) : config active + calcul + fee account
        var feeComputed = feeEngine.compute(command.amount());
        BigDecimal feeAmount = feeComputed.feeAmount();
        var feeAccount = feeComputed.feeAccount();

        // Transaction (keeps amount = payment amount, not total)
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

        // Ledger: payer DEBIT (amount + fee)
        LedgerEntryEntity debit = new LedgerEntryEntity();
        debit.setId(UUID.randomUUID());
        debit.setTransaction(txn);
        debit.setAccount(payer);
        debit.setDirection(LedgerEntryEntity.Direction.DEBIT);
        debit.setAmount(command.amount().add(feeAmount));
        debit.setCreatedAt(command.createdAt());
        ledgerRepo.save(debit);

        // Ledger: merchant CREDIT (amount)
        LedgerEntryEntity creditMerchant = new LedgerEntryEntity();
        creditMerchant.setId(UUID.randomUUID());
        creditMerchant.setTransaction(txn);
        creditMerchant.setAccount(merchant);
        creditMerchant.setDirection(LedgerEntryEntity.Direction.CREDIT);
        creditMerchant.setAmount(command.amount());
        creditMerchant.setCreatedAt(command.createdAt());
        ledgerRepo.save(creditMerchant);

        // Ledger: platform fees CREDIT (fee)
        LedgerEntryEntity creditFee = new LedgerEntryEntity();
        creditFee.setId(UUID.randomUUID());
        creditFee.setTransaction(txn);
        creditFee.setAccount(feeAccount);
        creditFee.setDirection(LedgerEntryEntity.Direction.CREDIT);
        creditFee.setAmount(feeAmount);
        creditFee.setCreatedAt(command.createdAt());
        ledgerRepo.save(creditFee);

        return new PersistResult(txn.getId(), txn.getStatus().name());
    }
}
